# Agents in LangChain (Java)

## 🎯 Overview
Agents in LangChain are autonomous entities that can reason, make decisions, and take actions to accomplish tasks. Unlike simple chains, agents can dynamically decide which tools to use, when to use them, and how to interpret results to achieve their goals.

## 🧠 Core Agent Concepts

### What are Agents?
Agents are intelligent systems that:
- **Reason**: Analyze problems and plan approaches
- **Decide**: Choose appropriate tools and actions
- **Act**: Execute tools and process results  
- **Adapt**: Learn from outcomes and adjust strategies
- **Persist**: Remember context across interactions

### Agent Types
1. **ReAct Agents**: Reasoning and Acting iteratively
2. **Plan-and-Execute Agents**: Plan first, then execute
3. **Tool-Using Agents**: Specialized for tool interactions
4. **Conversational Agents**: Dialogue-focused agents
5. **Multi-Agent Systems**: Coordinated agent teams

## 🏗️ Agent Architecture Patterns

### 1. **ReAct Pattern**
```
Thought → Action → Observation → Thought → Action → ...
```

### 2. **Plan-Execute Pattern** 
```
Planning → Step 1 → Step 2 → Step 3 → Review → Adjust
```

### 3. **Tool Selection Pattern**
```
Query Analysis → Tool Selection → Tool Execution → Result Integration
```

### 4. **Multi-Agent Coordination**
```
Agent A ↔ Coordinator ↔ Agent B
              ↕
            Agent C
```

## 💻 Java Agent Implementation

### Base Agent Framework
```java
package com.example.agent.agents;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all agents
 */
public interface Agent {
    
    /**
     * Process a user input and return response
     */
    AgentResponse execute(String input, AgentContext context) throws AgentExecutionException;
    
    /**
     * Execute asynchronously
     */
    default CompletableFuture<AgentResponse> executeAsync(String input, AgentContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(input, context);
            } catch (AgentExecutionException e) {
                return AgentResponse.error(e.getMessage());
            }
        });
    }
    
    /**
     * Get agent name and description
     */
    String getName();
    String getDescription();
    
    /**
     * Get agent capabilities
     */
    List<String> getCapabilities();
    
    /**
     * Initialize agent with configuration
     */
    void initialize(Map<String, Object> config);
    
    /**
     * Clean up agent resources
     */
    void shutdown();
}

/**
 * Agent execution context
 */
public class AgentContext {
    private final String sessionId;
    private final String userId;
    private final Map<String, Object> variables;
    private final ConversationBufferMemory memory;
    private final ToolRegistry toolRegistry;
    
    public AgentContext(String sessionId, String userId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.variables = new HashMap<>();
        this.memory = new ConversationBufferMemory(50);
        this.toolRegistry = null; // Will be injected
    }
    
    public AgentContext(String sessionId, String userId, ConversationBufferMemory memory, 
                       ToolRegistry toolRegistry) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.variables = new HashMap<>();
        this.memory = memory;
        this.toolRegistry = toolRegistry;
    }
    
    // Context manipulation methods
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getVariable(String key, Class<T> type) {
        Object value = variables.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }
    
    // Getters
    public String getSessionId() { return sessionId; }
    public String getUserId() { return userId; }
    public Map<String, Object> getVariables() { return new HashMap<>(variables); }
    public ConversationBufferMemory getMemory() { return memory; }
    public ToolRegistry getToolRegistry() { return toolRegistry; }
}

/**
 * Agent response container
 */
public class AgentResponse {
    private final boolean success;
    private final String response;
    private final String reasoning;
    private final List<AgentAction> actions;
    private final Map<String, Object> metadata;
    private final long executionTimeMs;
    
    public static AgentResponse success(String response) {
        return new AgentResponse(true, response, null, List.of(), Map.of(), 0);
    }
    
    public static AgentResponse success(String response, String reasoning, 
                                      List<AgentAction> actions, long executionTime) {
        return new AgentResponse(true, response, reasoning, actions, Map.of(), executionTime);
    }
    
    public static AgentResponse error(String errorMessage) {
        return new AgentResponse(false, null, null, List.of(), 
                               Map.of("error", errorMessage), 0);
    }
    
    private AgentResponse(boolean success, String response, String reasoning, 
                         List<AgentAction> actions, Map<String, Object> metadata, 
                         long executionTimeMs) {
        this.success = success;
        this.response = response;
        this.reasoning = reasoning;
        this.actions = new ArrayList<>(actions);
        this.metadata = new HashMap<>(metadata);
        this.executionTimeMs = executionTimeMs;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getResponse() { return response; }
    public String getReasoning() { return reasoning; }
    public List<AgentAction> getActions() { return new ArrayList<>(actions); }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public long getExecutionTimeMs() { return executionTimeMs; }
}

/**
 * Represents an action taken by an agent
 */
public class AgentAction {
    private final String actionType;
    private final String tool;
    private final Map<String, Object> parameters;
    private final String reasoning;
    private final Object result;
    private final boolean success;
    
    public AgentAction(String actionType, String tool, Map<String, Object> parameters, 
                      String reasoning, Object result, boolean success) {
        this.actionType = actionType;
        this.tool = tool;
        this.parameters = new HashMap<>(parameters);
        this.reasoning = reasoning;
        this.result = result;
        this.success = success;
    }
    
    // Getters
    public String getActionType() { return actionType; }
    public String getTool() { return tool; }
    public Map<String, Object> getParameters() { return new HashMap<>(parameters); }
    public String getReasoning() { return reasoning; }
    public Object getResult() { return result; }
    public boolean isSuccess() { return success; }
}

/**
 * Agent execution exception
 */
public class AgentExecutionException extends Exception {
    public AgentExecutionException(String message) {
        super(message);
    }
    
    public AgentExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Abstract base agent with common functionality
 */
public abstract class BaseAgent implements Agent {
    
    protected final ChatLanguageModel llm;
    protected final String name;
    protected final String description;
    protected Map<String, Object> config;
    protected boolean initialized = false;
    
    protected BaseAgent(ChatLanguageModel llm, String name, String description) {
        this.llm = llm;
        this.name = name;
        this.description = description;
        this.config = new HashMap<>();
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
    public void initialize(Map<String, Object> config) {
        this.config = new HashMap<>(config);
        this.initialized = true;
        doInitialize();
    }
    
    /**
     * Subclasses override for custom initialization
     */
    protected void doInitialize() {
        // Default: no-op
    }
    
    @Override
    public void shutdown() {
        initialized = false;
        doShutdown();
    }
    
    /**
     * Subclasses override for cleanup
     */
    protected void doShutdown() {
        // Default: no-op
    }
    
    @Override
    public AgentResponse execute(String input, AgentContext context) throws AgentExecutionException {
        if (!initialized) {
            throw new AgentExecutionException("Agent not initialized: " + name);
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Add user input to memory
            context.getMemory().addMessage(UserMessage.from(input));
            
            // Execute agent logic
            AgentResponse response = executeInternal(input, context);
            
            // Add agent response to memory if successful
            if (response.isSuccess() && response.getResponse() != null) {
                context.getMemory().addMessage(AiMessage.from(response.getResponse()));
            }
            
            return response;
            
        } catch (Exception e) {
            throw new AgentExecutionException("Agent execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Subclasses implement the core agent logic
     */
    protected abstract AgentResponse executeInternal(String input, AgentContext context) 
        throws Exception;
    
    /**
     * Helper method to generate LLM response
     */
    protected String generateLLMResponse(String prompt) throws Exception {
        AiMessage response = llm.generate(UserMessage.from(prompt)).content();
        return response.text();
    }
}
```

### ReAct Agent Implementation
```java
/**
 * ReAct (Reasoning and Acting) Agent
 */
public class ReActAgent extends BaseAgent {
    
    private static final int MAX_ITERATIONS = 10;
    private static final String REACT_PROMPT_TEMPLATE = 
        "You are a helpful assistant that can reason step by step and use tools when needed.\n" +
        "You have access to the following tools: {tools}\n\n" +
        "Use the following format:\n" +
        "Thought: I need to think about what the user is asking and plan my approach\n" +
        "Action: [tool_name]\n" +
        "Action Input: [tool_parameters_as_json]\n" +
        "Observation: [tool_result]\n" +
        "... (this Thought/Action/Action Input/Observation can repeat N times)\n" +
        "Thought: I now know the final answer\n" +
        "Final Answer: [your final response to the user]\n\n" +
        "Question: {input}\n" +
        "Conversation History: {history}\n\n";
    
    private final ToolRegistry toolRegistry;
    
    public ReActAgent(ChatLanguageModel llm, ToolRegistry toolRegistry) {
        super(llm, "ReActAgent", "Reasoning and Acting agent with tool usage");
        this.toolRegistry = toolRegistry;
    }
    
    @Override
    public List<String> getCapabilities() {
        List<String> capabilities = new ArrayList<>();
        capabilities.add("Step-by-step reasoning");
        capabilities.add("Tool selection and usage");
        capabilities.add("Iterative problem solving");
        
        if (toolRegistry != null) {
            capabilities.addAll(toolRegistry.getAllTools().stream()
                .map(tool -> "Tool: " + tool.getName())
                .toList());
        }
        
        return capabilities;
    }
    
    @Override
    protected AgentResponse executeInternal(String input, AgentContext context) throws Exception {
        List<AgentAction> actions = new ArrayList<>();
        StringBuilder reasoning = new StringBuilder();
        
        String currentInput = input;
        String conversationHistory = context.getMemory().getFormattedHistory();
        
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Create ReAct prompt
            String prompt = createReActPrompt(currentInput, conversationHistory, actions);
            
            // Generate reasoning and action
            String llmResponse = generateLLMResponse(prompt);
            reasoning.append("Iteration ").append(iteration + 1).append(":\n")
                    .append(llmResponse).append("\n\n");
            
            // Parse response for action
            ReActParsing parsed = parseReActResponse(llmResponse);
            
            if (parsed.isFinalAnswer()) {
                // Agent has reached final answer
                return AgentResponse.success(
                    parsed.getFinalAnswer(),
                    reasoning.toString(),
                    actions,
                    System.currentTimeMillis()
                );
            }
            
            if (parsed.hasAction()) {
                // Execute the specified action
                AgentAction action = executeAction(parsed, context);
                actions.add(action);
                
                // Update current input with observation
                currentInput = "Previous observation: " + action.getResult();
            } else {
                // No clear action, continue reasoning
                currentInput = "Continue reasoning: " + llmResponse;
            }
        }
        
        // Max iterations reached
        return AgentResponse.success(
            "I've been working on your request but need more time to complete it properly. " +
            "Here's my current progress:\n" + reasoning.toString(),
            reasoning.toString(),
            actions,
            System.currentTimeMillis()
        );
    }
    
    private String createReActPrompt(String input, String history, List<AgentAction> actions) {
        // Get available tools description
        String toolsDescription = toolRegistry.getAllTools().stream()
            .map(tool -> String.format("- %s: %s", tool.getName(), tool.getDescription()))
            .collect(Collectors.joining("\n"));
        
        // Add previous actions to context
        StringBuilder actionsContext = new StringBuilder();
        for (AgentAction action : actions) {
            actionsContext.append("Previous Action: ").append(action.getTool())
                         .append(" with parameters: ").append(action.getParameters())
                         .append("\nResult: ").append(action.getResult()).append("\n");
        }
        
        return REACT_PROMPT_TEMPLATE
            .replace("{tools}", toolsDescription)
            .replace("{input}", input)
            .replace("{history}", history + "\n" + actionsContext.toString());
    }
    
    private ReActParsing parseReActResponse(String response) {
        // Simple parsing logic - in production, use more robust parsing
        String[] lines = response.split("\n");
        
        String thought = null;
        String action = null;
        String actionInput = null;
        String finalAnswer = null;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("Thought:")) {
                thought = trimmed.substring(8).trim();
            } else if (trimmed.startsWith("Action:")) {
                action = trimmed.substring(7).trim();
            } else if (trimmed.startsWith("Action Input:")) {
                actionInput = trimmed.substring(13).trim();
            } else if (trimmed.startsWith("Final Answer:")) {
                finalAnswer = trimmed.substring(13).trim();
            }
        }
        
        return new ReActParsing(thought, action, actionInput, finalAnswer);
    }
    
    private AgentAction executeAction(ReActParsing parsed, AgentContext context) {
        try {
            String toolName = parsed.getAction();
            String actionInput = parsed.getActionInput();
            
            Optional<AgentTool> toolOpt = toolRegistry.getTool(toolName);
            if (toolOpt.isEmpty()) {
                return new AgentAction("tool_call", toolName, Map.of(), 
                                     "Tool not found", "Tool '" + toolName + "' not available", false);
            }
            
            // Parse action input as JSON parameters
            Map<String, Object> parameters = parseActionInput(actionInput);
            
            // Execute tool
            ToolResult result = toolOpt.get().execute(parameters);
            
            return new AgentAction("tool_call", toolName, parameters, 
                                 parsed.getThought(), result.getResult(), result.isSuccess());
            
        } catch (Exception e) {
            return new AgentAction("tool_call", parsed.getAction(), Map.of(), 
                                 parsed.getThought(), "Error: " + e.getMessage(), false);
        }
    }
    
    private Map<String, Object> parseActionInput(String actionInput) {
        try {
            // Try to parse as JSON
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(actionInput, Map.class);
        } catch (Exception e) {
            // Fallback to simple key-value parsing
            Map<String, Object> params = new HashMap<>();
            if (actionInput != null && !actionInput.trim().isEmpty()) {
                params.put("input", actionInput);
            }
            return params;
        }
    }
}

/**
 * ReAct response parsing result
 */
class ReActParsing {
    private final String thought;
    private final String action;
    private final String actionInput;
    private final String finalAnswer;
    
    public ReActParsing(String thought, String action, String actionInput, String finalAnswer) {
        this.thought = thought;
        this.action = action;
        this.actionInput = actionInput;
        this.finalAnswer = finalAnswer;
    }
    
    public boolean hasAction() {
        return action != null && !action.trim().isEmpty();
    }
    
    public boolean isFinalAnswer() {
        return finalAnswer != null && !finalAnswer.trim().isEmpty();
    }
    
    // Getters
    public String getThought() { return thought; }
    public String getAction() { return action; }
    public String getActionInput() { return actionInput; }
    public String getFinalAnswer() { return finalAnswer; }
}
```

### Plan-Execute Agent Implementation
```java
/**
 * Plan-and-Execute Agent
 */
public class PlanExecuteAgent extends BaseAgent {
    
    private static final String PLANNING_PROMPT = 
        "Given the following objective, create a step-by-step plan to achieve it.\n" +
        "Available tools: {tools}\n\n" +
        "Objective: {objective}\n\n" +
        "Create a numbered plan with specific steps. Each step should:\n" +
        "1. Be specific and actionable\n" +
        "2. Indicate which tool to use (if any)\n" +
        "3. Build upon previous steps\n\n" +
        "Plan:";
    
    private static final String EXECUTION_PROMPT = 
        "Execute this step of the plan:\n" +
        "Step: {step}\n" +
        "Available tools: {tools}\n" +
        "Previous results: {previous_results}\n\n" +
        "If you need to use a tool, specify:\n" +
        "Tool: [tool_name]\n" +
        "Parameters: [parameters_as_json]\n\n" +
        "Otherwise, provide the step result directly.";
    
    private final ToolRegistry toolRegistry;
    
    public PlanExecuteAgent(ChatLanguageModel llm, ToolRegistry toolRegistry) {
        super(llm, "PlanExecuteAgent", "Plan first, then execute step by step");
        this.toolRegistry = toolRegistry;
    }
    
    @Override
    public List<String> getCapabilities() {
        return List.of(
            "Strategic planning",
            "Step-by-step execution", 
            "Progress tracking",
            "Plan adaptation"
        );
    }
    
    @Override
    protected AgentResponse executeInternal(String input, AgentContext context) throws Exception {
        List<AgentAction> actions = new ArrayList<>();
        StringBuilder reasoning = new StringBuilder();
        
        // Phase 1: Planning
        reasoning.append("=== PLANNING PHASE ===\n");
        List<String> plan = createPlan(input);
        reasoning.append("Created plan with ").append(plan.size()).append(" steps:\n");
        for (int i = 0; i < plan.size(); i++) {
            reasoning.append(i + 1).append(". ").append(plan.get(i)).append("\n");
        }
        reasoning.append("\n");
        
        // Phase 2: Execution
        reasoning.append("=== EXECUTION PHASE ===\n");
        List<String> stepResults = new ArrayList<>();
        
        for (int i = 0; i < plan.size(); i++) {
            String step = plan.get(i);
            reasoning.append("Executing step ").append(i + 1).append(": ").append(step).append("\n");
            
            try {
                ExecutionResult result = executeStep(step, stepResults);
                stepResults.add(result.getResult());
                
                if (result.getAction() != null) {
                    actions.add(result.getAction());
                }
                
                reasoning.append("Result: ").append(result.getResult()).append("\n\n");
                
            } catch (Exception e) {
                reasoning.append("Error: ").append(e.getMessage()).append("\n");
                stepResults.add("Failed: " + e.getMessage());
            }
        }
        
        // Phase 3: Synthesis
        reasoning.append("=== SYNTHESIS PHASE ===\n");
        String finalResponse = synthesizeResults(input, stepResults);
        reasoning.append("Final synthesis completed.\n");
        
        return AgentResponse.success(finalResponse, reasoning.toString(), actions, 
                                   System.currentTimeMillis());
    }
    
    private List<String> createPlan(String objective) throws Exception {
        String toolsDescription = toolRegistry.getAllTools().stream()
            .map(tool -> String.format("- %s: %s", tool.getName(), tool.getDescription()))
            .collect(Collectors.joining("\n"));
        
        String planningPrompt = PLANNING_PROMPT
            .replace("{objective}", objective)
            .replace("{tools}", toolsDescription);
        
        String planResponse = generateLLMResponse(planningPrompt);
        
        // Parse plan into steps
        List<String> steps = new ArrayList<>();
        String[] lines = planResponse.split("\n");
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("\\d+\\..*")) {
                // Extract step text (remove number and dot)
                String step = trimmed.replaceFirst("\\d+\\.", "").trim();
                if (!step.isEmpty()) {
                    steps.add(step);
                }
            }
        }
        
        return steps;
    }
    
    private ExecutionResult executeStep(String step, List<String> previousResults) throws Exception {
        String toolsDescription = toolRegistry.getAllTools().stream()
            .map(tool -> String.format("- %s: %s", tool.getName(), tool.getDescription()))
            .collect(Collectors.joining("\n"));
        
        String previousResultsText = String.join("\n", previousResults);
        
        String executionPrompt = EXECUTION_PROMPT
            .replace("{step}", step)
            .replace("{tools}", toolsDescription)
            .replace("{previous_results}", previousResultsText);
        
        String executionResponse = generateLLMResponse(executionPrompt);
        
        // Check if response indicates tool usage
        if (executionResponse.contains("Tool:")) {
            return executeStepWithTool(executionResponse, step);
        } else {
            // Direct execution result
            return new ExecutionResult(executionResponse, null);
        }
    }
    
    private ExecutionResult executeStepWithTool(String response, String step) throws Exception {
        // Parse tool information from response
        String[] lines = response.split("\n");
        String toolName = null;
        String parametersJson = null;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("Tool:")) {
                toolName = trimmed.substring(5).trim();
            } else if (trimmed.startsWith("Parameters:")) {
                parametersJson = trimmed.substring(11).trim();
            }
        }
        
        if (toolName == null) {
            return new ExecutionResult("No clear tool specified in response", null);
        }
        
        Optional<AgentTool> toolOpt = toolRegistry.getTool(toolName);
        if (toolOpt.isEmpty()) {
            return new ExecutionResult("Tool not found: " + toolName, null);
        }
        
        // Parse parameters
        Map<String, Object> parameters = new HashMap<>();
        if (parametersJson != null && !parametersJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                parameters = mapper.readValue(parametersJson, Map.class);
            } catch (Exception e) {
                parameters.put("input", parametersJson);
            }
        }
        
        // Execute tool
        ToolResult toolResult = toolOpt.get().execute(parameters);
        
        AgentAction action = new AgentAction("step_execution", toolName, parameters, 
                                           step, toolResult.getResult(), toolResult.isSuccess());
        
        String result = toolResult.isSuccess() ? 
            toolResult.getResult().toString() : 
            "Tool execution failed: " + toolResult.getErrorMessage();
        
        return new ExecutionResult(result, action);
    }
    
    private String synthesizeResults(String originalObjective, List<String> stepResults) throws Exception {
        StringBuilder synthesis = new StringBuilder();
        synthesis.append("Based on the step-by-step execution:\n\n");
        
        for (int i = 0; i < stepResults.size(); i++) {
            synthesis.append("Step ").append(i + 1).append(": ").append(stepResults.get(i)).append("\n");
        }
        
        String synthesisPrompt = String.format(
            "Original objective: %s\n\nExecution results:\n%s\n\n" +
            "Provide a comprehensive final answer that addresses the original objective:",
            originalObjective, synthesis.toString()
        );
        
        return generateLLMResponse(synthesisPrompt);
    }
}

/**
 * Execution result container
 */
class ExecutionResult {
    private final String result;
    private final AgentAction action;
    
    public ExecutionResult(String result, AgentAction action) {
        this.result = result;
        this.action = action;
    }
    
    public String getResult() { return result; }
    public AgentAction getAction() { return action; }
}
```

### Conversational Agent Implementation
```java
/**
 * Specialized conversational agent with personality and context
 */
public class ConversationalAgent extends BaseAgent {
    
    private final String personality;
    private final Map<String, String> conversationTemplates;
    
    public ConversationalAgent(ChatLanguageModel llm, String personality) {
        super(llm, "ConversationalAgent", "Personality-driven conversational agent");
        this.personality = personality;
        this.conversationTemplates = initializeTemplates();
    }
    
    @Override
    public List<String> getCapabilities() {
        return List.of(
            "Natural conversation",
            "Personality-driven responses",
            "Context awareness", 
            "Emotional intelligence",
            "Topic management"
        );
    }
    
    @Override
    protected AgentResponse executeInternal(String input, AgentContext context) throws Exception {
        // Analyze conversation context
        ConversationAnalysis analysis = analyzeConversation(input, context);
        
        // Select appropriate response template
        String template = selectResponseTemplate(analysis);
        
        // Generate personalized response
        String response = generatePersonalizedResponse(input, context, template, analysis);
        
        // Update conversation metadata
        updateConversationMetadata(context, analysis);
        
        return AgentResponse.success(response, "Conversational response with " + personality + " personality", 
                                   List.of(), System.currentTimeMillis());
    }
    
    private ConversationAnalysis analyzeConversation(String input, AgentContext context) throws Exception {
        String history = context.getMemory().getFormattedHistory();
        
        String analysisPrompt = String.format(
            "Analyze this conversation:\n" +
            "History: %s\n" +
            "Current input: %s\n\n" +
            "Provide analysis in this format:\n" +
            "Topic: [main topic]\n" +
            "Sentiment: [positive/negative/neutral]\n" +
            "Intent: [question/request/chat/complaint/compliment]\n" +
            "Context_needed: [yes/no]\n" +
            "Urgency: [high/medium/low]",
            history, input
        );
        
        String analysisResponse = generateLLMResponse(analysisPrompt);
        return parseConversationAnalysis(analysisResponse);
    }
    
    private ConversationAnalysis parseConversationAnalysis(String response) {
        Map<String, String> analysis = new HashMap<>();
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    analysis.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        }
        
        return new ConversationAnalysis(
            analysis.getOrDefault("topic", "general"),
            analysis.getOrDefault("sentiment", "neutral"), 
            analysis.getOrDefault("intent", "chat"),
            "yes".equals(analysis.get("context_needed")),
            analysis.getOrDefault("urgency", "medium")
        );
    }
    
    private String selectResponseTemplate(ConversationAnalysis analysis) {
        String key = analysis.getIntent() + "_" + analysis.getSentiment();
        return conversationTemplates.getOrDefault(key, conversationTemplates.get("default"));
    }
    
    private String generatePersonalizedResponse(String input, AgentContext context, 
                                              String template, ConversationAnalysis analysis) throws Exception {
        
        String personalityContext = "You have this personality: " + personality + "\n";
        String conversationContext = context.getMemory().getFormattedHistory();
        String analysisContext = String.format(
            "Conversation analysis - Topic: %s, Sentiment: %s, Intent: %s",
            analysis.getTopic(), analysis.getSentiment(), analysis.getIntent()
        );
        
        String fullPrompt = String.format(
            "%s\n%s\n%s\n\nTemplate: %s\n\nUser input: %s\n\n" +
            "Generate a natural, personality-appropriate response:",
            personalityContext, conversationContext, analysisContext, template, input
        );
        
        return generateLLMResponse(fullPrompt);
    }
    
    private void updateConversationMetadata(AgentContext context, ConversationAnalysis analysis) {
        context.setVariable("last_topic", analysis.getTopic());
        context.setVariable("last_sentiment", analysis.getSentiment());
        context.setVariable("last_intent", analysis.getIntent());
        context.setVariable("conversation_turns", 
            context.getVariable("conversation_turns", Integer.class).orElse(0) + 1);
    }
    
    private Map<String, String> initializeTemplates() {
        Map<String, String> templates = new HashMap<>();
        
        templates.put("question_neutral", "I'd be happy to help you with that question.");
        templates.put("question_positive", "Great question! I'm excited to help you with this.");
        templates.put("question_negative", "I understand this might be frustrating. Let me help you work through this.");
        
        templates.put("request_neutral", "I'll do my best to assist with your request.");
        templates.put("request_positive", "Absolutely! I'd be delighted to help with that.");
        templates.put("request_negative", "I hear your concern and will try to address it.");
        
        templates.put("chat_neutral", "Thanks for sharing that with me.");
        templates.put("chat_positive", "That's wonderful! I enjoy our conversation.");
        templates.put("chat_negative", "I'm sorry to hear that. Is there anything I can do to help?");
        
        templates.put("complaint_negative", "I sincerely apologize for any inconvenience. Let me see how I can help resolve this.");
        templates.put("compliment_positive", "Thank you so much! That really means a lot to me.");
        
        templates.put("default", "I appreciate you reaching out. How can I assist you today?");
        
        return templates;
    }
}

/**
 * Conversation analysis result
 */
class ConversationAnalysis {
    private final String topic;
    private final String sentiment;
    private final String intent;
    private final boolean contextNeeded;
    private final String urgency;
    
    public ConversationAnalysis(String topic, String sentiment, String intent, 
                               boolean contextNeeded, String urgency) {
        this.topic = topic;
        this.sentiment = sentiment;
        this.intent = intent;
        this.contextNeeded = contextNeeded;
        this.urgency = urgency;
    }
    
    // Getters
    public String getTopic() { return topic; }
    public String getSentiment() { return sentiment; }
    public String getIntent() { return intent; }
    public boolean isContextNeeded() { return contextNeeded; }
    public String getUrgency() { return urgency; }
}
```

## 🚀 Best Practices

1. **Agent Design**
   - Define clear agent purposes and capabilities
   - Implement proper reasoning loops
   - Handle edge cases gracefully
   - Provide transparent decision-making

2. **Tool Integration**
   - Validate tool availability before use
   - Handle tool failures gracefully
   - Provide meaningful error messages
   - Log tool usage for debugging

3. **Memory Management**
   - Use appropriate memory strategies
   - Clean up old conversations
   - Implement context prioritization
   - Handle memory limits properly

4. **Performance**
   - Set reasonable iteration limits
   - Implement timeouts for tool calls
   - Use caching where appropriate
   - Monitor agent execution times

5. **Safety and Ethics**
   - Implement content filtering
   - Validate user inputs
   - Respect user privacy
   - Provide opt-out mechanisms

## 🔗 Integration with Other Components

Agents integrate with:
- **Tools**: Agents use tools to extend capabilities
- **Memory**: Agents maintain conversation context
- **Chains**: Agents can use chains for complex workflows
- **State Management**: Agents can trigger state changes

---

*Next: [Prompt Templates](../prompts/) - Learn about structured prompt creation and optimization.*
