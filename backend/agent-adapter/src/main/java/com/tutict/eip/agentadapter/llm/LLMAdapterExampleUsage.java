package com.tutict.eip.agentadapter.llm;

public class LLMAdapterExampleUsage {

    public String run(LLMConfig config) {
        LLMAdapter adapter = AdapterFactory.create(config);
        String result = adapter.generate("生成一个Spring Boot控制器");
        return result;
    }
}
