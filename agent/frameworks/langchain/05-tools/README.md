# Tools Integration in LangChain (Java)

## 🎯 Overview
Tools integration in LangChain enables agents to extend their capabilities by calling external functions, APIs, and services. This allows agents to perform actions beyond text generation, such as web searches, calculations, database queries, and API calls.

## 🧠 Core Tool Concepts

### What are Tools?
Tools are external capabilities that agents can invoke to:
- **Retrieve Information**: Web search, database queries, API calls
- **Perform Calculations**: Mathematical operations, data analysis
- **Execute Actions**: Send emails, create files, update systems
- **Process Data**: File manipulation, format conversion

### Tool Components
1. **Tool Definition**: Description and parameters
2. **Tool Execution**: Implementation logic
3. **Tool Selection**: Agent decides which tool to use
4. **Result Processing**: Handling tool outputs
5. **Error Handling**: Managing failures gracefully

## 🏗️ Tool Architecture Patterns

### 1. **Simple Function Tools**
```
User Query → Agent Analysis → Tool Selection → Tool Execution → Response
```

### 2. **Multi-Tool Workflows**
```
Query → Tool 1 → Process Result → Tool 2 → Combine Results → Response
```

### 3. **Conditional Tool Usage**
```
Query → Analysis → If(condition) → Tool A, Else → Tool B → Response
```

### 4. **Tool Chaining**
```
Query → Tool A → Result A → Tool B(using Result A) → Final Result
```

## 💻 Java Tool Implementation

### Basic Tool Interface
```java
package com.example.agent.tools;

import java.util.Map;
import java.util.Optional;

/**
 * Base interface for all tools
 */
public interface AgentTool {
    
    /**
     * Get tool name for identification
     */
    String getName();
    
    /**
     * Get tool description for agent decision making
     */
    String getDescription();
    
    /**
     * Get tool parameters schema
     */
    Map<String, ToolParameter> getParameters();
    
    /**
     * Execute the tool with given parameters
     */
    ToolResult execute(Map<String, Object> parameters) throws ToolExecutionException;
    
    /**
     * Validate parameters before execution
     */
    default boolean validateParameters(Map<String, Object> parameters) {
        for (Map.Entry<String, ToolParameter> param : getParameters().entrySet()) {
            if (param.getValue().isRequired() && !parameters.containsKey(param.getKey())) {
                return false;
            }
        }
        return true;
    }
}

/**
 * Tool parameter definition
 */
public class ToolParameter {
    private final String name;
    private final String type;
    private final String description;
    private final boolean required;
    private final Object defaultValue;
    
    public ToolParameter(String name, String type, String description, boolean required) {
        this(name, type, description, required, null);
    }
    
    public ToolParameter(String name, String type, String description, 
                        boolean required, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
    }
    
    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public boolean isRequired() { return required; }
    public Object getDefaultValue() { return defaultValue; }
}

/**
 * Tool execution result
 */
public class ToolResult {
    private final boolean success;
    private final Object result;
    private final String errorMessage;
    private final Map<String, Object> metadata;
    
    public static ToolResult success(Object result) {
        return new ToolResult(true, result, null, Map.of());
    }
    
    public static ToolResult success(Object result, Map<String, Object> metadata) {
        return new ToolResult(true, result, null, metadata);
    }
    
    public static ToolResult failure(String errorMessage) {
        return new ToolResult(false, null, errorMessage, Map.of());
    }
    
    private ToolResult(boolean success, Object result, String errorMessage, 
                      Map<String, Object> metadata) {
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
        this.metadata = metadata;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public Object getResult() { return result; }
    public String getErrorMessage() { return errorMessage; }
    public Map<String, Object> getMetadata() { return metadata; }
}

/**
 * Tool execution exception
 */
public class ToolExecutionException extends Exception {
    public ToolExecutionException(String message) {
        super(message);
    }
    
    public ToolExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Built-in Tools Implementation
```java
/**
 * Web search tool using DuckDuckGo
 */
@Component
public class WebSearchTool implements AgentTool {
    
    private final WebClient webClient;
    
    public WebSearchTool() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.duckduckgo.com")
            .build();
    }
    
    @Override
    public String getName() {
        return "web_search";
    }
    
    @Override
    public String getDescription() {
        return "Search the web for current information on any topic. Use this when you need up-to-date information.";
    }
    
    @Override
    public Map<String, ToolParameter> getParameters() {
        return Map.of(
            "query", new ToolParameter("query", "string", "Search query", true),
            "max_results", new ToolParameter("max_results", "integer", "Maximum number of results", false, 5)
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters) throws ToolExecutionException {
        try {
            String query = (String) parameters.get("query");
            int maxResults = (Integer) parameters.getOrDefault("max_results", 5);
            
            if (query == null || query.trim().isEmpty()) {
                return ToolResult.failure("Search query is required");
            }
            
            // Perform web search
            String searchUrl = "/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&format=json";
            
            String response = webClient.get()
                .uri(searchUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            // Parse and format results
            List<SearchResult> results = parseSearchResults(response, maxResults);
            
            Map<String, Object> metadata = Map.of(
                "query", query,
                "results_count", results.size(),
                "search_time", System.currentTimeMillis()
            );
            
            return ToolResult.success(formatSearchResults(results), metadata);
            
        } catch (Exception e) {
            throw new ToolExecutionException("Web search failed: " + e.getMessage(), e);
        }
    }
    
    private List<SearchResult> parseSearchResults(String response, int maxResults) {
        // Implementation to parse DuckDuckGo response
        // This is a simplified version
        List<SearchResult> results = new ArrayList<>();
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode relatedTopics = root.get("RelatedTopics");
            
            if (relatedTopics != null && relatedTopics.isArray()) {
                int count = 0;
                for (JsonNode topic : relatedTopics) {
                    if (count >= maxResults) break;
                    
                    String text = topic.get("Text").asText();
                    String url = topic.get("FirstURL").asText();
                    
                    results.add(new SearchResult(text, url));
                    count++;
                }
            }
        } catch (Exception e) {
            // Fallback to mock results if parsing fails
            results.add(new SearchResult("Search result for: " + response, "https://example.com"));
        }
        
        return results;
    }
    
    private String formatSearchResults(List<SearchResult> results) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("Search Results:\n");
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            formatted.append(String.format("%d. %s\n   URL: %s\n", 
                i + 1, result.getTitle(), result.getUrl()));
        }
        
        return formatted.toString();
    }
}

class SearchResult {
    private final String title;
    private final String url;
    
    public SearchResult(String title, String url) {
        this.title = title;
        this.url = url;
    }
    
    public String getTitle() { return title; }
    public String getUrl() { return url; }
}

/**
 * Calculator tool for mathematical operations
 */
@Component
public class CalculatorTool implements AgentTool {
    
    @Override
    public String getName() {
        return "calculator";
    }
    
    @Override
    public String getDescription() {
        return "Perform mathematical calculations. Supports basic arithmetic, trigonometry, and more complex operations.";
    }
    
    @Override
    public Map<String, ToolParameter> getParameters() {
        return Map.of(
            "expression", new ToolParameter("expression", "string", "Mathematical expression to evaluate", true)
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters) throws ToolExecutionException {
        try {
            String expression = (String) parameters.get("expression");
            
            if (expression == null || expression.trim().isEmpty()) {
                return ToolResult.failure("Mathematical expression is required");
            }
            
            // Clean and validate expression
            String cleanExpression = sanitizeExpression(expression);
            
            // Use ScriptEngine for safe evaluation
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            
            Object result = engine.eval(cleanExpression);
            
            Map<String, Object> metadata = Map.of(
                "expression", expression,
                "cleaned_expression", cleanExpression,
                "calculation_time", System.currentTimeMillis()
            );
            
            return ToolResult.success(result.toString(), metadata);
            
        } catch (Exception e) {
            throw new ToolExecutionException("Calculation failed: " + e.getMessage(), e);
        }
    }
    
    private String sanitizeExpression(String expression) {
        // Remove potential dangerous characters and functions
        String clean = expression.replaceAll("[^0-9+\\-*/().\\s]", "");
        
        // Basic validation
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Invalid mathematical expression");
        }
        
        return clean;
    }
}

/**
 * File operations tool
 */
@Component
public class FileOperationsTool implements AgentTool {
    
    private final String workingDirectory;
    
    public FileOperationsTool() {
        this.workingDirectory = System.getProperty("user.dir") + "/temp/";
        createWorkingDirectory();
    }
    
    @Override
    public String getName() {
        return "file_operations";
    }
    
    @Override
    public String getDescription() {
        return "Perform file operations like read, write, create, and list files in a safe working directory.";
    }
    
    @Override
    public Map<String, ToolParameter> getParameters() {
        return Map.of(
            "operation", new ToolParameter("operation", "string", "Operation: read, write, create, list, delete", true),
            "filename", new ToolParameter("filename", "string", "Name of the file", false),
            "content", new ToolParameter("content", "string", "Content for write/create operations", false)
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters) throws ToolExecutionException {
        try {
            String operation = (String) parameters.get("operation");
            String filename = (String) parameters.get("filename");
            String content = (String) parameters.get("content");
            
            switch (operation.toLowerCase()) {
                case "list":
                    return listFiles();
                case "read":
                    return readFile(filename);
                case "write":
                case "create":
                    return writeFile(filename, content);
                case "delete":
                    return deleteFile(filename);
                default:
                    return ToolResult.failure("Unsupported operation: " + operation);
            }
            
        } catch (Exception e) {
            throw new ToolExecutionException("File operation failed: " + e.getMessage(), e);
        }
    }
    
    private ToolResult listFiles() {
        try {
            File dir = new File(workingDirectory);
            File[] files = dir.listFiles();
            
            if (files == null) {
                return ToolResult.success("Directory is empty or does not exist");
            }
            
            StringBuilder fileList = new StringBuilder("Files in working directory:\n");
            for (File file : files) {
                fileList.append(String.format("- %s (%s bytes)\n", 
                    file.getName(), file.length()));
            }
            
            return ToolResult.success(fileList.toString());
        } catch (Exception e) {
            return ToolResult.failure("Failed to list files: " + e.getMessage());
        }
    }
    
    private ToolResult readFile(String filename) {
        try {
            if (filename == null) {
                return ToolResult.failure("Filename is required for read operation");
            }
            
            Path filePath = Paths.get(workingDirectory, filename);
            
            if (!Files.exists(filePath)) {
                return ToolResult.failure("File does not exist: " + filename);
            }
            
            String content = Files.readString(filePath);
            
            Map<String, Object> metadata = Map.of(
                "filename", filename,
                "size", Files.size(filePath),
                "last_modified", Files.getLastModifiedTime(filePath).toString()
            );
            
            return ToolResult.success(content, metadata);
        } catch (Exception e) {
            return ToolResult.failure("Failed to read file: " + e.getMessage());
        }
    }
    
    private ToolResult writeFile(String filename, String content) {
        try {
            if (filename == null) {
                return ToolResult.failure("Filename is required for write operation");
            }
            
            if (content == null) {
                content = "";
            }
            
            Path filePath = Paths.get(workingDirectory, filename);
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            Map<String, Object> metadata = Map.of(
                "filename", filename,
                "size", content.length(),
                "created_at", System.currentTimeMillis()
            );
            
            return ToolResult.success("File created successfully: " + filename, metadata);
        } catch (Exception e) {
            return ToolResult.failure("Failed to write file: " + e.getMessage());
        }
    }
    
    private ToolResult deleteFile(String filename) {
        try {
            if (filename == null) {
                return ToolResult.failure("Filename is required for delete operation");
            }
            
            Path filePath = Paths.get(workingDirectory, filename);
            
            if (!Files.exists(filePath)) {
                return ToolResult.failure("File does not exist: " + filename);
            }
            
            Files.delete(filePath);
            return ToolResult.success("File deleted successfully: " + filename);
        } catch (Exception e) {
            return ToolResult.failure("Failed to delete file: " + e.getMessage());
        }
    }
    
    private void createWorkingDirectory() {
        try {
            Files.createDirectories(Paths.get(workingDirectory));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create working directory", e);
        }
    }
}
```

### Tool Manager and Registry
```java
/**
 * Tool registry for managing available tools
 */
@Service
public class ToolRegistry {
    
    private final Map<String, AgentTool> tools;
    private final ChatLanguageModel llm;
    
    public ToolRegistry(List<AgentTool> availableTools, ChatLanguageModel llm) {
        this.llm = llm;
        this.tools = availableTools.stream()
            .collect(Collectors.toMap(AgentTool::getName, Function.identity()));
    }
    
    /**
     * Get tool by name
     */
    public Optional<AgentTool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }
    
    /**
     * Get all available tools
     */
    public Collection<AgentTool> getAllTools() {
        return tools.values();
    }
    
    /**
     * Select appropriate tool for a given task
     */
    public Optional<AgentTool> selectTool(String userQuery) {
        StringBuilder toolDescriptions = new StringBuilder();
        toolDescriptions.append("Available tools:\n");
        
        for (AgentTool tool : tools.values()) {
            toolDescriptions.append(String.format("- %s: %s\n", 
                tool.getName(), tool.getDescription()));
        }
        
        String prompt = String.format(
            "Given the user query: '%s'\n\n%s\n" +
            "Which tool would be most appropriate? Respond with just the tool name, or 'none' if no tool is needed.",
            userQuery, toolDescriptions.toString()
        );
        
        try {
            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
            String toolName = response.text().trim().toLowerCase();
            
            return getTool(toolName);
        } catch (Exception e) {
            System.err.println("Tool selection failed: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Extract parameters for tool execution
     */
    public Map<String, Object> extractParameters(AgentTool tool, String userQuery) {
        Map<String, ToolParameter> paramSpec = tool.getParameters();
        
        StringBuilder paramPrompt = new StringBuilder();
        paramPrompt.append("Extract parameters for the following tool call:\n");
        paramPrompt.append("Tool: ").append(tool.getName()).append("\n");
        paramPrompt.append("User Query: ").append(userQuery).append("\n\n");
        paramPrompt.append("Parameters needed:\n");
        
        for (ToolParameter param : paramSpec.values()) {
            paramPrompt.append(String.format("- %s (%s): %s %s\n",
                param.getName(), param.getType(), param.getDescription(),
                param.isRequired() ? "[REQUIRED]" : "[OPTIONAL]"));
        }
        
        paramPrompt.append("\nReturn parameters as JSON format.");
        
        try {
            AiMessage response = llm.generate(UserMessage.from(paramPrompt.toString())).content();
            
            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.text(), Map.class);
            
        } catch (Exception e) {
            System.err.println("Parameter extraction failed: " + e.getMessage());
            return Map.of();
        }
    }
    
    /**
     * Execute tool with automatic parameter extraction
     */
    public ToolResult executeWithQuery(String userQuery) {
        try {
            // Select appropriate tool
            Optional<AgentTool> toolOpt = selectTool(userQuery);
            if (toolOpt.isEmpty()) {
                return ToolResult.failure("No appropriate tool found for the query");
            }
            
            AgentTool tool = toolOpt.get();
            
            // Extract parameters
            Map<String, Object> parameters = extractParameters(tool, userQuery);
            
            // Validate parameters
            if (!tool.validateParameters(parameters)) {
                return ToolResult.failure("Invalid parameters for tool: " + tool.getName());
            }
            
            // Execute tool
            return tool.execute(parameters);
            
        } catch (Exception e) {
            return ToolResult.failure("Tool execution failed: " + e.getMessage());
        }
    }
}

/**
 * Agent with tool integration
 */
@Service
public class ToolUsingAgent {
    
    private final ChatLanguageModel llm;
    private final ToolRegistry toolRegistry;
    private final ConversationBufferMemory memory;
    
    public ToolUsingAgent(ChatLanguageModel llm, ToolRegistry toolRegistry) {
        this.llm = llm;
        this.toolRegistry = toolRegistry;
        this.memory = new ConversationBufferMemory(20);
    }
    
    public String processQuery(String userQuery) {
        try {
            memory.addMessage(UserMessage.from(userQuery));
            
            // Determine if tool usage is needed
            if (requiresToolUsage(userQuery)) {
                return processWithTools(userQuery);
            } else {
                return processWithoutTools(userQuery);
            }
            
        } catch (Exception e) {
            return "I encountered an error while processing your request: " + e.getMessage();
        }
    }
    
    private boolean requiresToolUsage(String query) {
        String analysisPrompt = String.format(
            "Does this user query require external tools or can it be answered with general knowledge? " +
            "Query: '%s'\n" +
            "Respond with 'TOOL_REQUIRED' or 'NO_TOOL_NEEDED'", query
        );
        
        try {
            AiMessage response = llm.generate(UserMessage.from(analysisPrompt)).content();
            return response.text().contains("TOOL_REQUIRED");
        } catch (Exception e) {
            return false; // Default to no tools if analysis fails
        }
    }
    
    private String processWithTools(String userQuery) {
        try {
            // Execute tool
            ToolResult toolResult = toolRegistry.executeWithQuery(userQuery);
            
            // Generate response incorporating tool result
            String responsePrompt;
            if (toolResult.isSuccess()) {
                responsePrompt = String.format(
                    "User asked: '%s'\n" +
                    "Tool result: %s\n" +
                    "Provide a helpful response incorporating this information:",
                    userQuery, toolResult.getResult()
                );
            } else {
                responsePrompt = String.format(
                    "User asked: '%s'\n" +
                    "Tool execution failed: %s\n" +
                    "Provide a helpful response explaining the issue and suggesting alternatives:",
                    userQuery, toolResult.getErrorMessage()
                );
            }
            
            AiMessage response = llm.generate(UserMessage.from(responsePrompt)).content();
            
            memory.addMessage(response);
            return response.text();
            
        } catch (Exception e) {
            String errorResponse = "I tried to use tools to help with your request, but encountered an error: " + 
                e.getMessage();
            memory.addMessage(AiMessage.from(errorResponse));
            return errorResponse;
        }
    }
    
    private String processWithoutTools(String userQuery) {
        try {
            // Create context with conversation history
            String context = memory.getFormattedHistory();
            
            String prompt = String.format(
                "Conversation history:\n%s\n\nUser: %s\n\nAssistant:",
                context, userQuery
            );
            
            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
            
            memory.addMessage(response);
            return response.text();
            
        } catch (Exception e) {
            String errorResponse = "I encountered an error while processing your request: " + 
                e.getMessage();
            memory.addMessage(AiMessage.from(errorResponse));
            return errorResponse;
        }
    }
}
```

## 🔄 Advanced Tool Patterns

### Tool Chaining
```java
/**
 * Chain multiple tools for complex operations
 */
@Service
public class ToolChain {
    
    private final ToolRegistry toolRegistry;
    private final ChatLanguageModel llm;
    
    public ToolChain(ToolRegistry toolRegistry, ChatLanguageModel llm) {
        this.toolRegistry = toolRegistry;
        this.llm = llm;
    }
    
    public String executeChain(String userQuery, List<String> toolNames) {
        StringBuilder results = new StringBuilder();
        String currentInput = userQuery;
        
        for (String toolName : toolNames) {
            Optional<AgentTool> toolOpt = toolRegistry.getTool(toolName);
            if (toolOpt.isEmpty()) {
                return "Tool not found: " + toolName;
            }
            
            AgentTool tool = toolOpt.get();
            Map<String, Object> parameters = toolRegistry.extractParameters(tool, currentInput);
            
            try {
                ToolResult result = tool.execute(parameters);
                if (result.isSuccess()) {
                    results.append("Step ").append(toolNames.indexOf(toolName) + 1)
                          .append(" (").append(toolName).append("): ")
                          .append(result.getResult()).append("\n");
                    currentInput = result.getResult().toString();
                } else {
                    return "Tool chain failed at " + toolName + ": " + result.getErrorMessage();
                }
            } catch (Exception e) {
                return "Tool chain failed at " + toolName + ": " + e.getMessage();
            }
        }
        
        // Generate final response
        String finalPrompt = String.format(
            "Based on this tool chain execution:\n%s\n" +
            "Provide a comprehensive answer to the original query: '%s'",
            results.toString(), userQuery
        );
        
        try {
            AiMessage response = llm.generate(UserMessage.from(finalPrompt)).content();
            return response.text();
        } catch (Exception e) {
            return results.toString();
        }
    }
}
```

### Custom API Integration Tool
```java
/**
 * Generic API integration tool
 */
@Component
public class APIIntegrationTool implements AgentTool {
    
    private final WebClient webClient;
    private final Map<String, APIEndpoint> endpoints;
    
    public APIIntegrationTool() {
        this.webClient = WebClient.builder().build();
        this.endpoints = initializeEndpoints();
    }
    
    @Override
    public String getName() {
        return "api_integration";
    }
    
    @Override
    public String getDescription() {
        return "Integrate with external APIs to fetch data or perform operations.";
    }
    
    @Override
    public Map<String, ToolParameter> getParameters() {
        return Map.of(
            "endpoint", new ToolParameter("endpoint", "string", "API endpoint name", true),
            "method", new ToolParameter("method", "string", "HTTP method (GET, POST, etc.)", false, "GET"),
            "parameters", new ToolParameter("parameters", "object", "API parameters", false),
            "headers", new ToolParameter("headers", "object", "Additional headers", false)
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters) throws ToolExecutionException {
        try {
            String endpointName = (String) parameters.get("endpoint");
            String method = (String) parameters.getOrDefault("method", "GET");
            Map<String, Object> apiParams = (Map<String, Object>) parameters.getOrDefault("parameters", Map.of());
            Map<String, String> headers = (Map<String, String>) parameters.getOrDefault("headers", Map.of());
            
            APIEndpoint endpoint = endpoints.get(endpointName);
            if (endpoint == null) {
                return ToolResult.failure("Unknown endpoint: " + endpointName);
            }
            
            // Build request
            WebClient.RequestHeadersSpec<?> requestSpec = buildRequest(endpoint, method, apiParams, headers);
            
            // Execute request
            String response = requestSpec.retrieve()
                .bodyToMono(String.class)
                .block();
            
            // Process response
            Object processedResult = endpoint.processResponse(response);
            
            Map<String, Object> metadata = Map.of(
                "endpoint", endpointName,
                "method", method,
                "response_size", response.length(),
                "execution_time", System.currentTimeMillis()
            );
            
            return ToolResult.success(processedResult, metadata);
            
        } catch (Exception e) {
            throw new ToolExecutionException("API call failed: " + e.getMessage(), e);
        }
    }
    
    private Map<String, APIEndpoint> initializeEndpoints() {
        Map<String, APIEndpoint> endpoints = new HashMap<>();
        
        // Weather API example
        endpoints.put("weather", new APIEndpoint(
            "https://api.openweathermap.org/data/2.5/weather",
            "GET",
            response -> {
                // Parse weather JSON and return formatted string
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(response);
                    String city = root.get("name").asText();
                    double temp = root.get("main").get("temp").asDouble() - 273.15; // Convert from Kelvin
                    String description = root.get("weather").get(0).get("description").asText();
                    
                    return String.format("Weather in %s: %.1f°C, %s", city, temp, description);
                } catch (Exception e) {
                    return "Weather data: " + response;
                }
            }
        ));
        
        // News API example
        endpoints.put("news", new APIEndpoint(
            "https://newsapi.org/v2/top-headlines",
            "GET",
            response -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(response);
                    JsonNode articles = root.get("articles");
                    
                    StringBuilder news = new StringBuilder("Top Headlines:\n");
                    for (int i = 0; i < Math.min(5, articles.size()); i++) {
                        JsonNode article = articles.get(i);
                        news.append(String.format("%d. %s\n", i + 1, 
                            article.get("title").asText()));
                    }
                    
                    return news.toString();
                } catch (Exception e) {
                    return "News data: " + response;
                }
            }
        ));
        
        return endpoints;
    }
    
    private WebClient.RequestHeadersSpec<?> buildRequest(APIEndpoint endpoint, String method, 
                                                        Map<String, Object> params, 
                                                        Map<String, String> headers) {
        
        WebClient.RequestBodySpec requestSpec = webClient.method(HttpMethod.valueOf(method))
            .uri(endpoint.getUrl());
        
        // Add headers
        headers.forEach(requestSpec::header);
        
        // Add parameters based on method
        if ("GET".equals(method)) {
            // Add as query parameters
            WebClient.RequestHeadersSpec<?> headersSpec = requestSpec;
            for (Map.Entry<String, Object> param : params.entrySet()) {
                headersSpec = ((WebClient.RequestHeadersUriSpec<?>) headersSpec)
                    .uri(uriBuilder -> uriBuilder.queryParam(param.getKey(), param.getValue()).build());
            }
            return headersSpec;
        } else {
            // Add as request body
            return requestSpec.bodyValue(params);
        }
    }
}

class APIEndpoint {
    private final String url;
    private final String method;
    private final Function<String, Object> responseProcessor;
    
    public APIEndpoint(String url, String method, Function<String, Object> responseProcessor) {
        this.url = url;
        this.method = method;
        this.responseProcessor = responseProcessor;
    }
    
    public Object processResponse(String response) {
        return responseProcessor.apply(response);
    }
    
    public String getUrl() { return url; }
    public String getMethod() { return method; }
}
```

## 🚀 Best Practices

1. **Tool Design**
   - Keep tools focused on single responsibilities
   - Provide clear descriptions for agent decision-making
   - Include comprehensive parameter validation
   - Handle errors gracefully

2. **Security**
   - Sanitize all inputs before execution
   - Implement proper access controls
   - Use safe execution environments
   - Log all tool executions for auditing

3. **Performance**
   - Implement timeout mechanisms
   - Use connection pooling for API calls
   - Cache results where appropriate
   - Monitor tool execution times

4. **Error Handling**
   - Provide meaningful error messages
   - Implement retry mechanisms for transient failures
   - Graceful degradation when tools are unavailable
   - Comprehensive logging for debugging

5. **Integration**
   - Design tools to work well together
   - Support tool chaining for complex operations
   - Maintain state between tool calls when needed
   - Provide metadata for result interpretation

## 🔗 Integration with Other Components

Tools integrate with:
- **Agents**: Agents select and use appropriate tools
- **Memory**: Tool results can be stored in memory
- **State Management**: Tool execution can trigger state changes
- **Chains**: Tools can be part of processing chains

---

*Next: [Chains](../chains/) - Learn about combining multiple components into workflows.*
