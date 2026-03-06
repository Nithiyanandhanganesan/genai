/**
 * Vector Stores Examples - Complete Implementation
 * Demonstrates vector storage, similarity search, and RAG systems
 */
package com.example.agent.langchain.vectorstores;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive vector stores and RAG demonstration
 */
public class VectorStoresExampleSuite {

    private static ChatLanguageModel llm;
    private static EmbeddingService embeddingService;

    public static void main(String[] args) {
        System.out.println("=== Vector Stores & RAG Examples ===");

        // Initialize LLM
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("Please set OPENAI_API_KEY environment variable");
            return;
        }

        llm = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-3.5-turbo")
            .temperature(0.7)
            .build();

        // Initialize embedding service (mock for demo)
        embeddingService = new MockEmbeddingService();

        try {
            // Run all vector store examples
            runBasicVectorStoreExample();
            runDocumentIngestionExample();
            runSimilaritySearchExample();
            runRAGSystemExample();
            runAdvancedRAGExample();
            runVectorStoreComparisonExample();
            runProductionPatternsExample();

        } catch (Exception e) {
            System.err.println("Error running vector store examples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Basic vector store operations
     */
    private static void runBasicVectorStoreExample() {
        System.out.println("\n1. Basic Vector Store Example:");

        // Create in-memory vector store
        InMemoryVectorStore vectorStore = new InMemoryVectorStore(embeddingService);

        // Add documents to vector store
        List<Document> documents = Arrays.asList(
            Document.builder()
                .id("doc1")
                .content("Java is a programming language developed by Sun Microsystems in 1995.")
                .metadata("category", "programming")
                .metadata("language", "java")
                .build(),

            Document.builder()
                .id("doc2")
                .content("Python is a high-level programming language known for its simplicity.")
                .metadata("category", "programming")
                .metadata("language", "python")
                .build(),

            Document.builder()
                .id("doc3")
                .content("Machine learning is a subset of artificial intelligence that uses statistical techniques.")
                .metadata("category", "ai")
                .metadata("field", "machine_learning")
                .build(),

            Document.builder()
                .id("doc4")
                .content("React is a JavaScript library for building user interfaces, particularly web applications.")
                .metadata("category", "web_development")
                .metadata("framework", "react")
                .build()
        );

        try {
            // Add documents to vector store
            vectorStore.addDocuments(documents);

            System.out.println("Added " + documents.size() + " documents to vector store");

            // Get store statistics
            VectorStoreStats stats = vectorStore.getStats();
            System.out.println("Vector store stats: " + stats.getTotalDocuments() + " documents, " +
                             stats.getDimensions() + " dimensions");

            // Test document retrieval
            Optional<Document> retrievedDoc = vectorStore.getDocument("doc1");
            if (retrievedDoc.isPresent()) {
                System.out.println("Retrieved document: " + retrievedDoc.get().getContent());
            }

        } catch (Exception e) {
            System.err.println("Basic vector store error: " + e.getMessage());
        }
    }

    /**
     * Document ingestion and preprocessing
     */
    private static void runDocumentIngestionExample() {
        System.out.println("\n2. Document Ingestion Example:");

        InMemoryVectorStore vectorStore = new InMemoryVectorStore(embeddingService);
        DocumentProcessor processor = new DocumentProcessor();

        // Large document that needs chunking
        String largeDocument = """
            Artificial Intelligence (AI) is a broad field of computer science focused on building smart machines 
            capable of performing tasks that typically require human intelligence. AI systems can be designed to 
            learn from data, recognize patterns, make decisions, and solve problems.
            
            Machine Learning is a subset of AI that focuses on the development of algorithms that can learn and 
            make predictions or decisions based on data. Rather than being explicitly programmed to perform a 
            task, machine learning systems improve their performance through experience.
            
            Deep Learning is a specialized subset of machine learning that uses neural networks with multiple 
            layers (hence 'deep') to model and understand complex patterns in data. It has been particularly 
            successful in areas like image recognition, natural language processing, and game playing.
            
            Natural Language Processing (NLP) is another important area of AI that focuses on the interaction 
            between computers and human language. NLP systems can understand, interpret, and generate human 
            language in a valuable way.
            """;

        try {
            // Process and chunk the document
            List<String> chunks = processor.chunkDocument(largeDocument, 200, 50);
            System.out.println("Document split into " + chunks.size() + " chunks");

            // Create documents from chunks
            List<Document> chunkDocs = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                Document doc = Document.builder()
                    .id("ai_doc_chunk_" + i)
                    .content(chunks.get(i))
                    .metadata("source", "ai_overview")
                    .metadata("chunk_index", i)
                    .metadata("total_chunks", chunks.size())
                    .build();
                chunkDocs.add(doc);
            }

            // Add chunked documents to vector store
            vectorStore.addDocuments(chunkDocs);

            System.out.println("Ingested document with " + chunkDocs.size() + " chunks");

            // Test chunking quality
            for (int i = 0; i < Math.min(2, chunks.size()); i++) {
                System.out.println("Chunk " + i + ": " +
                    chunks.get(i).substring(0, Math.min(100, chunks.get(i).length())) + "...");
            }

        } catch (Exception e) {
            System.err.println("Document ingestion error: " + e.getMessage());
        }
    }

    /**
     * Similarity search capabilities
     */
    private static void runSimilaritySearchExample() {
        System.out.println("\n3. Similarity Search Example:");

        InMemoryVectorStore vectorStore = new InMemoryVectorStore(embeddingService);

        // Add technical documentation
        List<Document> techDocs = Arrays.asList(
            Document.builder()
                .id("spring_boot")
                .content("Spring Boot makes it easy to create stand-alone, production-grade Spring-based applications.")
                .metadata("framework", "spring")
                .metadata("type", "documentation")
                .build(),

            Document.builder()
                .id("docker_intro")
                .content("Docker containers wrap software in a complete filesystem with everything needed to run.")
                .metadata("technology", "docker")
                .metadata("type", "documentation")
                .build(),

            Document.builder()
                .id("kubernetes_basics")
                .content("Kubernetes orchestrates containerized applications across clusters of machines.")
                .metadata("technology", "kubernetes")
                .metadata("type", "documentation")
                .build(),

            Document.builder()
                .id("microservices")
                .content("Microservices architecture structures applications as collections of loosely coupled services.")
                .metadata("pattern", "architecture")
                .metadata("type", "documentation")
                .build()
        );

        try {
            vectorStore.addDocuments(techDocs);

            // Test different types of similarity searches
            String[] queries = {
                "How to build applications with Spring?",
                "Container deployment and management",
                "Breaking down monolithic applications",
                "Development frameworks for Java"
            };

            for (String query : queries) {
                System.out.println("\nQuery: " + query);

                // Standard similarity search
                List<DocumentMatch> matches = vectorStore.similaritySearch(query, 2);

                System.out.println("Top matches:");
                for (int i = 0; i < matches.size(); i++) {
                    DocumentMatch match = matches.get(i);
                    System.out.printf("  %d. [%.3f] %s (ID: %s)\n",
                        i + 1, match.getSimilarityScore(),
                        match.getDocument().getContent(), match.getDocument().getId());
                }
            }

            // Test filtered search
            System.out.println("\nFiltered search for 'container' with technology filter:");
            List<DocumentMatch> filteredMatches = vectorStore.similaritySearchWithFilter(
                "container deployment", 3, Map.of("technology", "docker"));

            filteredMatches.forEach(match ->
                System.out.printf("  [%.3f] %s\n", match.getSimilarityScore(), match.getDocument().getContent()));

        } catch (Exception e) {
            System.err.println("Similarity search error: " + e.getMessage());
        }
    }

    /**
     * Basic RAG system implementation
     */
    private static void runRAGSystemExample() {
        System.out.println("\n4. RAG System Example:");

        // Create RAG system
        InMemoryVectorStore vectorStore = new InMemoryVectorStore(embeddingService);
        DocumentProcessor processor = new DocumentProcessor();
        RAGService ragService = new RAGService(vectorStore, llm, processor,
            createBasicTemplateRegistry());

        // Add knowledge base documents
        List<String> knowledgeBase = Arrays.asList(
            "Spring Framework is a comprehensive programming and configuration model for modern Java-based enterprise applications.",
            "Spring Boot simplifies the development of new Spring applications through auto-configuration and starter dependencies.",
            "Spring Security provides comprehensive security services for Java applications, including authentication and authorization.",
            "Spring Data provides a consistent programming model for data access while retaining special traits of underlying data stores.",
            "Microservices with Spring Boot allow you to build small, independent services that communicate over well-defined APIs."
        );

        try {
            // Ingest knowledge base
            ragService.addDocuments(knowledgeBase, Map.of("source", "spring_documentation"));

            System.out.println("Added " + knowledgeBase.size() + " documents to knowledge base");

            // Test RAG queries
            String[] ragQueries = {
                "What is Spring Framework used for?",
                "How does Spring Boot help with development?",
                "What security features are available in Spring?",
                "How do you build microservices with Spring?"
            };

            for (String query : ragQueries) {
                System.out.println("\nRAG Query: " + query);

                RAGResponse response = ragService.answerQuestion(query);

                if (response != null) {
                    System.out.println("Answer: " + response.getAnswer());
                    System.out.println("Sources used: " + response.getSourceCount());
                    System.out.println("Execution time: " + response.getExecutionTimeMs() + "ms");

                    // Show retrieved context
                    System.out.println("Retrieved context:");
                    for (int i = 0; i < Math.min(2, response.getRetrievedDocuments().size()); i++) {
                        DocumentMatch match = response.getRetrievedDocuments().get(i);
                        System.out.printf("  Source %d [%.3f]: %s\n",
                            i + 1, match.getSimilarityScore(),
                            match.getDocument().getContent().substring(0,
                                Math.min(80, match.getDocument().getContent().length())) + "...");
                    }
                } else {
                    System.out.println("No answer generated");
                }
            }

        } catch (Exception e) {
            System.err.println("RAG system error: " + e.getMessage());
        }
    }

    /**
     * Advanced RAG with multi-step reasoning
     */
    private static void runAdvancedRAGExample() {
        System.out.println("\n5. Advanced RAG Example:");

        InMemoryVectorStore vectorStore = new InMemoryVectorStore(embeddingService);
        RAGService ragService = new RAGService(vectorStore, llm, new DocumentProcessor(),
            createAdvancedTemplateRegistry());

        // Add comprehensive technical knowledge
        List<String> advancedKnowledge = Arrays.asList(
            "Cloud-native applications are designed specifically for cloud computing environments, emphasizing scalability and resilience.",
            "DevOps practices combine software development and IT operations to shorten development cycles and provide continuous delivery.",
            "Infrastructure as Code (IaC) manages and provisions computing infrastructure through machine-readable definition files.",
            "Continuous Integration/Continuous Deployment (CI/CD) automates the integration and deployment of code changes.",
            "Site Reliability Engineering (SRE) applies software engineering approaches to infrastructure and operations problems.",
            "Observability in distributed systems includes logging, metrics, and tracing to understand system behavior.",
            "Service mesh provides infrastructure layer for service-to-service communication in microservices architectures."
        );

        try {
            ragService.addDocuments(advancedKnowledge, Map.of("domain", "cloud_native", "level", "advanced"));

            // Complex multi-step question
            String complexQuery = "How do cloud-native applications benefit from DevOps practices, " +
                                "and what role does observability play in managing them?";

            System.out.println("Complex Query: " + complexQuery);

            // Use conversation history for context
            List<String> conversationHistory = Arrays.asList(
                "I'm interested in modern software development practices",
                "We're moving our applications to the cloud"
            );

            RAGResponse complexResponse = ragService.answerComplexQuestion(complexQuery, conversationHistory);

            if (complexResponse != null) {
                System.out.println("\nComplex Answer:");
                System.out.println(complexResponse.getAnswer());

                System.out.println("\nSources integrated:");
                complexResponse.getRetrievedDocuments().forEach(match ->
                    System.out.printf("  [%.3f] %s\n", match.getSimilarityScore(),
                        match.getDocument().getContent().substring(0, 60) + "..."));

                System.out.println("\nTotal sources: " + complexResponse.getSourceCount());
            }

        } catch (Exception e) {
            System.err.println("Advanced RAG error: " + e.getMessage());
        }
    }

    /**
     * Vector store performance comparison
     */
    private static void runVectorStoreComparisonExample() {
        System.out.println("\n6. Vector Store Performance Comparison:");

        // Compare different vector store implementations
        InMemoryVectorStore inMemoryStore = new InMemoryVectorStore(embeddingService);
        MockDatabaseVectorStore dbStore = new MockDatabaseVectorStore(embeddingService);

        // Create test documents
        List<Document> testDocs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            testDocs.add(Document.builder()
                .id("test_doc_" + i)
                .content("This is test document number " + i + " with some varied content about topic " + (i % 10))
                .metadata("index", i)
                .metadata("category", "test")
                .build());
        }

        try {
            // Test ingestion performance
            System.out.println("Testing ingestion performance:");

            long startTime = System.currentTimeMillis();
            inMemoryStore.addDocuments(testDocs);
            long inMemoryTime = System.currentTimeMillis() - startTime;
            System.out.println("In-memory store: " + inMemoryTime + "ms for " + testDocs.size() + " documents");

            startTime = System.currentTimeMillis();
            dbStore.addDocuments(testDocs);
            long dbTime = System.currentTimeMillis() - startTime;
            System.out.println("Database store: " + dbTime + "ms for " + testDocs.size() + " documents");

            // Test search performance
            System.out.println("\nTesting search performance:");
            String testQuery = "document about topic 5";

            startTime = System.currentTimeMillis();
            List<DocumentMatch> inMemoryResults = inMemoryStore.similaritySearch(testQuery, 5);
            long inMemorySearchTime = System.currentTimeMillis() - startTime;
            System.out.println("In-memory search: " + inMemorySearchTime + "ms, " + inMemoryResults.size() + " results");

            startTime = System.currentTimeMillis();
            List<DocumentMatch> dbResults = dbStore.similaritySearch(testQuery, 5);
            long dbSearchTime = System.currentTimeMillis() - startTime;
            System.out.println("Database search: " + dbSearchTime + "ms, " + dbResults.size() + " results");

            // Compare memory usage (simplified)
            VectorStoreStats inMemoryStats = inMemoryStore.getStats();
            VectorStoreStats dbStats = dbStore.getStats();

            System.out.println("\nStorage comparison:");
            System.out.println("In-memory: " + inMemoryStats.getTotalDocuments() + " docs, " +
                             inMemoryStats.getTotalSize() + " bytes");
            System.out.println("Database: " + dbStats.getTotalDocuments() + " docs, " +
                             dbStats.getTotalSize() + " bytes");

        } catch (Exception e) {
            System.err.println("Performance comparison error: " + e.getMessage());
        }
    }

    /**
     * Production-ready patterns and best practices
     */
    private static void runProductionPatternsExample() {
        System.out.println("\n7. Production Patterns Example:");

        // Production vector store with monitoring and error handling
        ProductionVectorStore prodStore = new ProductionVectorStore(embeddingService);

        // Batch processing example
        System.out.println("Batch processing large document set:");

        List<String> largeBatch = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeBatch.add("Production document " + i + " containing important business information " +
                          "about process " + (i % 100) + " and category " + (i % 20));
        }

        try {
            // Process in batches for better performance
            Map<String, Object> batchMetadata = Map.of(
                "batch_id", "prod_batch_001",
                "source", "business_docs",
                "processing_date", new Date().toString()
            );

            long startTime = System.currentTimeMillis();
            prodStore.batchAddDocuments(largeBatch, batchMetadata, 100); // Batch size 100
            long batchTime = System.currentTimeMillis() - startTime;

            System.out.println("Processed " + largeBatch.size() + " documents in " + batchTime + "ms");
            System.out.println("Average: " + (batchTime / largeBatch.size()) + "ms per document");

            // Test production search with monitoring
            String prodQuery = "business information about process 50";
            ProductionSearchResult result = prodStore.productionSearch(prodQuery, 5);

            System.out.println("\nProduction search results:");
            System.out.println("Query: " + prodQuery);
            System.out.println("Results: " + result.getMatches().size());
            System.out.println("Search time: " + result.getSearchTimeMs() + "ms");
            System.out.println("Cache hit: " + result.isCacheHit());

            // Show monitoring metrics
            System.out.println("\nMonitoring metrics:");
            Map<String, Object> metrics = prodStore.getMetrics();
            metrics.forEach((key, value) -> System.out.println("  " + key + ": " + value));

            // Demonstrate error handling and recovery
            System.out.println("\nTesting error handling:");
            try {
                prodStore.simulateError();
            } catch (Exception e) {
                System.out.println("Gracefully handled error: " + e.getMessage());
            }

            // Show health check
            Map<String, Object> health = prodStore.healthCheck();
            System.out.println("Health status: " + health);

        } catch (Exception e) {
            System.err.println("Production patterns error: " + e.getMessage());
        }
    }

    /**
     * Helper methods
     */
    private static PromptTemplateRegistry createBasicTemplateRegistry() {
        PromptTemplateRegistry registry = new PromptTemplateRegistry();

        // Add basic RAG template
        registry.register("rag_answer",
            new StringPromptTemplate(
                "Based on the following context:\n{context}\n\nQuestion: {question}\n\nAnswer:",
                "Basic RAG template"
            ), "rag");

        return registry;
    }

    private static PromptTemplateRegistry createAdvancedTemplateRegistry() {
        PromptTemplateRegistry registry = createBasicTemplateRegistry();

        // Add advanced templates
        registry.register("complex_analysis",
            new StringPromptTemplate(
                "Analyze this complex question and break it down into components:\n{question}\n\n" +
                "Consider the context:\n{context}\n\n" +
                "Provide a comprehensive answer that addresses all aspects:",
                "Complex analysis template"
            ), "advanced");

        registry.register("multi_source_synthesis",
            new StringPromptTemplate(
                "Synthesize information from multiple sources to answer:\n{question}\n\n" +
                "Sources:\n{sources}\n\n" +
                "Provide a well-reasoned answer that integrates the information:",
                "Multi-source synthesis template"
            ), "advanced");

        return registry;
    }
}

/**
 * Mock production vector store with monitoring
 */
class ProductionVectorStore {
    private final InMemoryVectorStore underlyingStore;
    private final Map<String, Object> metrics = new ConcurrentHashMap<>();
    private final Map<String, ProductionSearchResult> searchCache = new ConcurrentHashMap<>();

    public ProductionVectorStore(EmbeddingService embeddingService) {
        this.underlyingStore = new InMemoryVectorStore(embeddingService);
        initializeMetrics();
    }

    private void initializeMetrics() {
        metrics.put("documents_processed", 0L);
        metrics.put("searches_performed", 0L);
        metrics.put("average_search_time_ms", 0.0);
        metrics.put("cache_hits", 0L);
        metrics.put("errors_encountered", 0L);
        metrics.put("uptime_start", System.currentTimeMillis());
    }

    public void batchAddDocuments(List<String> documents, Map<String, Object> metadata, int batchSize)
            throws VectorStoreException {

        int processed = 0;
        List<Document> batch = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {
            Document doc = Document.builder()
                .id("prod_doc_" + processed)
                .content(documents.get(i))
                .metadata(metadata)
                .build();
            batch.add(doc);
            processed++;

            if (batch.size() >= batchSize || i == documents.size() - 1) {
                underlyingStore.addDocuments(batch);
                batch.clear();

                // Update metrics
                metrics.put("documents_processed", (Long) metrics.get("documents_processed") + batch.size());

                // Simulate progress reporting
                if (processed % 500 == 0 || processed == documents.size()) {
                    System.out.println("Processed " + processed + "/" + documents.size() + " documents");
                }
            }
        }
    }

    public ProductionSearchResult productionSearch(String query, int topK) throws VectorStoreException {
        long startTime = System.currentTimeMillis();
        String cacheKey = query + "_" + topK;

        // Check cache first
        ProductionSearchResult cached = searchCache.get(cacheKey);
        if (cached != null && System.currentTimeMillis() - cached.getTimestamp() < 60000) { // 1 minute cache
            metrics.put("cache_hits", (Long) metrics.get("cache_hits") + 1);
            return cached.withCacheHit(true);
        }

        try {
            List<DocumentMatch> matches = underlyingStore.similaritySearch(query, topK);
            long searchTime = System.currentTimeMillis() - startTime;

            ProductionSearchResult result = new ProductionSearchResult(matches, searchTime, false);
            searchCache.put(cacheKey, result);

            // Update metrics
            updateSearchMetrics(searchTime);

            return result;

        } catch (Exception e) {
            metrics.put("errors_encountered", (Long) metrics.get("errors_encountered") + 1);
            throw e;
        }
    }

    private void updateSearchMetrics(long searchTime) {
        long totalSearches = (Long) metrics.get("searches_performed") + 1;
        double currentAvg = (Double) metrics.get("average_search_time_ms");
        double newAvg = (currentAvg * (totalSearches - 1) + searchTime) / totalSearches;

        metrics.put("searches_performed", totalSearches);
        metrics.put("average_search_time_ms", newAvg);
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> currentMetrics = new HashMap<>(metrics);
        currentMetrics.put("uptime_ms", System.currentTimeMillis() - (Long) metrics.get("uptime_start"));
        return currentMetrics;
    }

    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        try {
            VectorStoreStats stats = underlyingStore.getStats();
            health.put("status", "healthy");
            health.put("documents", stats.getTotalDocuments());
            health.put("total_size", stats.getTotalSize());
            health.put("dimensions", stats.getDimensions());
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
        }

        return health;
    }

    public void simulateError() throws RuntimeException {
        metrics.put("errors_encountered", (Long) metrics.get("errors_encountered") + 1);
        throw new RuntimeException("Simulated production error - connection timeout");
    }
}

/**
 * Production search result with metadata
 */
class ProductionSearchResult {
    private final List<DocumentMatch> matches;
    private final long searchTimeMs;
    private final long timestamp;
    private final boolean cacheHit;

    public ProductionSearchResult(List<DocumentMatch> matches, long searchTimeMs, boolean cacheHit) {
        this.matches = new ArrayList<>(matches);
        this.searchTimeMs = searchTimeMs;
        this.timestamp = System.currentTimeMillis();
        this.cacheHit = cacheHit;
    }

    public ProductionSearchResult withCacheHit(boolean cacheHit) {
        return new ProductionSearchResult(matches, searchTimeMs, cacheHit);
    }

    public List<DocumentMatch> getMatches() { return new ArrayList<>(matches); }
    public long getSearchTimeMs() { return searchTimeMs; }
    public long getTimestamp() { return timestamp; }
    public boolean isCacheHit() { return cacheHit; }
}

/**
 * Mock database vector store for comparison
 */
class MockDatabaseVectorStore implements VectorStore {
    private final EmbeddingService embeddingService;
    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private final Map<String, float[]> embeddings = new ConcurrentHashMap<>();

    public MockDatabaseVectorStore(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @Override
    public void addDocuments(List<Document> docs) throws VectorStoreException {
        for (Document doc : docs) {
            // Simulate database latency
            try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            documents.put(doc.getId(), doc);
            if (!doc.hasEmbedding()) {
                float[] embedding = embeddingService.embed(doc.getContent());
                embeddings.put(doc.getId(), embedding);
            } else {
                embeddings.put(doc.getId(), doc.getEmbedding());
            }
        }
    }

    @Override
    public List<DocumentMatch> similaritySearch(String query, int topK) throws VectorStoreException {
        try {
            // Simulate database query latency
            Thread.sleep(5);

            float[] queryEmbedding = embeddingService.embed(query);
            return similaritySearchByVector(queryEmbedding, topK);
        } catch (Exception e) {
            throw new VectorStoreException("Database search failed", e);
        }
    }

    @Override
    public List<DocumentMatch> similaritySearchByVector(float[] queryVector, int topK) throws VectorStoreException {
        // Simulate database vector search (same logic as in-memory but with latency)
        try { Thread.sleep(3); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

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

        return matches.stream()
            .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
            .limit(topK)
            .collect(Collectors.toList());
    }

    @Override
    public List<DocumentMatch> similaritySearchWithFilter(String query, int topK, Map<String, Object> filter)
            throws VectorStoreException {
        List<DocumentMatch> allMatches = similaritySearch(query, Integer.MAX_VALUE);
        return allMatches.stream()
            .filter(match -> matchesFilter(match.getDocument(), filter))
            .limit(topK)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteDocuments(List<String> documentIds) throws VectorStoreException {
        documentIds.forEach(id -> {
            documents.remove(id);
            embeddings.remove(id);
        });
    }

    @Override
    public Optional<Document> getDocument(String documentId) throws VectorStoreException {
        return Optional.ofNullable(documents.get(documentId));
    }

    @Override
    public void updateMetadata(String documentId, Map<String, Object> metadata) throws VectorStoreException {
        Document doc = documents.get(documentId);
        if (doc != null) {
            Map<String, Object> newMetadata = new HashMap<>(doc.getMetadata());
            newMetadata.putAll(metadata);
            Document updatedDoc = Document.builder()
                .id(doc.getId())
                .content(doc.getContent())
                .metadata(newMetadata)
                .embedding(doc.getEmbedding())
                .build();
            documents.put(documentId, updatedDoc);
        }
    }

    @Override
    public VectorStoreStats getStats() throws VectorStoreException {
        long totalSize = documents.values().stream()
            .mapToLong(doc -> doc.getContent().length())
            .sum();

        int dimensions = embeddings.isEmpty() ? 0 : embeddings.values().iterator().next().length;

        return new VectorStoreStats(documents.size(), totalSize, dimensions,
            Map.of("store_type", "mock_database"));
    }

    @Override
    public void close() throws VectorStoreException {
        documents.clear();
        embeddings.clear();
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0.0;

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
        if (filter == null || filter.isEmpty()) return true;

        Map<String, Object> docMetadata = document.getMetadata();
        return filter.entrySet().stream()
            .allMatch(entry -> Objects.equals(entry.getValue(), docMetadata.get(entry.getKey())));
    }
}
