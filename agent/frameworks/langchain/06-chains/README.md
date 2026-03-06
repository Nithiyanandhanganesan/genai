# Chains in LangChain (Java)

## 🎯 Overview
Chains in LangChain are sequences of operations that combine multiple components to accomplish complex tasks. They enable building sophisticated workflows by linking together LLMs, prompts, tools, memory, and other components in a structured manner.

## 🧠 Core Chain Concepts

### What are Chains?
Chains are composable units that:
- **Sequential Processing**: Execute components in order
- **Data Flow**: Pass outputs from one component to the next
- **Error Handling**: Manage failures gracefully
- **State Management**: Maintain context across operations
- **Reusability**: Create modular, reusable workflows

### Chain Types
1. **Simple Chains**: Linear sequence of operations
2. **Sequential Chains**: Multiple chains linked together
3. **Router Chains**: Conditional routing based on input
4. **Transform Chains**: Data transformation pipelines
5. **Conversation Chains**: Stateful dialogue management

## 🏗️ Chain Architecture Patterns

### 1. **Linear Chain**
```
Input → Component 1 → Component 2 → Component 3 → Output
```

### 2. **Branching Chain**
```
Input → Analysis → Branch A → Output A
                → Branch B → Output B
```

### 3. **Merge Chain**
```
Input A → Process A ↘
                    → Merge → Output
Input B → Process B ↗
```

### 4. **Recursive Chain**
```
Input → Process → Decision → Output
          ↑         ↓
          ← Refine ←
```

## 💻 Java Chain Implementation

### Base Chain Framework
```java
package com.example.agent.chains;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all chains
 */
public interface Chain<I, O> {
    
    /**
     * Execute the chain with given input
     */
    ChainResult<O> execute(I input) throws ChainExecutionException;
    
    /**
     * Execute the chain asynchronously
     */
    default CompletableFuture<ChainResult<O>> executeAsync(I input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(input);
            } catch (ChainExecutionException e) {
                return ChainResult.failure(e.getMessage());
            }
        });
    }
    
    /**
     * Get chain name for debugging
     */
    String getName();
    
    /**
     * Get chain description
     */
    String getDescription();
    
    /**
     * Validate input before execution
     */
    default boolean validateInput(I input) {
        return input != null;
    }
}

/**
 * Chain execution result
 */
public class ChainResult<T> {
    private final boolean success;
    private final T result;
    private final String errorMessage;
    private final Map<String, Object> metadata;
    private final long executionTimeMs;
    
    public static <T> ChainResult<T> success(T result) {
        return new ChainResult<>(true, result, null, Map.of(), System.currentTimeMillis());
    }
    
    public static <T> ChainResult<T> success(T result, Map<String, Object> metadata, long executionTime) {
        return new ChainResult<>(true, result, null, metadata, executionTime);
    }
    
    public static <T> ChainResult<T> failure(String errorMessage) {
        return new ChainResult<>(false, null, errorMessage, Map.of(), System.currentTimeMillis());
    }
    
    private ChainResult(boolean success, T result, String errorMessage, 
                       Map<String, Object> metadata, long executionTimeMs) {
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
        this.metadata = metadata;
        this.executionTimeMs = executionTimeMs;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public T getResult() { return result; }
    public String getErrorMessage() { return errorMessage; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public long getExecutionTimeMs() { return executionTimeMs; }
}

/**
 * Chain execution exception
 */
public class ChainExecutionException extends Exception {
    public ChainExecutionException(String message) {
        super(message);
    }
    
    public ChainExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Abstract base chain with common functionality
 */
public abstract class BaseChain<I, O> implements Chain<I, O> {
    
    protected final String name;
    protected final String description;
    protected final Map<String, Object> config;
    
    protected BaseChain(String name, String description) {
        this(name, description, Map.of());
    }
    
    protected BaseChain(String name, String description, Map<String, Object> config) {
        this.name = name;
        this.description = description;
        this.config = new HashMap<>(config);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public ChainResult<O> execute(I input) throws ChainExecutionException {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate input
            if (!validateInput(input)) {
                return ChainResult.failure("Invalid input for chain: " + name);
            }
            
            // Log chain execution start
            logExecution("Starting", input);
            
            // Execute the actual chain logic
            O result = executeInternal(input);
            
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Create metadata
            Map<String, Object> metadata = createExecutionMetadata(input, result, executionTime);
            
            // Log successful execution
            logExecution("Completed", result);
            
            return ChainResult.success(result, metadata, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logError("Failed", e);
            throw new ChainExecutionException("Chain execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Subclasses implement the actual chain logic here
     */
    protected abstract O executeInternal(I input) throws Exception;
    
    protected Map<String, Object> createExecutionMetadata(I input, O result, long executionTime) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("chain_name", name);
        metadata.put("execution_time_ms", executionTime);
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("input_type", input.getClass().getSimpleName());
        metadata.put("output_type", result.getClass().getSimpleName());
        return metadata;
    }
    
    protected void logExecution(String phase, Object data) {
        System.out.println(String.format("[%s] %s - %s: %s", 
            new Date(), name, phase, data.toString().substring(0, Math.min(100, data.toString().length()))));
    }
    
    protected void logError(String phase, Exception e) {
        System.err.println(String.format("[%s] %s - %s: %s", 
            new Date(), name, phase, e.getMessage()));
    }
}
```

### LLM Chain Implementation
```java
/**
 * Basic LLM chain for single prompt-response
 */
public class LLMChain extends BaseChain<String, String> {
    
    private final ChatLanguageModel llm;
    private final PromptTemplate promptTemplate;
    
    public LLMChain(ChatLanguageModel llm, PromptTemplate promptTemplate) {
        super("LLMChain", "Basic LLM prompt-response chain");
        this.llm = llm;
        this.promptTemplate = promptTemplate;
    }
    
    @Override
    protected String executeInternal(String input) throws Exception {
        // Format prompt with input
        String formattedPrompt = promptTemplate.format(Map.of("input", input));
        
        // Generate LLM response
        AiMessage response = llm.generate(UserMessage.from(formattedPrompt)).content();
        
        return response.text();
    }
    
    @Override
    public boolean validateInput(String input) {
        return input != null && !input.trim().isEmpty();
    }
}

/**
 * Conversation chain with memory
 */
public class ConversationChain extends BaseChain<String, String> {
    
    private final ChatLanguageModel llm;
    private final ConversationBufferMemory memory;
    private final PromptTemplate promptTemplate;
    
    public ConversationChain(ChatLanguageModel llm, ConversationBufferMemory memory, 
                            PromptTemplate promptTemplate) {
        super("ConversationChain", "Stateful conversation chain with memory");
        this.llm = llm;
        this.memory = memory;
        this.promptTemplate = promptTemplate;
    }
    
    @Override
    protected String executeInternal(String input) throws Exception {
        // Add user message to memory
        memory.addMessage(UserMessage.from(input));
        
        // Get conversation history
        String conversationHistory = memory.getFormattedHistory();
        
        // Format prompt with history and current input
        Map<String, Object> variables = Map.of(
            "history", conversationHistory,
            "input", input
        );
        String formattedPrompt = promptTemplate.format(variables);
        
        // Generate response
        AiMessage response = llm.generate(UserMessage.from(formattedPrompt)).content();
        
        // Add AI response to memory
        memory.addMessage(response);
        
        return response.text();
    }
}

/**
 * Sequential chain that runs multiple chains in sequence
 */
public class SequentialChain extends BaseChain<String, String> {
    
    private final List<Chain<String, String>> chains;
    private final boolean stopOnFailure;
    
    public SequentialChain(List<Chain<String, String>> chains) {
        this(chains, true);
    }
    
    public SequentialChain(List<Chain<String, String>> chains, boolean stopOnFailure) {
        super("SequentialChain", "Chain that executes multiple chains in sequence");
        this.chains = new ArrayList<>(chains);
        this.stopOnFailure = stopOnFailure;
    }
    
    @Override
    protected String executeInternal(String input) throws Exception {
        String currentInput = input;
        StringBuilder results = new StringBuilder();
        
        for (int i = 0; i < chains.size(); i++) {
            Chain<String, String> chain = chains.get(i);
            
            try {
                ChainResult<String> result = chain.execute(currentInput);
                
                if (result.isSuccess()) {
                    currentInput = result.getResult();
                    results.append("Step ").append(i + 1).append(" (").append(chain.getName())
                           .append("): ").append(result.getResult()).append("\n");
                } else {
                    String error = "Step " + (i + 1) + " failed: " + result.getErrorMessage();
                    
                    if (stopOnFailure) {
                        throw new ChainExecutionException(error);
                    } else {
                        results.append(error).append("\n");
                        currentInput = "Previous step failed: " + result.getErrorMessage();
                    }
                }
            } catch (ChainExecutionException e) {
                if (stopOnFailure) {
                    throw e;
                } else {
                    results.append("Step ").append(i + 1).append(" error: ").append(e.getMessage()).append("\n");
                    currentInput = "Previous step error: " + e.getMessage();
                }
            }
        }
        
        return currentInput; // Return final result
    }
    
    @Override
    protected Map<String, Object> createExecutionMetadata(String input, String result, long executionTime) {
        Map<String, Object> metadata = super.createExecutionMetadata(input, result, executionTime);
        metadata.put("num_chains", chains.size());
        metadata.put("chain_names", chains.stream().map(Chain::getName).toList());
        metadata.put("stop_on_failure", stopOnFailure);
        return metadata;
    }
}

/**
 * Router chain that selects different chains based on input
 */
public class RouterChain extends BaseChain<String, String> {
    
    private final ChatLanguageModel llm;
    private final Map<String, Chain<String, String>> routingMap;
    private final Chain<String, String> defaultChain;
    
    public RouterChain(ChatLanguageModel llm, Map<String, Chain<String, String>> routingMap, 
                      Chain<String, String> defaultChain) {
        super("RouterChain", "Chain that routes to different chains based on input analysis");
        this.llm = llm;
        this.routingMap = new HashMap<>(routingMap);
        this.defaultChain = defaultChain;
    }
    
    @Override
    protected String executeInternal(String input) throws Exception {
        // Analyze input to determine routing
        String routingKey = analyzeInputForRouting(input);
        
        // Select appropriate chain
        Chain<String, String> selectedChain = routingMap.getOrDefault(routingKey, defaultChain);
        
        if (selectedChain == null) {
            throw new ChainExecutionException("No appropriate chain found for input: " + input);
        }
        
        // Execute selected chain
        ChainResult<String> result = selectedChain.execute(input);
        
        if (result.isSuccess()) {
            return "Routed to: " + selectedChain.getName() + "\nResult: " + result.getResult();
        } else {
            throw new ChainExecutionException("Routed chain failed: " + result.getErrorMessage());
        }
    }
    
    private String analyzeInputForRouting(String input) throws Exception {
        StringBuilder routingPrompt = new StringBuilder();
        routingPrompt.append("Analyze this input and determine the most appropriate category:\n");
        routingPrompt.append("Input: ").append(input).append("\n\n");
        routingPrompt.append("Available categories:\n");
        
        for (String key : routingMap.keySet()) {
            routingPrompt.append("- ").append(key).append("\n");
        }
        
        routingPrompt.append("\nRespond with just the category name:");
        
        AiMessage response = llm.generate(UserMessage.from(routingPrompt.toString())).content();
        return response.text().trim().toLowerCase();
    }
    
    @Override
    protected Map<String, Object> createExecutionMetadata(String input, String result, long executionTime) {
        Map<String, Object> metadata = super.createExecutionMetadata(input, result, executionTime);
        metadata.put("available_routes", new ArrayList<>(routingMap.keySet()));
        metadata.put("has_default_chain", defaultChain != null);
        return metadata;
    }
}
```

### Transform and Processing Chains
```java
/**
 * Transform chain for data processing
 */
public class TransformChain<I, O> extends BaseChain<I, O> {
    
    private final Function<I, O> transformer;
    
    public TransformChain(String name, String description, Function<I, O> transformer) {
        super(name, description);
        this.transformer = transformer;
    }
    
    @Override
    protected O executeInternal(I input) throws Exception {
        try {
            return transformer.apply(input);
        } catch (Exception e) {
            throw new ChainExecutionException("Transform failed: " + e.getMessage(), e);
        }
    }
}

/**
 * Text processing chain for document analysis
 */
public class TextProcessingChain extends BaseChain<String, Map<String, Object>> {
    
    private final ChatLanguageModel llm;
    
    public TextProcessingChain(ChatLanguageModel llm) {
        super("TextProcessingChain", "Comprehensive text analysis and processing");
        this.llm = llm;
    }
    
    @Override
    protected Map<String, Object> executeInternal(String input) throws Exception {
        Map<String, Object> results = new HashMap<>();
        
        // Basic statistics
        results.put("character_count", input.length());
        results.put("word_count", input.split("\\s+").length);
        results.put("sentence_count", input.split("[.!?]+").length);
        
        // Extract key information using LLM
        Map<String, String> analysisPrompts = Map.of(
            "summary", "Provide a brief summary of this text: " + input,
            "sentiment", "Analyze the sentiment of this text (positive/negative/neutral): " + input,
            "topics", "Extract the main topics from this text: " + input,
            "entities", "Extract key entities (people, places, organizations) from this text: " + input
        );
        
        for (Map.Entry<String, String> entry : analysisPrompts.entrySet()) {
            try {
                AiMessage response = llm.generate(UserMessage.from(entry.getValue())).content();
                results.put(entry.getKey(), response.text());
            } catch (Exception e) {
                results.put(entry.getKey(), "Analysis failed: " + e.getMessage());
            }
        }
        
        return results;
    }
    
    @Override
    public boolean validateInput(String input) {
        return input != null && input.trim().length() > 10; // Minimum text length
    }
}

/**
 * Question-Answer chain for document QA
 */
public class QAChain extends BaseChain<QAInput, String> {
    
    private final ChatLanguageModel llm;
    private final VectorMemory vectorMemory; // Assuming we have vector memory for context
    
    public QAChain(ChatLanguageModel llm, VectorMemory vectorMemory) {
        super("QAChain", "Question-answering chain with context retrieval");
        this.llm = llm;
        this.vectorMemory = vectorMemory;
    }
    
    @Override
    protected String executeInternal(QAInput input) throws Exception {
        String question = input.getQuestion();
        String context = input.getContext();
        
        // If no context provided, try to retrieve from vector memory
        if (context == null || context.trim().isEmpty()) {
            List<RelevantMemory> relevantMemories = vectorMemory.retrieveRelevantMemories(question, 3);
            context = relevantMemories.stream()
                .map(mem -> mem.getEntry().getContent())
                .collect(Collectors.joining("\n"));
        }
        
        // Create QA prompt
        String qaPrompt = String.format(
            "Context: %s\n\nQuestion: %s\n\n" +
            "Answer the question based on the provided context. " +
            "If the answer is not in the context, say 'I don't have enough information to answer that question.'",
            context, question
        );
        
        // Generate answer
        AiMessage response = llm.generate(UserMessage.from(qaPrompt)).content();
        return response.text();
    }
    
    @Override
    public boolean validateInput(QAInput input) {
        return input != null && input.getQuestion() != null && !input.getQuestion().trim().isEmpty();
    }
}

/**
 * QA input container
 */
class QAInput {
    private final String question;
    private final String context;
    
    public QAInput(String question) {
        this(question, null);
    }
    
    public QAInput(String question, String context) {
        this.question = question;
        this.context = context;
    }
    
    public String getQuestion() { return question; }
    public String getContext() { return context; }
}
```

### Advanced Chain Patterns
```java
/**
 * Parallel execution chain for concurrent processing
 */
public class ParallelChain extends BaseChain<String, List<String>> {
    
    private final List<Chain<String, String>> parallelChains;
    private final ExecutorService executorService;
    
    public ParallelChain(List<Chain<String, String>> parallelChains) {
        super("ParallelChain", "Execute multiple chains concurrently");
        this.parallelChains = new ArrayList<>(parallelChains);
        this.executorService = Executors.newFixedThreadPool(Math.min(parallelChains.size(), 10));
    }
    
    @Override
    protected List<String> executeInternal(String input) throws Exception {
        // Submit all chains for parallel execution
        List<CompletableFuture<String>> futures = parallelChains.stream()
            .map(chain -> CompletableFuture.supplyAsync(() -> {
                try {
                    ChainResult<String> result = chain.execute(input);
                    return result.isSuccess() ? result.getResult() : 
                           "Chain " + chain.getName() + " failed: " + result.getErrorMessage();
                } catch (ChainExecutionException e) {
                    return "Chain " + chain.getName() + " error: " + e.getMessage();
                }
            }, executorService))
            .toList();
        
        // Collect all results
        List<String> results = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            try {
                results.add(future.get(30, TimeUnit.SECONDS)); // 30 second timeout
            } catch (Exception e) {
                results.add("Execution timeout or error: " + e.getMessage());
            }
        }
        
        return results;
    }
    
    @Override
    protected Map<String, Object> createExecutionMetadata(String input, List<String> result, long executionTime) {
        Map<String, Object> metadata = super.createExecutionMetadata(input, result, executionTime);
        metadata.put("parallel_chains", parallelChains.size());
        metadata.put("chain_names", parallelChains.stream().map(Chain::getName).toList());
        metadata.put("results_count", result.size());
        return metadata;
    }
}

/**
 * Retry chain with failure handling
 */
public class RetryChain<I, O> extends BaseChain<I, O> {
    
    private final Chain<I, O> innerChain;
    private final int maxRetries;
    private final long retryDelayMs;
    private final Predicate<Exception> shouldRetry;
    
    public RetryChain(Chain<I, O> innerChain, int maxRetries) {
        this(innerChain, maxRetries, 1000, e -> true);
    }
    
    public RetryChain(Chain<I, O> innerChain, int maxRetries, long retryDelayMs, 
                     Predicate<Exception> shouldRetry) {
        super("RetryChain[" + innerChain.getName() + "]", 
              "Retry wrapper for " + innerChain.getDescription());
        this.innerChain = innerChain;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.shouldRetry = shouldRetry;
    }
    
    @Override
    protected O executeInternal(I input) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                ChainResult<O> result = innerChain.execute(input);
                
                if (result.isSuccess()) {
                    return result.getResult();
                } else {
                    lastException = new ChainExecutionException(result.getErrorMessage());
                    
                    if (!shouldRetry.test(lastException) || attempt == maxRetries) {
                        throw lastException;
                    }
                }
            } catch (ChainExecutionException e) {
                lastException = e;
                
                if (!shouldRetry.test(e) || attempt == maxRetries) {
                    throw e;
                }
            }
            
            // Wait before retry
            if (attempt < maxRetries) {
                Thread.sleep(retryDelayMs);
            }
        }
        
        throw lastException != null ? lastException : 
            new ChainExecutionException("All retries failed");
    }
    
    @Override
    protected Map<String, Object> createExecutionMetadata(I input, O result, long executionTime) {
        Map<String, Object> metadata = super.createExecutionMetadata(input, result, executionTime);
        metadata.put("inner_chain", innerChain.getName());
        metadata.put("max_retries", maxRetries);
        metadata.put("retry_delay_ms", retryDelayMs);
        return metadata;
    }
}
```

## 🔄 Chain Composition and Builder

### Chain Builder Pattern
```java
/**
 * Fluent builder for creating complex chain compositions
 */
public class ChainBuilder {
    
    private final ChatLanguageModel llm;
    
    public ChainBuilder(ChatLanguageModel llm) {
        this.llm = llm;
    }
    
    public static ChainBuilder create(ChatLanguageModel llm) {
        return new ChainBuilder(llm);
    }
    
    /**
     * Create a simple LLM chain
     */
    public LLMChain llm(PromptTemplate template) {
        return new LLMChain(llm, template);
    }
    
    /**
     * Create a conversation chain
     */
    public ConversationChain conversation(ConversationBufferMemory memory, PromptTemplate template) {
        return new ConversationChain(llm, memory, template);
    }
    
    /**
     * Create a sequential chain
     */
    public SequentialChain sequential(Chain<String, String>... chains) {
        return new SequentialChain(Arrays.asList(chains));
    }
    
    /**
     * Create a router chain
     */
    public RouterChainBuilder router() {
        return new RouterChainBuilder(llm);
    }
    
    /**
     * Create a parallel chain
     */
    public ParallelChain parallel(Chain<String, String>... chains) {
        return new ParallelChain(Arrays.asList(chains));
    }
    
    /**
     * Create a retry wrapper
     */
    public <I, O> RetryChain<I, O> retry(Chain<I, O> chain, int maxRetries) {
        return new RetryChain<>(chain, maxRetries);
    }
}

/**
 * Builder for router chains
 */
class RouterChainBuilder {
    private final ChatLanguageModel llm;
    private final Map<String, Chain<String, String>> routes = new HashMap<>();
    private Chain<String, String> defaultChain;
    
    public RouterChainBuilder(ChatLanguageModel llm) {
        this.llm = llm;
    }
    
    public RouterChainBuilder route(String key, Chain<String, String> chain) {
        routes.put(key, chain);
        return this;
    }
    
    public RouterChainBuilder defaultRoute(Chain<String, String> chain) {
        this.defaultChain = chain;
        return this;
    }
    
    public RouterChain build() {
        return new RouterChain(llm, routes, defaultChain);
    }
}
```

## 🚀 Best Practices

1. **Chain Design**
   - Keep chains focused and single-purpose
   - Use clear naming and descriptions
   - Implement proper input validation
   - Handle errors gracefully

2. **Performance**
   - Use parallel chains for independent operations
   - Implement caching where appropriate
   - Set reasonable timeouts
   - Monitor execution times

3. **Error Handling**
   - Provide meaningful error messages
   - Implement retry mechanisms for transient failures
   - Use circuit breaker patterns for external dependencies
   - Log failures for debugging

4. **Composition**
   - Design chains to be composable
   - Use builder patterns for complex compositions
   - Support both synchronous and asynchronous execution
   - Provide metadata for debugging

5. **Testing**
   - Test individual chains in isolation
   - Test chain compositions end-to-end
   - Mock external dependencies
   - Test error conditions and edge cases

## 🔗 Integration with Other Components

Chains integrate with:
- **Memory**: Chains can use memory for context
- **Tools**: Chains can incorporate tool usage
- **Agents**: Agents can use chains for reasoning
- **State Management**: Chains can trigger state changes

---

*Next: [Agents](../agents/) - Learn about autonomous decision-making entities that can use chains and tools.*
