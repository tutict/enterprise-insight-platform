package com.tutict.eip.agentadapter.domain;

public class VerificationCommandResult {

    private String command;
    private int exitCode;
    private boolean timedOut;
    private String stdout;
    private String stderr;
    private long durationMillis;

    public VerificationCommandResult() {
    }

    public VerificationCommandResult(
            String command,
            int exitCode,
            boolean timedOut,
            String stdout,
            String stderr,
            long durationMillis
    ) {
        this.command = command;
        this.exitCode = exitCode;
        this.timedOut = timedOut;
        this.stdout = stdout;
        this.stderr = stderr;
        this.durationMillis = durationMillis;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }
}
