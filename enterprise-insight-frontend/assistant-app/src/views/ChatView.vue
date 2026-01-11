<template>
  <section class="panel">
    <!-- 顶部说明与快捷操作 -->
    <header class="header">
      <div>
        <h1>Insight Agent</h1>
        <p>Mock agent responses for analytics questions.</p>
      </div>
      <button class="ghost" @click="loadSample">Load Sample</button>
    </header>
    <!-- 对话气泡区 -->
    <div class="chat">
      <div v-for="msg in messages" :key="msg.id" :class="['bubble', msg.role]">
        <p class="role">{{ msg.role }}</p>
        <p>{{ msg.text }}</p>
      </div>
    </div>
    <!-- 输入区 -->
    <div class="input">
      <input v-model="draft" placeholder="Ask about revenue, conversion, or retention..." />
      <button class="primary" @click="send">Send</button>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

// 对话状态
const draft = ref('')
const messages = ref([
  {
    id: 1,
    role: 'assistant',
    text: 'Welcome! Ask me about KPIs, datasets, or SQL suggestions.',
  },
])

// 发送消息（Mock）
const send = () => {
  if (!draft.value.trim()) return
  messages.value.push({
    id: Date.now(),
    role: 'user',
    text: draft.value,
  })
  messages.value.push({
    id: Date.now() + 1,
    role: 'assistant',
    text: 'Mock reply: I recommend checking revenue trend for the last 7 days.',
  })
  draft.value = ''
}

// 加载示例对话
const loadSample = () => {
  messages.value.push({
    id: Date.now() + 2,
    role: 'assistant',
    text: 'Sample: revenue is up 12%, orders up 4.1%, AOV slightly down.',
  })
}
</script>

<style scoped>
.panel {
  padding: 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.header p {
  margin: 4px 0 0;
  color: rgba(255, 255, 255, 0.6);
}

.chat {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 360px;
  overflow: auto;
}

.bubble {
  padding: 12px;
  border-radius: 12px;
  background: rgba(10, 12, 18, 0.7);
}

.bubble.user {
  border: 1px solid rgba(100, 255, 191, 0.4);
}

.bubble.assistant {
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.role {
  margin: 0 0 6px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: rgba(255, 255, 255, 0.6);
}

.input {
  display: flex;
  gap: 10px;
}

input {
  flex: 1;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(10, 12, 18, 0.8);
  color: #fff;
}

.primary,
.ghost {
  border-radius: 10px;
  padding: 10px 16px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}

.primary {
  background: #64ffbf;
  color: #0b0b0f;
}

.ghost {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}
</style>
