# Spring AI RAG Agent

基于 Spring AI 和阿里云 DashScope 构建的 RAG（检索增强生成）系统，支持多格式文档处理、中文语义分块、向量存储和智能对话。

## 项目结构

```
agent/
├── rag/                    # RAG 模块 - 文档处理与向量存储
│   ├── reader/             # 多格式文档读取器
│   ├── cleaner/            # 文档清洗器
│   ├── spliter/            # 文本分块器
│   ├── embedding/          # 向量化服务
│   └── controller/         # REST API 控制器
├── springai/               # Agent 模块 - AI 对话功能
│   └── controller/         # Chat API 控制器
└── pom.xml                 # 父 POM
```

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 21 |
| Spring Boot | 3.5.12 |
| Spring AI | 1.1.0 |
| Spring AI Alibaba | 1.1.0.0 |
| PostgreSQL + pgvector | - |
| HanLP | portable-1.8.4 |

## 功能特性

### RAG 模块

#### 1. 多格式文档读取

支持以下文档格式：

| 格式 | 实现类 |
|------|--------|
| PDF | `PdfDocumentReaderStrategy` |
| HTML | `JsoupDocumentReaderStrategy` |
| Markdown | `MarkdownDocumentReaderStrategy` |
| JSON | `JsonDocumentReaderStrategy` |
| Text | `TextDocumentReaderStrategy` |
| Office/Tika | `TikaDocumentReaderStrategy` |

#### 2. 文档清洗

`DocumentCleaner` 提供多种清洗选项：

- HTML/XML 标签移除
- 空白字符规范化
- 控制字符移除
- 换行符规范化
- 空行移除
- 连字符行断修复
- Unicode 规范化
- 零宽字符移除

#### 3. 文本分块

提供多种分块策略：

| 分块器 | 说明 |
|--------|------|
| `TokenTextSplitter` | 基于 Token 数量分块 |
| `OverlapParagraphTextSplitter` | 按段落分块，支持重叠 |
| `RecursiveCharacterTextSplitter` | 递归字符分块 |
| `ChineseSemanticTextSplitter` | **中文语义分块**（基于 HanLP） |

#### 4. 向量化与存储

- 使用阿里云 DashScope `text-embedding-v4` 模型
- 支持自定义维度（768/1024）
- PostgreSQL pgvector 向量存储
- HNSW 索引， cosine_distance 距离计算

### SpringAI 模块

- ChatModel API：同步/流式对话
- ChatClient API：高级对话客户端
- 支持 SystemMessage/UserMessage 组合
- 支持多模型切换（如 deepseek-r1）

## 快速开始

### 1. 环境准备

#### PostgreSQL + pgvector

```bash
# 使用 Docker 启动 pgvector
docker run -d \
  --name pgvector \
  -e POSTGRES_USER=pgvector \
  -e POSTGRES_PASSWORD=pgvector \
  -e POSTGRES_DB=rag \
  -p 5432:5432 \
  pgvector/pgvector:pg16
```

#### 配置 DashScope API Key

在 `application.yml` 中配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
```

或设置环境变量：

```bash
export DASHSCOPE_API_KEY=your-api-key
```

### 2. 构建与运行

```bash
# 构建项目
./mvnw clean package -DskipTests

# 运行 RAG 模块
./mvnw spring-boot:run -pl rag

# 运行 Agent 模块
./mvnw spring-boot:run -pl springai
```

### 3. 服务端口

| 模块 | 端口 |
|------|------|
| rag | 8001 |
| springai | 8000 |

## API 接口

### RAG 模块 (`http://localhost:8001/rag`)

#### 文档读取

```
GET /rag/read?filePath=/path/to/file.pdf
```

#### 文本分块

| 接口 | 说明 |
|------|------|
| `GET /rag/split?filePath=...` | Token 分块 |
| `GET /rag/overlap_split?filePath=...` | 段落重叠分块 |
| `GET /rag/recursive_split?filePath=...` | 递归分块 |
| `GET /rag/chinese_semantic_split?filePath=...` | **中文语义分块** |

#### 向量化

```
GET /rag/embedding/test           # 测试 embedding
GET /rag/embedding/embed?filePath=...  # 分块并向量化存储
```

### Agent 模块 (`http://localhost:8000`)

#### ChatModel API

```
GET /model/call/string?message=你好
GET /model/call/messages?message=你好
GET /model/call/stream?message=你好    # 流式响应
GET /model/call/prompt?message=你好    # 自定义模型
```

#### ChatClient API

```
GET /client/call/messages?message=你好  # 流式响应
```

## 中文语义分块器

`ChineseSemanticTextSplitter` 是专为中文文本设计的语义分块器：

### 特性

- 基于 HanLP 实现中文句子分割
- 识别中文标点符号（。！？；）作为句子边界
- 不会在句子中间切断
- 支持自定义 `chunkSize` 和 `overlap`
- 超长句子使用 HanLP 分词智能分割

### 使用示例

```java
ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(500, 100);
List<Document> chunks = splitter.apply(documents);
```

### 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `chunkSize` | 每块最大字符数 | - |
| `overlap` | 相邻块重叠字符数 | - |
| `preserveSentence` | 是否保持句子完整性 | true |

## Embedding 维度配置

`text-embedding-v3/v4` 支持动态维度配置：

```yaml
spring:
  ai:
    dashscope:
      embedding:
        options:
          model: "text-embedding-v4"
          dimensions: 768  # 支持 768/1024
    vectorstore:
      pgvector:
        dimensions: 768  # 需与 embedding 维度一致
```

| 模型 | 支持维度 |
|------|----------|
| text-embedding-v3 | 1024 |
| text-embedding-v4 | 768, 1024 |

**注意**：修改维度后需删除旧表重建：

```sql
DROP TABLE IF EXISTS vector_st;
```

## 配置说明

### RAG 模块 (`rag/src/main/resources/application.yml`)

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      embedding:
        options:
          model: "text-embedding-v4"
          dimensions: 768
    vectorstore:
      pgvector:
        index-type: hnsw
        distance-type: cosine_distance
        dimensions: 768
        initialize-schema: true
        table-name: vector_st
  datasource:
    url: jdbc:postgresql://localhost:5432/rag
    username: pgvector
    password: pgvector
```

## 扩展开发

### 添加新的文档读取器

1. 实现 `IDocumentReaderStrategy` 接口：

```java
@Component
public class MyDocumentReaderStrategy implements IDocumentReaderStrategy {
    
    @Override
    public boolean supports(File file) {
        return file.getName().endsWith(".myformat");
    }
    
    @Override
    public List<Document> read(File file) throws IOException {
        // 实现读取逻辑
        return documents;
    }
}
```

2. Spring 会自动注入到 `DocumentStrategyFactory`

### 添加新的分块器

继承 `TextSplitter` 并实现 `splitText` 方法：

```java
public class MyTextSplitter extends TextSplitter {
    
    public List<String> splitText(String text) {
        // 实现分块逻辑
        return chunks;
    }
    
    public List<Document> apply(List<Document> documents) {
        // 批量处理
        return result;
    }
}
```

## License

MIT License