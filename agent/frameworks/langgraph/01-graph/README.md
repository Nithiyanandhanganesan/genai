# Graph Architecture in LangGraph (Java)

## 🎯 Overview
LangGraph's graph architecture represents workflows as directed graphs where nodes perform computations and edges define the flow of execution. This enables complex, non-linear workflows with conditional logic, loops, and parallel processing - capabilities that traditional chain-based approaches cannot easily handle.

## 🧠 Core Graph Concepts

### Graph Components
1. **Nodes**: Computation units (functions, agents, tools, human input)
2. **Edges**: Flow control between nodes (static or conditional)
3. **State**: Data that flows through the graph and is modified by nodes
4. **Compilation**: Process of preparing the graph for execution
5. **Execution**: Running the graph with input data

### Graph Types
- **Sequential Graphs**: Linear execution flow
- **Branching Graphs**: Conditional paths based on state
- **Cyclic Graphs**: Loops and iterative processing
- **Parallel Graphs**: Concurrent node execution

## 🏗️ Graph Architecture Patterns

### 1. **Simple Sequential Graph**
```
[Start] → [Process] → [Validate] → [End]
```

### 2. **Conditional Branching Graph**
```
[Start] → [Decision Node] → [Path A] → [End]
                        ↓
                      [Path B] → [End]
```

### 3. **Iterative Processing Graph**
```
[Start] → [Process] → [Check] → [End]
            ↑           ↓
            └─[Refine]←─┘
```

### 4. **Human-in-the-Loop Graph**
```
[Start] → [Generate] → [Human Review] → [Finalize] → [End]
                            ↓
                       [Revise] ←─────────┘
```

## 💻 Java Implementation with LangGraph4j

### Basic Graph Structure
```java
package com.example.agent.graph;

import java.util.*;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;

/**
 * State definition for the graph
 */
public class GraphState {
    private Map<String, Object> data;
    private String currentStep;
    private List<String> executionPath;
    private Map<String, Object> metadata;
    
    public GraphState() {
        this.data = new HashMap<>();
        this.executionPath = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    public GraphState(Map<String, Object> initialData) {
        this();
        this.data.putAll(initialData);
    }
    
    // State manipulation methods
    public void updateData(String key, Object value) {
        data.put(key, value);
    }
    
    public <T> Optional<T> getData(String key, Class<T> type) {
        Object value = data.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }
    
    public void addExecutionStep(String step) {
        executionPath.add(step);
        currentStep = step;
    }
    
    // Getters and setters
    public Map<String, Object> getData() { return new HashMap<>(data); }
    public void setData(Map<String, Object> data) { this.data = new HashMap<>(data); }
    
    public String getCurrentStep() { return currentStep; }
    public List<String> getExecutionPath() { return new ArrayList<>(executionPath); }
    
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public void addMetadata(String key, Object value) { metadata.put(key, value); }
}

/**
 * Graph node definition
 */
@FunctionalInterface
public interface GraphNode {
    GraphState execute(GraphState state) throws Exception;
}

/**
 * Edge condition for conditional routing
 */
@FunctionalInterface
public interface EdgeCondition {
    String evaluate(GraphState state);
}

/**
 * Graph edge definition
 */
public class GraphEdge {
    private final String fromNode;
    private final String toNode;
    private final EdgeCondition condition;
    private final boolean isConditional;
    
    // Static edge constructor
    public GraphEdge(String fromNode, String toNode) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.condition = null;
        this.isConditional = false;
    }
    
    // Conditional edge constructor
    public GraphEdge(String fromNode, EdgeCondition condition) {
        this.fromNode = fromNode;
        this.toNode = null;
        this.condition = condition;
        this.isConditional = true;
    }
    
    public String getNextNode(GraphState state) {
        if (isConditional) {
            return condition.evaluate(state);
        }
        return toNode;
    }
    
    // Getters
    public String getFromNode() { return fromNode; }
    public String getToNode() { return toNode; }
    public boolean isConditional() { return isConditional; }
}

/**
 * Main StateGraph implementation
 */
public class StateGraph {
    
    private final Map<String, GraphNode> nodes;
    private final List<GraphEdge> edges;
    private String startNode;
    private final Set<String> endNodes;
    private boolean compiled;
    
    public StateGraph() {
        this.nodes = new LinkedHashMap<>();
        this.edges = new ArrayList<>();
        this.endNodes = new HashSet<>();
        this.compiled = false;
    }
    
    /**
     * Add a node to the graph
     */
    public StateGraph addNode(String name, GraphNode node) {
        if (compiled) {
            throw new IllegalStateException("Cannot modify compiled graph");
        }
        nodes.put(name, node);
        return this;
    }
    
    /**
     * Add a static edge between two nodes
     */
    public StateGraph addEdge(String fromNode, String toNode) {
        if (compiled) {
            throw new IllegalStateException("Cannot modify compiled graph");
        }
        edges.add(new GraphEdge(fromNode, toNode));
        return this;
    }
    
    /**
     * Add a conditional edge with routing logic
     */
    public StateGraph addConditionalEdge(String fromNode, EdgeCondition condition) {
        if (compiled) {
            throw new IllegalStateException("Cannot modify compiled graph");
        }
        edges.add(new GraphEdge(fromNode, condition));
        return this;
    }
    
    /**
     * Set the starting node
     */
    public StateGraph setEntryPoint(String startNode) {
        this.startNode = startNode;
        return this;
    }
    
    /**
     * Add end node
     */
    public StateGraph addEndNode(String endNode) {
        endNodes.add(endNode);
        return this;
    }
    
    /**
     * Compile the graph for execution
     */
    public CompiledGraph compile() {
        if (startNode == null) {
            throw new IllegalStateException("Start node must be set before compilation");
        }
        
        // Validate graph structure
        validateGraph();
        
        compiled = true;
        return new CompiledGraph(this);
    }
    
    private void validateGraph() {
        // Check that all referenced nodes exist
        for (GraphEdge edge : edges) {
            if (!nodes.containsKey(edge.getFromNode())) {
                throw new IllegalArgumentException("Unknown node: " + edge.getFromNode());
            }
            
            if (!edge.isConditional() && !nodes.containsKey(edge.getToNode()) && 
                !endNodes.contains(edge.getToNode()) && !"END".equals(edge.getToNode())) {
                throw new IllegalArgumentException("Unknown node: " + edge.getToNode());
            }
        }
        
        // Check that start node exists
        if (!nodes.containsKey(startNode)) {
            throw new IllegalArgumentException("Start node does not exist: " + startNode);
        }
    }
    
    // Getters for compiled graph
    Map<String, GraphNode> getNodes() { return nodes; }
    List<GraphEdge> getEdges() { return edges; }
    String getStartNode() { return startNode; }
    Set<String> getEndNodes() { return endNodes; }
}

/**
 * Compiled and executable graph
 */
public class CompiledGraph {
    
    private final StateGraph sourceGraph;
    private final Map<String, List<GraphEdge>> outgoingEdges;
    
    public CompiledGraph(StateGraph sourceGraph) {
        this.sourceGraph = sourceGraph;
        this.outgoingEdges = buildEdgeMap();
    }
    
    private Map<String, List<GraphEdge>> buildEdgeMap() {
        Map<String, List<GraphEdge>> edgeMap = new HashMap<>();
        
        for (GraphEdge edge : sourceGraph.getEdges()) {
            edgeMap.computeIfAbsent(edge.getFromNode(), k -> new ArrayList<>()).add(edge);
        }
        
        return edgeMap;
    }
    
    /**
     * Execute the graph with initial state
     */
    public GraphState invoke(GraphState initialState) {
        return invoke(initialState, new HashMap<>());
    }
    
    /**
     * Execute the graph with configuration options
     */
    public GraphState invoke(GraphState initialState, Map<String, Object> config) {
        GraphState currentState = new GraphState(initialState.getData());
        currentState.addMetadata("executionId", UUID.randomUUID().toString());
        currentState.addMetadata("startTime", System.currentTimeMillis());
        
        String currentNode = sourceGraph.getStartNode();
        int maxIterations = (Integer) config.getOrDefault("maxIterations", 100);
        int iterations = 0;
        
        try {
            while (currentNode != null && !sourceGraph.getEndNodes().contains(currentNode) && 
                   !"END".equals(currentNode) && iterations < maxIterations) {
                
                iterations++;
                currentState.addExecutionStep(currentNode);
                
                // Execute current node
                GraphNode node = sourceGraph.getNodes().get(currentNode);
                if (node != null) {
                    currentState = node.execute(currentState);
                }
                
                // Determine next node
                currentNode = getNextNode(currentNode, currentState);
                
                // Add debugging information
                currentState.addMetadata("lastNode", currentNode);
                currentState.addMetadata("iteration", iterations);
            }
            
            // Add final execution metadata
            currentState.addMetadata("endTime", System.currentTimeMillis());
            currentState.addMetadata("totalIterations", iterations);
            currentState.addMetadata("completed", true);
            
            if (iterations >= maxIterations) {
                currentState.addMetadata("terminationReason", "maxIterations");
            } else {
                currentState.addMetadata("terminationReason", "normal");
            }
            
        } catch (Exception e) {
            currentState.addMetadata("error", e.getMessage());
            currentState.addMetadata("errorType", e.getClass().getSimpleName());
            currentState.addMetadata("completed", false);
            throw new RuntimeException("Graph execution failed", e);
        }
        
        return currentState;
    }
    
    /**
     * Execute graph asynchronously
     */
    public CompletableFuture<GraphState> invokeAsync(GraphState initialState) {
        return CompletableFuture.supplyAsync(() -> invoke(initialState));
    }
    
    private String getNextNode(String currentNode, GraphState state) {
        List<GraphEdge> edges = outgoingEdges.get(currentNode);
        if (edges == null || edges.isEmpty()) {
            return "END";
        }
        
        // For conditional edges, evaluate condition
        for (GraphEdge edge : edges) {
            if (edge.isConditional()) {
                return edge.getNextNode(state);
            } else {
                return edge.getToNode();
            }
        }
        
        return "END";
    }
    
    /**
     * Get graph structure for visualization
     */
    public Map<String, Object> getGraphStructure() {
        Map<String, Object> structure = new HashMap<>();
        structure.put("nodes", new ArrayList<>(sourceGraph.getNodes().keySet()));
        structure.put("edges", sourceGraph.getEdges().stream()
            .map(edge -> Map.of(
                "from", edge.getFromNode(),
                "to", edge.isConditional() ? "conditional" : edge.getToNode(),
                "type", edge.isConditional() ? "conditional" : "static"
            ))
            .toList());
        structure.put("startNode", sourceGraph.getStartNode());
        structure.put("endNodes", sourceGraph.getEndNodes());
        
        return structure;
    }
}
```

### Example: Document Processing Workflow
```java
public class DocumentProcessingGraph {
    
    public static CompiledGraph createDocumentProcessingGraph(
            ChatLanguageModel llm,
            DocumentAnalysisService analysisService) {
        
        StateGraph graph = new StateGraph();
        
        // Add nodes for each step
        graph.addNode("analyze_document", state -> {
            String documentContent = state.getData("document_content", String.class)
                .orElseThrow(() -> new IllegalArgumentException("Document content required"));
            
            // Analyze document structure and content
            DocumentAnalysis analysis = analysisService.analyzeDocument(documentContent);
            
            state.updateData("analysis", analysis);
            state.updateData("document_type", analysis.getDocumentType());
            state.updateData("confidence", analysis.getConfidence());
            
            return state;
        });
        
        graph.addNode("extract_entities", state -> {
            String content = state.getData("document_content", String.class).orElse("");
            
            String entityPrompt = "Extract all entities from this document: " + content;
            AiMessage response = llm.generate(UserMessage.from(entityPrompt)).content();
            
            state.updateData("entities", parseEntities(response.text()));
            return state;
        });
        
        graph.addNode("generate_summary", state -> {
            String content = state.getData("document_content", String.class).orElse("");
            
            String summaryPrompt = "Provide a comprehensive summary of this document: " + content;
            AiMessage response = llm.generate(UserMessage.from(summaryPrompt)).content();
            
            state.updateData("summary", response.text());
            return state;
        });
        
        graph.addNode("human_review", state -> {
            // In real implementation, this would pause for human input
            System.out.println("Document ready for human review:");
            System.out.println("Summary: " + state.getData("summary", String.class).orElse(""));
            
            // Simulate human approval
            state.updateData("human_approved", true);
            state.updateData("review_notes", "Document approved by human reviewer");
            
            return state;
        });
        
        graph.addNode("finalize_processing", state -> {
            Map<String, Object> results = new HashMap<>();
            results.put("document_type", state.getData("document_type", String.class).orElse("unknown"));
            results.put("entities", state.getData("entities", List.class).orElse(Collections.emptyList()));
            results.put("summary", state.getData("summary", String.class).orElse(""));
            results.put("processed_at", System.currentTimeMillis());
            
            state.updateData("final_results", results);
            state.updateData("processing_complete", true);
            
            return state;
        });
        
        // Add workflow edges with conditional logic
        graph.setEntryPoint("analyze_document");
        
        // After analysis, decide path based on confidence
        graph.addConditionalEdge("analyze_document", state -> {
            Double confidence = state.getData("confidence", Double.class).orElse(0.0);
            
            if (confidence > 0.8) {
                return "extract_entities";
            } else {
                return "human_review";
            }
        });
        
        graph.addEdge("extract_entities", "generate_summary");
        graph.addEdge("generate_summary", "finalize_processing");
        
        // Human review can approve or require reprocessing
        graph.addConditionalEdge("human_review", state -> {
            Boolean approved = state.getData("human_approved", Boolean.class).orElse(false);
            
            if (approved) {
                return "extract_entities";
            } else {
                return "analyze_document"; // Retry analysis
            }
        });
        
        graph.addEndNode("finalize_processing");
        
        return graph.compile();
    }
    
    private static List<String> parseEntities(String entityResponse) {
        // Simple entity parsing - in real implementation, use proper JSON parsing
        return Arrays.asList(entityResponse.split(","))
            .stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }
}
```

### Example: Customer Support Workflow
```java
public class CustomerSupportGraph {
    
    public static CompiledGraph createSupportGraph(
            ChatLanguageModel llm,
            TicketingService ticketService,
            KnowledgeBaseService knowledgeBase) {
        
        StateGraph graph = new StateGraph();
        
        // Classify incoming request
        graph.addNode("classify_request", state -> {
            String userMessage = state.getData("user_message", String.class)
                .orElseThrow(() -> new IllegalArgumentException("User message required"));
            
            String classificationPrompt = String.format(
                "Classify this customer request into categories: technical, billing, general, complaint.\n" +
                "Request: %s\nCategory:", userMessage);
            
            AiMessage response = llm.generate(UserMessage.from(classificationPrompt)).content();
            String category = response.text().toLowerCase().trim();
            
            state.updateData("request_category", category);
            state.updateData("classification_confidence", 0.85); // Placeholder
            
            return state;
        });
        
        // Search knowledge base for solutions
        graph.addNode("search_knowledge_base", state -> {
            String userMessage = state.getData("user_message", String.class).orElse("");
            String category = state.getData("request_category", String.class).orElse("");
            
            List<String> solutions = knowledgeBase.searchSolutions(userMessage, category);
            
            state.updateData("kb_solutions", solutions);
            state.updateData("solutions_found", !solutions.isEmpty());
            
            return state;
        });
        
        // Generate automated response
        graph.addNode("generate_response", state -> {
            String userMessage = state.getData("user_message", String.class).orElse("");
            List<String> solutions = (List<String>) state.getData("kb_solutions", List.class)
                .orElse(Collections.emptyList());
            
            String responsePrompt = String.format(
                "Generate a helpful customer service response for this request: %s\n" +
                "Available solutions: %s", userMessage, String.join(", ", solutions));
            
            AiMessage response = llm.generate(UserMessage.from(responsePrompt)).content();
            
            state.updateData("generated_response", response.text());
            return state;
        });
        
        // Create support ticket for complex issues
        graph.addNode("create_ticket", state -> {
            String userMessage = state.getData("user_message", String.class).orElse("");
            String category = state.getData("request_category", String.class).orElse("");
            
            String ticketId = ticketService.createTicket(userMessage, category);
            
            state.updateData("ticket_id", ticketId);
            state.updateData("ticket_created", true);
            
            return state;
        });
        
        // Human agent review
        graph.addNode("agent_review", state -> {
            String ticketId = state.getData("ticket_id", String.class).orElse("");
            System.out.println("Ticket " + ticketId + " assigned to human agent");
            
            // Simulate agent response
            state.updateData("agent_response", "Human agent will respond within 24 hours");
            state.updateData("escalated", true);
            
            return state;
        });
        
        // Set up workflow routing
        graph.setEntryPoint("classify_request");
        graph.addEdge("classify_request", "search_knowledge_base");
        
        // Route based on whether solutions were found
        graph.addConditionalEdge("search_knowledge_base", state -> {
            Boolean solutionsFound = state.getData("solutions_found", Boolean.class).orElse(false);
            String category = state.getData("request_category", String.class).orElse("");
            
            if (solutionsFound && !"complaint".equals(category)) {
                return "generate_response";
            } else {
                return "create_ticket";
            }
        });
        
        graph.addEndNode("generate_response");
        
        // Complex issues go to human review
        graph.addConditionalEdge("create_ticket", state -> {
            String category = state.getData("request_category", String.class).orElse("");
            
            if ("complaint".equals(category) || "billing".equals(category)) {
                return "agent_review";
            } else {
                return "END";
            }
        });
        
        graph.addEndNode("agent_review");
        
        return graph.compile();
    }
}
```

## 🔧 Advanced Graph Features

### Parallel Node Execution
```java
public class ParallelExecutionGraph {
    
    public static CompiledGraph createParallelProcessingGraph() {
        StateGraph graph = new StateGraph();
        
        // Split work into parallel streams
        graph.addNode("split_work", state -> {
            List<String> documents = state.getData("documents", List.class)
                .orElse(Collections.emptyList());
            
            // Split documents into chunks for parallel processing
            List<List<String>> chunks = chunkDocuments(documents, 3);
            state.updateData("document_chunks", chunks);
            
            return state;
        });
        
        // Process chunks in parallel (conceptually - actual parallel execution
        // would require async framework integration)
        graph.addNode("process_chunk_1", state -> {
            List<List<String>> chunks = (List<List<String>>) state.getData("document_chunks", List.class)
                .orElse(Collections.emptyList());
            
            if (!chunks.isEmpty()) {
                List<String> results = processDocumentChunk(chunks.get(0));
                state.updateData("results_1", results);
            }
            
            return state;
        });
        
        graph.addNode("process_chunk_2", state -> {
            List<List<String>> chunks = (List<List<String>>) state.getData("document_chunks", List.class)
                .orElse(Collections.emptyList());
            
            if (chunks.size() > 1) {
                List<String> results = processDocumentChunk(chunks.get(1));
                state.updateData("results_2", results);
            }
            
            return state;
        });
        
        graph.addNode("merge_results", state -> {
            List<String> results1 = (List<String>) state.getData("results_1", List.class)
                .orElse(Collections.emptyList());
            List<String> results2 = (List<String>) state.getData("results_2", List.class)
                .orElse(Collections.emptyList());
            
            List<String> allResults = new ArrayList<>();
            allResults.addAll(results1);
            allResults.addAll(results2);
            
            state.updateData("final_results", allResults);
            
            return state;
        });
        
        graph.setEntryPoint("split_work");
        graph.addEdge("split_work", "process_chunk_1");
        graph.addEdge("split_work", "process_chunk_2");
        
        // Both parallel branches merge before final step
        graph.addEdge("process_chunk_1", "merge_results");
        graph.addEdge("process_chunk_2", "merge_results");
        
        graph.addEndNode("merge_results");
        
        return graph.compile();
    }
}
```

## 🚀 Best Practices

1. **Graph Design**
   - Keep nodes focused on single responsibilities
   - Use clear, descriptive node names
   - Design for error handling and recovery
   - Plan for debugging and observability

2. **State Management**
   - Use typed state access methods
   - Validate state at node boundaries
   - Keep state immutable where possible
   - Log state changes for debugging

3. **Performance**
   - Minimize state size for better performance
   - Use lazy evaluation where appropriate
   - Consider parallel execution for independent paths
   - Implement proper timeout handling

4. **Error Handling**
   - Add error nodes for graceful failure handling
   - Implement retry mechanisms for transient failures
   - Log errors with sufficient context
   - Provide meaningful error messages

5. **Testing**
   - Test individual nodes in isolation
   - Test complete graph workflows
   - Test error conditions and edge cases
   - Use graph visualization for debugging

---

*Next: [State Management in LangGraph](../state/) - Learn about managing state in graph-based workflows.*
