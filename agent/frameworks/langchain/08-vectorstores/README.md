# Vector Stores in LangChain (Java)

## 🎯 Overview
Vector Stores in LangChain enable semantic search and retrieval-augmented generation (RAG) by storing document embeddings and providing similarity search capabilities. They form the foundation for building intelligent question-answering systems and knowledge retrieval applications.

## 🧠 Core Vector Store Concepts

### What are Vector Stores?
Vector stores are specialized databases that:
- **Store Embeddings**: Convert text into high-dimensional vectors
- **Enable Similarity Search**: Find semantically similar content
- **Support RAG**: Retrieve relevant context for LLM responses
- **Scale Efficiently**: Handle large document collections
- **Provide Metadata Filtering**: Filter by document properties

### Vector Store Types
1. **In-Memory Stores**: FAISS, simple similarity stores
2. **Database-Backed**: PostgreSQL with pgvector, SQLite
3. **Cloud Services**: Pinecone, Weaviate, Qdrant
4. **Search Engines**: Elasticsearch, OpenSearch
5. **Graph Databases**: Neo4j with vector support

## 🏗️ Vector Store Architecture Patterns

### 1. **RAG Pipeline**
```
Documents → Chunking → Embedding → Vector Store → Retrieval → LLM → Response
```

### 2. **Hybrid Search**
```
Query → Vector Search + Keyword Search → Ranking → Results
```

### 3. **Multi-Modal Storage**
```
Text + Images + Metadata → Multi-Vector Store → Unified Search
```

### 4. **Hierarchical Retrieval**
```
Query → High-Level Search → Detailed Search → Context Assembly
```

## 💻 Java Vector Store Implementation

### Base Vector Store Framework
```java
package com.example.agent.vectorstores;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all vector stores
 */
public interface VectorStore {
    
    /**
     * Add documents with their embeddings
     */
    void addDocuments(List<Document> documents) throws VectorStoreException;
    
    /**
     * Add a single document
     */
    default void addDocument(Document document) throws VectorStoreException {
        addDocuments(List.of(document));
    }
    
    /**
     * Search for similar documents
     */
    List<DocumentMatch> similaritySearch(String query, int topK) throws VectorStoreException;
    
    /**
     * Search with custom embedding
     */
    List<DocumentMatch> similaritySearchByVector(float[] queryVector, int topK) 
        throws VectorStoreException;
    
    /**
     * Search with metadata filtering
     */
    List<DocumentMatch> similaritySearchWithFilter(String query, int topK, 
                                                   Map<String, Object> filter) 
        throws VectorStoreException;
    
    /**
     * Delete documents by IDs
     */
    void deleteDocuments(List<String> documentIds) throws VectorStoreException;
    
    /**
     * Get document by ID
     */
    Optional<Document> getDocument(String documentId) throws VectorStoreException;
    
    /**
     * Update document metadata
     */
    void updateMetadata(String documentId, Map<String, Object> metadata) 
        throws VectorStoreException;
    
    /**
     * Get store statistics
     */
    VectorStoreStats getStats() throws VectorStoreException;
    
    /**
     * Close store and cleanup resources
     */
    void close() throws VectorStoreException;
}

/**
 * Document representation with content and metadata
 */
public class Document {
    private final String id;
    private final String content;
    private final Map<String, Object> metadata;
    private final float[] embedding;
    
    public Document(String id, String content, Map<String, Object> metadata) {
        this(id, content, metadata, null);
    }
    
    public Document(String id, String content, Map<String, Object> metadata, float[] embedding) {
        this.id = id;
        this.content = content;
        this.metadata = new HashMap<>(metadata != null ? metadata : Map.of());
        this.embedding = embedding;
    }
    
    // Builder pattern
    public static DocumentBuilder builder() {
        return new DocumentBuilder();
    }
    
    // Getters
    public String getId() { return id; }
    public String getContent() { return content; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public float[] getEmbedding() { return embedding != null ? embedding.clone() : null; }
    public boolean hasEmbedding() { return embedding != null; }
    
    @Override
    public String toString() {
        return String.format("Document{id='%s', content='%s...', metadata=%s}", 
                           id, content.substring(0, Math.min(50, content.length())), metadata);
    }
}

/**
 * Document builder for fluent creation
 */
public class DocumentBuilder {
    private String id;
    private String content;
    private Map<String, Object> metadata = new HashMap<>();
    private float[] embedding;
    
    public DocumentBuilder id(String id) {
        this.id = id;
        return this;
    }
    
    public DocumentBuilder content(String content) {
        this.content = content;
        return this;
    }
    
    public DocumentBuilder metadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    public DocumentBuilder metadata(Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }
    
    public DocumentBuilder embedding(float[] embedding) {
        this.embedding = embedding;
        return this;
    }
    
    public Document build() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return new Document(id, content, metadata, embedding);
    }
}

/**
 * Document search result with similarity score
 */
public class DocumentMatch {
    private final Document document;
    private final double similarityScore;
    
    public DocumentMatch(Document document, double similarityScore) {
        this.document = document;
        this.similarityScore = similarityScore;
    }
    
    public Document getDocument() { return document; }
    public double getSimilarityScore() { return similarityScore; }
    
    @Override
    public String toString() {
        return String.format("DocumentMatch{score=%.3f, document=%s}", 
                           similarityScore, document);
    }
}

/**
 * Vector store statistics
 */
public class VectorStoreStats {
    private final long totalDocuments;
    private final long totalSize;
    private final int dimensions;
    private final Map<String, Object> additionalStats;
    
    public VectorStoreStats(long totalDocuments, long totalSize, int dimensions) {
        this(totalDocuments, totalSize, dimensions, Map.of());
    }
    
    public VectorStoreStats(long totalDocuments, long totalSize, int dimensions,
                           Map<String, Object> additionalStats) {
        this.totalDocuments = totalDocuments;
        this.totalSize = totalSize;
        this.dimensions = dimensions;
        this.additionalStats = new HashMap<>(additionalStats);
    }
    
    public long getTotalDocuments() { return totalDocuments; }
    public long getTotalSize() { return totalSize; }
    public int getDimensions() { return dimensions; }
    public Map<String, Object> getAdditionalStats() { return new HashMap<>(additionalStats); }
}

/**
 * Vector store exception
 */
public class VectorStoreException extends Exception {
    public VectorStoreException(String message) {
        super(message);
    }
    
    public VectorStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Embedding service interface
 */
public interface EmbeddingService {
    
    /**
     * Generate embedding for a single text
     */
    float[] embed(String text) throws EmbeddingException;
    
    /**
     * Generate embeddings for multiple texts
     */
    List<float[]> embedBatch(List<String> texts) throws EmbeddingException;
    
    /**
     * Get embedding dimensions
     */
    int getDimensions();
    
    /**
     * Get model name/identifier
     */
    String getModelName();
}

/**
 * Embedding exception
 */
public class EmbeddingException extends Exception {
    public EmbeddingException(String message) {
        super(message);
    }
    
    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### In-Memory Vector Store Implementation
```java
/**
 * Simple in-memory vector store using cosine similarity
 */
public class InMemoryVectorStore implements VectorStore {
    
    private final EmbeddingService embeddingService;
    private final Map<String, Document> documents;
    private final Map<String, float[]> embeddings;
    private final Object lock = new Object();
    
    public InMemoryVectorStore(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
        this.documents = new ConcurrentHashMap<>();
        this.embeddings = new ConcurrentHashMap<>();
    }
    
    @Override
    public void addDocuments(List<Document> docs) throws VectorStoreException {
        try {
            synchronized (lock) {
                List<Document> needEmbedding = new ArrayList<>();
                List<String> textsToEmbed = new ArrayList<>();
                
                // Separate documents that need embedding
                for (Document doc : docs) {
                    if (!doc.hasEmbedding()) {
                        needEmbedding.add(doc);
                        textsToEmbed.add(doc.getContent());
                    }
                }
                
                // Generate embeddings for documents that need them
                List<float[]> newEmbeddings = textsToEmbed.isEmpty() ? 
                    List.of() : embeddingService.embedBatch(textsToEmbed);
                
                // Store all documents
                int embeddingIndex = 0;
                for (Document doc : docs) {
                    documents.put(doc.getId(), doc);
                    
                    if (doc.hasEmbedding()) {
                        embeddings.put(doc.getId(), doc.getEmbedding());
                    } else if (embeddingIndex < newEmbeddings.size()) {
                        embeddings.put(doc.getId(), newEmbeddings.get(embeddingIndex++));
                    }
                }
            }
        } catch (Exception e) {
            throw new VectorStoreException("Failed to add documents", e);
        }
    }
    
    @Override
    public List<DocumentMatch> similaritySearch(String query, int topK) throws VectorStoreException {
        try {
            float[] queryEmbedding = embeddingService.embed(query);
            return similaritySearchByVector(queryEmbedding, topK);
        } catch (Exception e) {
            throw new VectorStoreException("Similarity search failed", e);
        }
    }
    
    @Override
    public List<DocumentMatch> similaritySearchByVector(float[] queryVector, int topK) 
            throws VectorStoreException {
        
        synchronized (lock) {
            List<DocumentMatch> matches = new ArrayList<>();
            
            for (Map.Entry<String, float[]> entry : embeddings.entrySet()) {
                String docId = entry.getKey();
                float[] docEmbedding = entry.getValue();
                
                double similarity = cosineSimilarity(queryVector, docEmbedding);
                Document doc = documents.get(docId);
                
                if (doc != null) {
                    matches.add(new DocumentMatch(doc, similarity));
                }
            }
            
            // Sort by similarity score (descending) and take top K
            return matches.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .limit(topK)
                .collect(Collectors.toList());
        }
    }
    
    @Override
    public List<DocumentMatch> similaritySearchWithFilter(String query, int topK, 
                                                          Map<String, Object> filter) 
            throws VectorStoreException {
        
        List<DocumentMatch> allMatches = similaritySearch(query, Integer.MAX_VALUE);
        
        return allMatches.stream()
            .filter(match -> matchesFilter(match.getDocument(), filter))
            .limit(topK)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteDocuments(List<String> documentIds) throws VectorStoreException {
        synchronized (lock) {
            for (String id : documentIds) {
                documents.remove(id);
                embeddings.remove(id);
            }
        }
    }
    
    @Override
    public Optional<Document> getDocument(String documentId) throws VectorStoreException {
        return Optional.ofNullable(documents.get(documentId));
    }
    
    @Override
    public void updateMetadata(String documentId, Map<String, Object> metadata) 
            throws VectorStoreException {
        
        synchronized (lock) {
            Document existingDoc = documents.get(documentId);
            if (existingDoc != null) {
                Map<String, Object> newMetadata = new HashMap<>(existingDoc.getMetadata());
                newMetadata.putAll(metadata);
                
                Document updatedDoc = new Document(existingDoc.getId(), existingDoc.getContent(), 
                                                 newMetadata, existingDoc.getEmbedding());
                documents.put(documentId, updatedDoc);
            } else {
                throw new VectorStoreException("Document not found: " + documentId);
            }
        }
    }
    
    @Override
    public VectorStoreStats getStats() throws VectorStoreException {
        synchronized (lock) {
            long totalSize = documents.values().stream()
                .mapToLong(doc -> doc.getContent().length())
                .sum();
            
            int dimensions = embeddings.isEmpty() ? 0 : 
                embeddings.values().iterator().next().length;
            
            Map<String, Object> additionalStats = Map.of(
                "store_type", "in_memory",
                "embedding_model", embeddingService.getModelName()
            );
            
            return new VectorStoreStats(documents.size(), totalSize, dimensions, additionalStats);
        }
    }
    
    @Override
    public void close() throws VectorStoreException {
        synchronized (lock) {
            documents.clear();
            embeddings.clear();
        }
    }
    
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0.0 ? 0.0 : dotProduct / denominator;
    }
    
    private boolean matchesFilter(Document document, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        
        Map<String, Object> docMetadata = document.getMetadata();
        
        for (Map.Entry<String, Object> filterEntry : filter.entrySet()) {
            String key = filterEntry.getKey();
            Object expectedValue = filterEntry.getValue();
            Object actualValue = docMetadata.get(key);
            
            if (!Objects.equals(expectedValue, actualValue)) {
                return false;
            }
        }
        
        return true;
    }
}

/**
 * OpenAI embedding service implementation
 */
@Service
public class OpenAIEmbeddingService implements EmbeddingService {
    
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OpenAIEmbeddingService(String apiKey) {
        this(apiKey, "text-embedding-ada-002");
    }
    
    public OpenAIEmbeddingService(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public float[] embed(String text) throws EmbeddingException {
        List<float[]> embeddings = embedBatch(List.of(text));
        return embeddings.isEmpty() ? null : embeddings.get(0);
    }
    
    @Override
    public List<float[]> embedBatch(List<String> texts) throws EmbeddingException {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "input", texts
            );
            
            String requestJson = objectMapper.writeValueAsString(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/embeddings"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new EmbeddingException("API call failed with status: " + response.statusCode());
            }
            
            return parseEmbeddingResponse(response.body());
            
        } catch (Exception e) {
            throw new EmbeddingException("Failed to generate embeddings", e);
        }
    }
    
    @Override
    public int getDimensions() {
        return "text-embedding-ada-002".equals(model) ? 1536 : 1536; // Default assumption
    }
    
    @Override
    public String getModelName() {
        return model;
    }
    
    private List<float[]> parseEmbeddingResponse(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode dataArray = root.get("data");
        
        List<float[]> embeddings = new ArrayList<>();
        
        for (JsonNode item : dataArray) {
            JsonNode embeddingArray = item.get("embedding");
            float[] embedding = new float[embeddingArray.size()];
            
            for (int i = 0; i < embeddingArray.size(); i++) {
                embedding[i] = (float) embeddingArray.get(i).asDouble();
            }
            
            embeddings.add(embedding);
        }
        
        return embeddings;
    }
}
```

### Database-Backed Vector Store
```java
/**
 * PostgreSQL vector store using pgvector extension
 */
@Service
public class PostgreSQLVectorStore implements VectorStore {
    
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;
    
    public PostgreSQLVectorStore(JdbcTemplate jdbcTemplate, EmbeddingService embeddingService) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingService = embeddingService;
        initializeSchema();
    }
    
    @Override
    public void addDocuments(List<Document> docs) throws VectorStoreException {
        try {
            String sql = """
                INSERT INTO documents (id, content, metadata, embedding) 
                VALUES (?, ?, ?::jsonb, ?::vector)
                ON CONFLICT (id) DO UPDATE SET 
                content = EXCLUDED.content, 
                metadata = EXCLUDED.metadata, 
                embedding = EXCLUDED.embedding,
                updated_at = CURRENT_TIMESTAMP
                """;
            
            List<Object[]> batchArgs = new ArrayList<>();
            List<Document> needEmbedding = new ArrayList<>();
            
            // Prepare documents and identify which need embeddings
            for (Document doc : docs) {
                if (!doc.hasEmbedding()) {
                    needEmbedding.add(doc);
                }
            }
            
            // Generate embeddings if needed
            Map<String, float[]> newEmbeddings = new HashMap<>();
            if (!needEmbedding.isEmpty()) {
                List<String> textsToEmbed = needEmbedding.stream()
                    .map(Document::getContent)
                    .collect(Collectors.toList());
                
                List<float[]> embeddings = embeddingService.embedBatch(textsToEmbed);
                
                for (int i = 0; i < needEmbedding.size(); i++) {
                    newEmbeddings.put(needEmbedding.get(i).getId(), embeddings.get(i));
                }
            }
            
            // Prepare batch insert arguments
            for (Document doc : docs) {
                float[] embedding = doc.hasEmbedding() ? 
                    doc.getEmbedding() : newEmbeddings.get(doc.getId());
                
                String metadataJson = new ObjectMapper().writeValueAsString(doc.getMetadata());
                String embeddingString = Arrays.toString(embedding);
                
                batchArgs.add(new Object[]{
                    doc.getId(), 
                    doc.getContent(), 
                    metadataJson,
                    embeddingString
                });
            }
            
            jdbcTemplate.batchUpdate(sql, batchArgs);
            
        } catch (Exception e) {
            throw new VectorStoreException("Failed to add documents", e);
        }
    }
    
    @Override
    public List<DocumentMatch> similaritySearch(String query, int topK) throws VectorStoreException {
        try {
            float[] queryEmbedding = embeddingService.embed(query);
            return similaritySearchByVector(queryEmbedding, topK);
        } catch (Exception e) {
            throw new VectorStoreException("Similarity search failed", e);
        }
    }
    
    @Override
    public List<DocumentMatch> similaritySearchByVector(float[] queryVector, int topK) 
            throws VectorStoreException {
        try {
            String embeddingString = Arrays.toString(queryVector);
            
            String sql = """
                SELECT id, content, metadata, embedding, 
                       1 - (embedding <=> ?::vector) as similarity_score
                FROM documents
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;
            
            return jdbcTemplate.query(sql, this::mapDocumentMatch, 
                                    embeddingString, embeddingString, topK);
            
        } catch (Exception e) {
            throw new VectorStoreException("Vector similarity search failed", e);
        }
    }
    
    @Override
    public List<DocumentMatch> similaritySearchWithFilter(String query, int topK, 
                                                          Map<String, Object> filter) 
            throws VectorStoreException {
        try {
            float[] queryEmbedding = embeddingService.embed(query);
            String embeddingString = Arrays.toString(queryEmbedding);
            
            StringBuilder sqlBuilder = new StringBuilder("""
                SELECT id, content, metadata, embedding, 
                       1 - (embedding <=> ?::vector) as similarity_score
                FROM documents
                """);
            
            List<Object> args = new ArrayList<>();
            args.add(embeddingString);
            args.add(embeddingString);
            
            if (filter != null && !filter.isEmpty()) {
                sqlBuilder.append(" WHERE ");
                List<String> conditions = new ArrayList<>();
                
                for (Map.Entry<String, Object> entry : filter.entrySet()) {
                    conditions.add("metadata ->> ? = ?");
                    args.add(entry.getKey());
                    args.add(entry.getValue().toString());
                }
                
                sqlBuilder.append(String.join(" AND ", conditions));
            }
            
            sqlBuilder.append(" ORDER BY embedding <=> ?::vector LIMIT ?");
            args.add(embeddingString);
            args.add(topK);
            
            return jdbcTemplate.query(sqlBuilder.toString(), this::mapDocumentMatch, args.toArray());
            
        } catch (Exception e) {
            throw new VectorStoreException("Filtered similarity search failed", e);
        }
    }
    
    @Override
    public void deleteDocuments(List<String> documentIds) throws VectorStoreException {
        try {
            String sql = "DELETE FROM documents WHERE id = ANY(?)";
            jdbcTemplate.update(sql, documentIds.toArray(new String[0]));
        } catch (Exception e) {
            throw new VectorStoreException("Failed to delete documents", e);
        }
    }
    
    @Override
    public Optional<Document> getDocument(String documentId) throws VectorStoreException {
        try {
            String sql = "SELECT id, content, metadata, embedding FROM documents WHERE id = ?";
            
            List<Document> results = jdbcTemplate.query(sql, this::mapDocument, documentId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            
        } catch (Exception e) {
            throw new VectorStoreException("Failed to get document", e);
        }
    }
    
    @Override
    public void updateMetadata(String documentId, Map<String, Object> metadata) 
            throws VectorStoreException {
        try {
            String metadataJson = new ObjectMapper().writeValueAsString(metadata);
            String sql = "UPDATE documents SET metadata = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            
            int updated = jdbcTemplate.update(sql, metadataJson, documentId);
            if (updated == 0) {
                throw new VectorStoreException("Document not found: " + documentId);
            }
        } catch (Exception e) {
            throw new VectorStoreException("Failed to update metadata", e);
        }
    }
    
    @Override
    public VectorStoreStats getStats() throws VectorStoreException {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as total_docs,
                    SUM(LENGTH(content)) as total_size,
                    COALESCE(array_length((SELECT embedding FROM documents LIMIT 1), 1), 0) as dimensions
                FROM documents
                """;
            
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, Object> additionalStats = Map.of(
                    "store_type", "postgresql",
                    "embedding_model", embeddingService.getModelName()
                );
                
                return new VectorStoreStats(
                    rs.getLong("total_docs"),
                    rs.getLong("total_size"),
                    rs.getInt("dimensions"),
                    additionalStats
                );
            });
            
        } catch (Exception e) {
            throw new VectorStoreException("Failed to get stats", e);
        }
    }
    
    @Override
    public void close() throws VectorStoreException {
        // Connection pooling handles cleanup
    }
    
    private void initializeSchema() {
        try {
            // Create extension if not exists
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            
            // Create table if not exists
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS documents (
                    id VARCHAR(255) PRIMARY KEY,
                    content TEXT NOT NULL,
                    metadata JSONB DEFAULT '{}',
                    embedding vector,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
            
            // Create index for similarity search
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS documents_embedding_idx 
                ON documents USING ivfflat (embedding vector_cosine_ops) 
                WITH (lists = 100)
                """);
            
            // Create metadata index
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS documents_metadata_idx 
                ON documents USING gin (metadata)
                """);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }
    
    private DocumentMatch mapDocumentMatch(ResultSet rs, int rowNum) throws SQLException {
        Document doc = mapDocument(rs, rowNum);
        double score = rs.getDouble("similarity_score");
        return new DocumentMatch(doc, score);
    }
    
    private Document mapDocument(ResultSet rs, int rowNum) throws SQLException {
        try {
            String id = rs.getString("id");
            String content = rs.getString("content");
            String metadataJson = rs.getString("metadata");
            
            Map<String, Object> metadata = metadataJson != null ? 
                new ObjectMapper().readValue(metadataJson, Map.class) : Map.of();
            
            // Parse embedding if available
            String embeddingString = rs.getString("embedding");
            float[] embedding = null;
            if (embeddingString != null) {
                // Parse vector string format: [1.0, 2.0, 3.0]
                embeddingString = embeddingString.substring(1, embeddingString.length() - 1);
                String[] parts = embeddingString.split(",");
                embedding = new float[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    embedding[i] = Float.parseFloat(parts[i].trim());
                }
            }
            
            return new Document(id, content, metadata, embedding);
            
        } catch (Exception e) {
            throw new SQLException("Failed to map document", e);
        }
    }
}
```

### RAG (Retrieval-Augmented Generation) Implementation
```java
/**
 * Complete RAG system implementation
 */
@Service
public class RAGService {
    
    private final VectorStore vectorStore;
    private final ChatLanguageModel llm;
    private final DocumentProcessor documentProcessor;
    private final PromptTemplateRegistry templateRegistry;
    
    public RAGService(VectorStore vectorStore, ChatLanguageModel llm,
                     DocumentProcessor documentProcessor, PromptTemplateRegistry templateRegistry) {
        this.vectorStore = vectorStore;
        this.llm = llm;
        this.documentProcessor = documentProcessor;
        this.templateRegistry = templateRegistry;
    }
    
    /**
     * Add documents to the knowledge base
     */
    public void addDocuments(List<String> texts, Map<String, Object> metadata) 
            throws VectorStoreException {
        
        List<Document> documents = new ArrayList<>();
        
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            
            // Process document into chunks if needed
            List<String> chunks = documentProcessor.chunkDocument(text);
            
            for (int j = 0; j < chunks.size(); j++) {
                String chunkId = String.format("doc_%d_chunk_%d", i, j);
                
                Map<String, Object> chunkMetadata = new HashMap<>(metadata);
                chunkMetadata.put("document_index", i);
                chunkMetadata.put("chunk_index", j);
                chunkMetadata.put("chunk_count", chunks.size());
                
                Document doc = Document.builder()
                    .id(chunkId)
                    .content(chunks.get(j))
                    .metadata(chunkMetadata)
                    .build();
                
                documents.add(doc);
            }
        }
        
        vectorStore.addDocuments(documents);
    }
    
    /**
     * Answer a question using RAG
     */
    public RAGResponse answerQuestion(String question) throws VectorStoreException, TemplateException {
        return answerQuestion(question, 5, null);
    }
    
    /**
     * Answer question with custom parameters
     */
    public RAGResponse answerQuestion(String question, int retrievalCount, 
                                    Map<String, Object> filter) 
            throws VectorStoreException, TemplateException {
        
        long startTime = System.currentTimeMillis();
        
        // Step 1: Retrieve relevant context
        List<DocumentMatch> matches = filter != null ?
            vectorStore.similaritySearchWithFilter(question, retrievalCount, filter) :
            vectorStore.similaritySearch(question, retrievalCount);
        
        // Step 2: Prepare context
        String context = prepareContext(matches);
        
        // Step 3: Generate answer
        String answer = generateAnswer(question, context, matches);
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        return new RAGResponse(answer, matches, context, executionTime);
    }
    
    /**
     * Multi-step question answering with follow-up questions
     */
    public RAGResponse answerComplexQuestion(String question, List<String> conversationHistory) 
            throws VectorStoreException, TemplateException {
        
        // Step 1: Analyze question complexity and context
        String enhancedQuestion = enhanceQuestionWithHistory(question, conversationHistory);
        
        // Step 2: Generate sub-questions if needed
        List<String> subQuestions = generateSubQuestions(enhancedQuestion);
        
        // Step 3: Answer each sub-question
        List<RAGResponse> subAnswers = new ArrayList<>();
        for (String subQ : subQuestions) {
            subAnswers.add(answerQuestion(subQ, 3, null));
        }
        
        // Step 4: Synthesize final answer
        String finalAnswer = synthesizeAnswers(question, subAnswers);
        
        // Combine all retrieved documents
        List<DocumentMatch> allMatches = subAnswers.stream()
            .flatMap(resp -> resp.getRetrievedDocuments().stream())
            .distinct()
            .collect(Collectors.toList());
        
        String combinedContext = prepareContext(allMatches);
        
        return new RAGResponse(finalAnswer, allMatches, combinedContext, 0);
    }
    
    private String prepareContext(List<DocumentMatch> matches) {
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < matches.size(); i++) {
            DocumentMatch match = matches.get(i);
            context.append("Source ").append(i + 1)
                   .append(" (relevance: ").append(String.format("%.3f", match.getSimilarityScore()))
                   .append("):\n")
                   .append(match.getDocument().getContent())
                   .append("\n\n");
        }
        
        return context.toString().trim();
    }
    
    private String generateAnswer(String question, String context, List<DocumentMatch> matches) 
            throws TemplateException {
        
        // Select appropriate template based on context availability
        String templateName = context.isEmpty() ? "no_context_answer" : "rag_answer";
        
        Map<String, Object> variables = Map.of(
            "question", question,
            "context", context,
            "source_count", matches.size()
        );
        
        try {
            String prompt = templateRegistry.format(templateName, variables);
            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
            return response.text();
        } catch (Exception e) {
            return "I encountered an error while processing your question: " + e.getMessage();
        }
    }
    
    private String enhanceQuestionWithHistory(String question, List<String> history) throws TemplateException {
        if (history == null || history.isEmpty()) {
            return question;
        }
        
        String historyContext = String.join("\n", history.subList(
            Math.max(0, history.size() - 5), history.size()));
        
        Map<String, Object> variables = Map.of(
            "question", question,
            "history", historyContext
        );
        
        try {
            String prompt = templateRegistry.format("enhance_question", variables);
            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
            return response.text();
        } catch (Exception e) {
            return question; // Fallback to original question
        }
    }
    
    private List<String> generateSubQuestions(String complexQuestion) throws TemplateException {
        Map<String, Object> variables = Map.of("question", complexQuestion);
        
        try {
            String prompt = templateRegistry.format("generate_subquestions", variables);
            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
            
            // Parse sub-questions from response
            return Arrays.stream(response.text().split("\n"))
                .map(String::trim)
                .filter(line -> line.matches("\\d+\\..*"))
                .map(line -> line.replaceFirst("\\d+\\.", "").trim())
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of(complexQuestion); // Fallback to original question
        }
    }
    
    private String synthesizeAnswers(String originalQuestion, List<RAGResponse> subAnswers) 
            throws TemplateException {
        
        StringBuilder subAnswersText = new StringBuilder();
        for (int i = 0; i < subAnswers.size(); i++) {
            subAnswersText.append("Sub-answer ").append(i + 1).append(": ")
                         .append(subAnswers.get(i).getAnswer()).append("\n");
        }
        
        Map<String, Object> variables = Map.of(
            "original_question", originalQuestion,
            "sub_answers", subAnswersText.toString()
        );
        
        try {
            String prompt = templateRegistry.format("synthesize_answers", variables);
            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
            return response.text();
        } catch (Exception e) {
            // Fallback: combine sub-answers
            return subAnswers.stream()
                .map(RAGResponse::getAnswer)
                .collect(Collectors.joining(" "));
        }
    }
}

/**
 * RAG response container
 */
public class RAGResponse {
    private final String answer;
    private final List<DocumentMatch> retrievedDocuments;
    private final String context;
    private final long executionTimeMs;
    
    public RAGResponse(String answer, List<DocumentMatch> retrievedDocuments, 
                      String context, long executionTimeMs) {
        this.answer = answer;
        this.retrievedDocuments = new ArrayList<>(retrievedDocuments);
        this.context = context;
        this.executionTimeMs = executionTimeMs;
    }
    
    public String getAnswer() { return answer; }
    public List<DocumentMatch> getRetrievedDocuments() { return new ArrayList<>(retrievedDocuments); }
    public String getContext() { return context; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    
    public int getSourceCount() { return retrievedDocuments.size(); }
    
    public List<String> getSourceContents() {
        return retrievedDocuments.stream()
            .map(match -> match.getDocument().getContent())
            .collect(Collectors.toList());
    }
}

/**
 * Document processing utilities
 */
@Component
public class DocumentProcessor {
    
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP = 200;
    
    /**
     * Split document into chunks for better retrieval
     */
    public List<String> chunkDocument(String text) {
        return chunkDocument(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }
    
    public List<String> chunkDocument(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        
        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }
        
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            
            // Try to break at sentence boundary
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                
                if (breakPoint > start + chunkSize / 2) {
                    end = breakPoint + 1;
                }
            }
            
            chunks.add(text.substring(start, end).trim());
            start = Math.max(start + chunkSize - overlap, end);
        }
        
        return chunks;
    }
    
    /**
     * Clean and preprocess text
     */
    public String preprocessText(String text) {
        return text
            .replaceAll("\\s+", " ")  // Normalize whitespace
            .replaceAll("\\p{Cntrl}", " ")  // Remove control characters
            .trim();
    }
    
    /**
     * Extract metadata from text
     */
    public Map<String, Object> extractMetadata(String text) {
        Map<String, Object> metadata = new HashMap<>();
        
        metadata.put("length", text.length());
        metadata.put("word_count", text.split("\\s+").length);
        metadata.put("processed_at", System.currentTimeMillis());
        
        // Simple heuristics for content type
        if (text.contains("```") || text.contains("function") || text.contains("class")) {
            metadata.put("content_type", "code");
        } else if (text.contains("http://") || text.contains("https://")) {
            metadata.put("content_type", "web");
        } else {
            metadata.put("content_type", "text");
        }
        
        return metadata;
    }
}
```

## 🚀 Best Practices

1. **Vector Store Selection**
   - Use in-memory stores for development and small datasets
   - Use database-backed stores for production and persistence
   - Use cloud services for scale and managed infrastructure
   - Consider hybrid approaches for complex requirements

2. **Document Chunking**
   - Choose appropriate chunk sizes for your use case
   - Maintain overlap between chunks for context
   - Consider semantic chunking strategies
   - Test different chunking strategies for optimal retrieval

3. **Embedding Strategy**
   - Use consistent embedding models throughout
   - Consider fine-tuned embeddings for domain-specific content
   - Batch embedding generation for efficiency
   - Cache embeddings to avoid recomputation

4. **Retrieval Optimization**
   - Use metadata filtering to improve relevance
   - Implement hybrid search (vector + keyword)
   - Tune similarity thresholds for your use case
   - Monitor and optimize retrieval performance

5. **RAG Implementation**
   - Design effective prompt templates
   - Handle cases where no relevant context is found
   - Implement answer quality assessment
   - Provide source attribution and transparency

## 🔗 Integration with Other Components

Vector Stores integrate with:
- **Agents**: Agents use vector stores for knowledge retrieval
- **Chains**: RAG chains combine retrieval and generation
- **Memory**: Vector stores can serve as long-term memory
- **Tools**: Vector search can be exposed as agent tools

---

*This completes the LangChain concepts. Next: LangGraph concepts starting with [State Management](../../langgraph/state/).*
