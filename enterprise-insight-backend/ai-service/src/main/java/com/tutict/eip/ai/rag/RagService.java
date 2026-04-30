package com.tutict.eip.ai.rag;

import com.tutict.eip.ai.AgentController.AgentAnswer;
import com.tutict.eip.ai.config.RagProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class RagService {

    private final OllamaClient ollamaClient;
    private final QdrantClient qdrantClient;
    private final RagProperties ragProperties;
    private final TextChunker textChunker;

    public RagService(
            OllamaClient ollamaClient,
            QdrantClient qdrantClient,
            RagProperties ragProperties,
            TextChunker textChunker
    ) {
        this.ollamaClient = ollamaClient;
        this.qdrantClient = qdrantClient;
        this.ragProperties = ragProperties;
        this.textChunker = textChunker;
    }

    public AgentAnswer answer(String question) {
        List<Double> queryEmbedding = singleEmbedding(question);
        qdrantClient.ensureCollection(queryEmbedding.size());
        List<QdrantClient.SearchHit> hits = qdrantClient.search(queryEmbedding, ragProperties.getTopK());
        ContextPack contextPack = buildContext(hits);
        String systemPrompt = buildSystemPrompt(contextPack.context());
        String reply = ollamaClient.chat(systemPrompt, question);
        List<String> nextActions = new ArrayList<>();
        if (!contextPack.sources().isEmpty()) {
            nextActions.addAll(contextPack.sources());
        } else {
            nextActions.add("Add relevant harness documents to the knowledge base");
            nextActions.add("Ask a more specific implementation question");
        }
        return new AgentAnswer(replyWithSources(reply, contextPack.sources()), "knowledge_answer", List.of(), nextActions);
    }

    public IngestResult ingest(List<IngestDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return new IngestResult(0, 0, List.of());
        }
        List<Chunk> chunks = new ArrayList<>();
        for (IngestDocument document : documents) {
            if (document == null || document.content() == null || document.content().isBlank()) {
                continue;
            }
            String docId = document.id() == null || document.id().isBlank()
                    ? UUID.randomUUID().toString()
                    : document.id();
            List<String> parts = textChunker.chunk(document.content());
            for (int i = 0; i < parts.size(); i++) {
                String chunkId = docId + ":" + i;
                String title = document.title() == null || document.title().isBlank()
                        ? "Document " + docId
                        : document.title();
                String text = title + "\n" + parts.get(i);
                Map<String, Object> payload = buildPayload(document, docId, i, title, parts.get(i));
                chunks.add(new Chunk(chunkId, text, payload));
            }
        }
        if (chunks.isEmpty()) {
            return new IngestResult(0, 0, List.of());
        }
        List<String> inputs = chunks.stream().map(Chunk::text).toList();
        List<List<Double>> embeddings = ollamaClient.embed(inputs);
        if (embeddings.size() != chunks.size()) {
            throw new IllegalStateException("Embedding size mismatch");
        }
        qdrantClient.ensureCollection(embeddings.get(0).size());
        List<QdrantClient.QdrantPoint> points = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            points.add(new QdrantClient.QdrantPoint(chunk.id(), embeddings.get(i), chunk.payload()));
        }
        qdrantClient.upsert(points);
        List<String> ids = chunks.stream().map(Chunk::id).distinct().toList();
        return new IngestResult(documents.size(), chunks.size(), ids);
    }

    private List<Double> singleEmbedding(String text) {
        List<List<Double>> embeddings = ollamaClient.embed(List.of(text));
        if (embeddings.isEmpty()) {
            throw new IllegalStateException("Empty embedding result");
        }
        return embeddings.get(0);
    }

    private ContextPack buildContext(List<QdrantClient.SearchHit> hits) {
        StringBuilder context = new StringBuilder();
        Set<String> sources = new LinkedHashSet<>();
        int maxChars = ragProperties.getMaxContextChars();
        for (QdrantClient.SearchHit hit : hits) {
            Object titleObj = hit.payload().get("title");
            Object contentObj = hit.payload().get("content");
            String title = titleObj == null ? "Reference" : titleObj.toString();
            String content = contentObj == null ? "" : contentObj.toString();
            if (content.isBlank()) {
                continue;
            }
            if (context.length() + content.length() > maxChars) {
                int remaining = Math.max(0, maxChars - context.length());
                content = content.substring(0, Math.min(content.length(), remaining));
            }
            context.append("[").append(title).append("]\n").append(content).append("\n\n");
            sources.add(title);
            if (context.length() >= maxChars) {
                break;
            }
        }
        return new ContextPack(context.toString().trim(), new ArrayList<>(sources));
    }

    private String buildSystemPrompt(String context) {
        if (context == null || context.isBlank()) {
            return """
                    You are an AI Harness Compiler knowledge assistant.
                    No relevant knowledge-base context was retrieved.
                    If the answer is not available, say that the current knowledge base has no relevant information and request more source material.
                    Do not invent facts.
                    """;
        }
        return """
                You are an AI Harness Compiler knowledge assistant.
                Answer only from the provided context.
                If the context is insufficient, say that the current knowledge base has no relevant information and request more source material.
                Keep the answer concise and include actionable engineering guidance when useful.

                Context:
                """ + context;
    }

    private String replyWithSources(String reply, List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return reply;
        }
        StringBuilder builder = new StringBuilder(reply);
        builder.append("\n\nSources: ");
        for (int i = 0; i < sources.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(sources.get(i));
        }
        return builder.toString();
    }

    private Map<String, Object> buildPayload(IngestDocument document, String docId, int index, String title, String content) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("docId", docId);
        payload.put("chunkIndex", index);
        payload.put("content", content);
        payload.put("ingestedAt", LocalDateTime.now().toString());
        payload.put("title", title);
        if (document.metadata() != null && !document.metadata().isEmpty()) {
            payload.putAll(document.metadata());
        }
        return payload;
    }

    private record Chunk(String id, String text, Map<String, Object> payload) {
    }

    public record IngestDocument(String id, String title, String content, Map<String, Object> metadata) {
    }

    public record IngestResult(int documents, int chunks, List<String> ids) {
    }

    private record ContextPack(String context, List<String> sources) {
    }
}
