# 企业智能数据分析前端（Garfish）

Garfish 微前端主应用 + 两个子应用（洞察 / 助手）。

## 应用结构
- `shell`（当前目录）：主应用编排与路由
- `insight-app`：数据洞察子应用
- `assistant-app`：AI 助手子应用

## 运行
```bash
# 主应用
npm install
npm run dev

# 洞察子应用
cd insight-app
npm install
npm run dev

# 助手子应用
cd assistant-app
npm install
npm run dev
```

## 端口
- 主应用：`http://localhost:5173`
- 洞察子应用：`http://localhost:5174`
- 助手子应用：`http://localhost:5175`
