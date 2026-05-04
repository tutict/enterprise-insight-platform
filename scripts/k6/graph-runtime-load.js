import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  scenarios: {
    graph_runtime: {
      executor: 'ramping-vus',
      stages: [
        { duration: __ENV.RAMP_UP || '30s', target: Number(__ENV.VUS || 10) },
        { duration: __ENV.STEADY || '1m', target: Number(__ENV.VUS || 10) },
        { duration: __ENV.RAMP_DOWN || '20s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800', 'p(99)<1500'],
    graph_compile_latency: ['p(95)<500'],
    graph_run_latency: ['p(95)<800'],
    compiler_latency: ['p(95)<700'],
    contract_failures: ['rate<0.01'],
  },
};

const graphCompileLatency = new Trend('graph_compile_latency', true);
const graphRunLatency = new Trend('graph_run_latency', true);
const compilerLatency = new Trend('compiler_latency', true);
const contractFailures = new Rate('contract_failures');

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const USERNAME = __ENV.USERNAME || 'admin';
const PASSWORD = __ENV.PASSWORD || 'admin';
const AUTH_MODE = (__ENV.AUTH_MODE || 'gateway').toLowerCase();
const SKIP_COMPILER = (__ENV.SKIP_COMPILER || 'false').toLowerCase() === 'true';
const RUN_ORCHESTRATOR = (__ENV.RUN_ORCHESTRATOR || 'false').toLowerCase() === 'true';

export function setup() {
  if (AUTH_MODE === 'none') {
    return { token: null };
  }
  if (__ENV.TOKEN) {
    return { token: __ENV.TOKEN };
  }

  const login = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: USERNAME, password: PASSWORD }),
    { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'auth_login' } },
  );

  check(login, {
    'login returned 200': (res) => res.status === 200,
  });

  const body = login.json();
  const token = body?.data?.token || body?.token;
  if (!token) {
    throw new Error(`login did not return a token: status=${login.status} body=${login.body}`);
  }
  return { token };
}

export default function (data) {
  const headers = {
    'Content-Type': 'application/json',
  };
  if (data.token) {
    headers.Authorization = `Bearer ${data.token}`;
  }

  if (!SKIP_COMPILER) {
    group('compiler contract', () => {
      const res = http.post(
        `${BASE_URL}/api/compiler/compile`,
        JSON.stringify({ requirement: 'Build a JWT login API with persistence and tests.' }),
        { headers, tags: { endpoint: 'compiler_compile' } },
      );
      compilerLatency.add(res.timings.duration);
      contractFailures.add(!check(res, {
        'compiler status is 200': (r) => r.status === 200,
        'compiler returns prompt': (r) => Boolean(r.json('prompt')),
        'compiler returns dsl': (r) => Boolean(r.json('dsl')),
      }));
    });
  }

  group('graph compile contract', () => {
    const res = http.post(
      `${BASE_URL}/api/graph/compile`,
      JSON.stringify(sampleGraph()),
      { headers, tags: { endpoint: 'graph_compile' } },
    );
    graphCompileLatency.add(res.timings.duration);
    contractFailures.add(!check(res, {
      'graph compile status is 200': (r) => r.status === 200,
      'graph compile valid': (r) => r.json('data.valid') === true,
    }));
  });

  group('graph run contract', () => {
    const res = http.post(
      `${BASE_URL}/api/graph/run`,
      JSON.stringify({ graph: sampleGraph() }),
      { headers, tags: { endpoint: 'graph_run' } },
    );
    graphRunLatency.add(res.timings.duration);
    contractFailures.add(!check(res, {
      'graph run accepted': (r) => r.status === 200,
      'graph run has runId': (r) => Boolean(r.json('data.runId')),
    }));
  });

  if (RUN_ORCHESTRATOR) {
    group('orchestrator pipeline contract', () => {
      const res = http.post(
        `${BASE_URL}/api/orchestrator/run`,
        JSON.stringify({
          requirement: 'Build a small health-check API.',
          targetDirectory: `k6-${__VU}-${__ITER}`,
          verifyCommands: [['mvn', 'test']],
          maxRepairRounds: 0,
        }),
        { headers, tags: { endpoint: 'orchestrator_run' }, timeout: '180s' },
      );
      contractFailures.add(!check(res, {
        'orchestrator completed': (r) => r.status === 200,
        'orchestrator returned generation': (r) => Boolean(r.json('data.generation')),
      }));
    });
  }

  sleep(Number(__ENV.SLEEP_SECONDS || 1));
}

export function handleSummary(data) {
  const duration = data.metrics.http_req_duration;
  const failed = data.metrics.http_req_failed;
  const reqs = data.metrics.http_reqs;
  const durationValues = duration?.values || {};
  const summary = {
    baseUrl: BASE_URL,
    authMode: AUTH_MODE,
    skipCompiler: SKIP_COMPILER,
    requests: reqs?.values?.count || 0,
    rps: Number((reqs?.values?.rate || 0).toFixed(2)),
    errorRate: Number((failed?.values?.rate || 0).toFixed(4)),
    latency: {
      avgMs: roundMetric(durationValues.avg),
      p95Ms: roundMetric(readMetric(durationValues, ['p(95)', 'p(95.00)'])),
      p99Ms: roundMetric(readMetric(durationValues, ['p(99)', 'p(99.00)'])),
    },
  };

  return {
    stdout: [
      'k6 graph runtime load summary',
      `baseUrl=${summary.baseUrl}`,
      `requests=${summary.requests}`,
      `rps=${summary.rps}`,
      `errorRate=${summary.errorRate}`,
      `latency.avgMs=${summary.latency.avgMs}`,
      `latency.p95Ms=${summary.latency.p95Ms}`,
      `latency.p99Ms=${summary.latency.p99Ms}`,
      '',
    ].join('\n'),
    'performance-results.json': JSON.stringify(summary, null, 2),
  };
}

function sampleGraph() {
  return {
    id: 'k6-graph',
    name: 'k6 Graph',
    startNodeId: 'start',
    maxIterations: 3,
    metadata: { requiredRepairIterations: 0 },
    nodes: [
      { id: 'start', label: 'start', type: 'start', config: {} },
      {
        id: 'check',
        label: 'check',
        type: 'condition',
        config: { expression: 'true' },
      },
      { id: 'end', label: 'end', type: 'end', config: {} },
    ],
    edges: [
      { id: 'start-check', source: 'start', target: 'check', condition: 'success', label: 'success' },
      { id: 'check-end', source: 'check', target: 'end', condition: 'success', label: 'success' },
      { id: 'check-start', source: 'check', target: 'start', condition: 'failed', label: 'failed', maxIterations: 1 },
    ],
  };
}

function readMetric(values, keys) {
  for (const key of keys) {
    if (typeof values[key] === 'number') {
      return values[key];
    }
  }
  return 0;
}

function roundMetric(value) {
  return Number((value || 0).toFixed(2));
}
