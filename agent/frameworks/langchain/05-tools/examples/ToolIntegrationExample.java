///**
// * Tools Integration Examples - Comprehensive Implementation
// * Demonstrates various tool types and usage patterns
// */
//package com.example.agent.tools;
//
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.net.URI;
//import java.time.Duration;
//
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.UserMessage;
//
///**
// * Example demonstration of tool integration
// */
//public class ToolIntegrationExample {
//
//    public static void main(String[] args) {
//        // Initialize components (replace with actual implementations)
//        ChatLanguageModel llm = createMockLLM();
//
//        // Create tools
//        List<AgentTool> tools = Arrays.asList(
//            new WebSearchTool(),
//            new CalculatorTool(),
//            new FileOperationsTool(),
//            new WeatherTool()
//        );
//
//        // Create tool registry
//        ToolRegistry toolRegistry = new ToolRegistry(tools, llm);
//
//        // Create agent with tools
//        ToolUsingAgent agent = new ToolUsingAgent(llm, toolRegistry);
//
//        // Demonstrate tool usage
//        demonstrateToolUsage(agent, toolRegistry);
//    }
//
//    private static void demonstrateToolUsage(ToolUsingAgent agent, ToolRegistry toolRegistry) {
//        System.out.println("=== Tool Integration Demonstration ===\n");
//
//        // Example 1: Web search
//        System.out.println("1. Web Search Example:");
//        String searchQuery = "What are the latest developments in AI?";
//        String response1 = agent.processQuery(searchQuery);
//        System.out.println("Query: " + searchQuery);
//        System.out.println("Response: " + response1);
//        System.out.println();
//
//        // Example 2: Calculator
//        System.out.println("2. Calculator Example:");
//        String mathQuery = "What is 15% of 2500?";
//        String response2 = agent.processQuery(mathQuery);
//        System.out.println("Query: " + mathQuery);
//        System.out.println("Response: " + response2);
//        System.out.println();
//
//        // Example 3: File operations
//        System.out.println("3. File Operations Example:");
//        String fileQuery = "Create a file called 'notes.txt' with the content 'AI learning notes'";
//        String response3 = agent.processQuery(fileQuery);
//        System.out.println("Query: " + fileQuery);
//        System.out.println("Response: " + response3);
//        System.out.println();
//
//        // Example 4: Tool chaining
//        System.out.println("4. Tool Chaining Example:");
//        demonstrateToolChaining(toolRegistry);
//        System.out.println();
//
//        // Example 5: Direct tool usage
//        System.out.println("5. Direct Tool Usage Example:");
//        demonstrateDirectToolUsage(toolRegistry);
//    }
//
//    private static void demonstrateToolChaining(ToolRegistry toolRegistry) {
//        ToolChain chain = new ToolChain(toolRegistry, createMockLLM());
//
//        String chainQuery = "Calculate 25 * 30 and then create a file with the result";
//        List<String> toolSequence = Arrays.asList("calculator", "file_operations");
//
//        String result = chain.executeChain(chainQuery, toolSequence);
//        System.out.println("Chain Query: " + chainQuery);
//        System.out.println("Chain Result: " + result);
//    }
//
//    private static void demonstrateDirectToolUsage(ToolRegistry toolRegistry) {
//        // Direct calculator usage
//        Optional<AgentTool> calcTool = toolRegistry.getTool("calculator");
//        if (calcTool.isPresent()) {
//            try {
//                Map<String, Object> params = Map.of("expression", "100 * 0.15");
//                ToolResult result = calcTool.get().execute(params);
//
//                System.out.println("Direct Calculator Call:");
//                System.out.println("Expression: 100 * 0.15");
//                System.out.println("Result: " + (result.isSuccess() ? result.getResult() : result.getErrorMessage()));
//                System.out.println("Metadata: " + result.getMetadata());
//            } catch (Exception e) {
//                System.err.println("Error: " + e.getMessage());
//            }
//        }
//    }
//
//    private static ChatLanguageModel createMockLLM() {
//        return new MockChatLanguageModel();
//    }
//}
//
///**
// * Mock ChatLanguageModel for demonstration purposes
// */
//class MockChatLanguageModel implements ChatLanguageModel {
//
//    @Override
//    public Response<AiMessage> generate(List<ChatMessage> messages) {
//        ChatMessage lastMessage = messages.get(messages.size() - 1);
//        String content = lastMessage.text().toLowerCase();
//
//        String response;
//        if (content.contains("tool") && content.contains("required")) {
//            response = "TOOL_REQUIRED";
//        } else if (content.contains("calculator") || content.contains("calculate")) {
//            response = "calculator";
//        } else if (content.contains("search") || content.contains("web")) {
//            response = "web_search";
//        } else if (content.contains("file") || content.contains("create")) {
//            response = "file_operations";
//        } else if (content.contains("weather")) {
//            response = "weather";
//        } else if (content.contains("parameters")) {
//            // Extract parameters based on context
//            if (content.contains("calculator")) {
//                response = "{\"expression\": \"" + extractExpression(content) + "\"}";
//            } else if (content.contains("file")) {
//                response = "{\"operation\": \"create\", \"filename\": \"notes.txt\", \"content\": \"AI learning notes\"}";
//            } else {
//                response = "{}";
//            }
//        } else {
//            response = "Based on the available information, here's what I found: " +
//                      generateMockResponse(content);
//        }
//
//        return Response.from(AiMessage.from(response));
//    }
//
//    private String extractExpression(String content) {
//        // Simple expression extraction
//        if (content.contains("15%") && content.contains("2500")) {
//            return "2500 * 0.15";
//        } else if (content.contains("25") && content.contains("30")) {
//            return "25 * 30";
//        } else if (content.contains("100") && content.contains("0.15")) {
//            return "100 * 0.15";
//        }
//        return "1 + 1";
//    }
//
//    private String generateMockResponse(String content) {
//        if (content.contains("ai") || content.contains("artificial")) {
//            return "AI continues to evolve rapidly with new developments in large language models and autonomous systems.";
//        } else if (content.contains("calculate") || content.contains("math")) {
//            return "I can help you with mathematical calculations.";
//        } else if (content.contains("file")) {
//            return "I can help you manage files in your workspace.";
//        } else {
//            return "I understand your request and will do my best to help.";
//        }
//    }
//}
//
///**
// * Weather tool implementation
// */
//class WeatherTool implements AgentTool {
//
//    @Override
//    public String getName() {
//        return "weather";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Get current weather information for a specified location.";
//    }
//
//    @Override
//    public Map<String, ToolParameter> getParameters() {
//        return Map.of(
//            "location", new ToolParameter("location", "string", "City name or location", true),
//            "units", new ToolParameter("units", "string", "Temperature units (celsius/fahrenheit)", false, "celsius")
//        );
//    }
//
//    @Override
//    public ToolResult execute(Map<String, Object> parameters) throws ToolExecutionException {
//        try {
//            String location = (String) parameters.get("location");
//            String units = (String) parameters.getOrDefault("units", "celsius");
//
//            if (location == null || location.trim().isEmpty()) {
//                return ToolResult.failure("Location is required");
//            }
//
//            // Mock weather data - in real implementation, call weather API
//            Map<String, Object> weatherData = generateMockWeatherData(location, units);
//
//            String weatherReport = formatWeatherReport(weatherData);
//
//            Map<String, Object> metadata = Map.of(
//                "location", location,
//                "units", units,
//                "data_source", "mock_weather_service",
//                "timestamp", System.currentTimeMillis()
//            );
//
//            return ToolResult.success(weatherReport, metadata);
//
//        } catch (Exception e) {
//            throw new ToolExecutionException("Weather lookup failed: " + e.getMessage(), e);
//        }
//    }
//
//    private Map<String, Object> generateMockWeatherData(String location, String units) {
//        // Generate realistic mock weather data
//        Random random = new Random();
//
//        double temperature;
//        if ("fahrenheit".equals(units)) {
//            temperature = 32 + random.nextDouble() * 80; // 32-112°F
//        } else {
//            temperature = random.nextDouble() * 40; // 0-40°C
//        }
//
//        String[] conditions = {"Sunny", "Partly Cloudy", "Cloudy", "Rainy", "Stormy"};
//        String condition = conditions[random.nextInt(conditions.length)];
//
//        int humidity = 30 + random.nextInt(60); // 30-90%
//        double windSpeed = random.nextDouble() * 20; // 0-20 mph/kmh
//
//        Map<String, Object> weatherData = new HashMap<>();
//        weatherData.put("location", location);
//        weatherData.put("temperature", Math.round(temperature * 10.0) / 10.0);
//        weatherData.put("condition", condition);
//        weatherData.put("humidity", humidity);
//        weatherData.put("windSpeed", Math.round(windSpeed * 10.0) / 10.0);
//        weatherData.put("units", units);
//
//        return weatherData;
//    }
//
//    private String formatWeatherReport(Map<String, Object> data) {
//        String tempUnit = "celsius".equals(data.get("units")) ? "°C" : "°F";
//        String speedUnit = "celsius".equals(data.get("units")) ? "km/h" : "mph";
//
//        return String.format(
//            "Weather in %s:\n" +
//            "Temperature: %.1f%s\n" +
//            "Condition: %s\n" +
//            "Humidity: %d%%\n" +
//            "Wind Speed: %.1f %s",
//            data.get("location"),
//            data.get("temperature"), tempUnit,
//            data.get("condition"),
//            data.get("humidity"),
//            data.get("windSpeed"), speedUnit
//        );
//    }
//}
//
///**
// * Advanced tool orchestrator for complex workflows
// */
//class ToolOrchestrator {
//
//    private final ToolRegistry toolRegistry;
//    private final ChatLanguageModel llm;
//
//    public ToolOrchestrator(ToolRegistry toolRegistry, ChatLanguageModel llm) {
//        this.toolRegistry = toolRegistry;
//        this.llm = llm;
//    }
//
//    /**
//     * Execute a complex workflow with multiple tools
//     */
//    public String executeWorkflow(String userQuery) {
//        try {
//            // Analyze query and plan workflow
//            WorkflowPlan plan = analyzeAndPlan(userQuery);
//
//            // Execute workflow steps
//            List<ToolResult> results = new ArrayList<>();
//            String currentContext = userQuery;
//
//            for (WorkflowStep step : plan.getSteps()) {
//                ToolResult stepResult = executeWorkflowStep(step, currentContext, results);
//                results.add(stepResult);
//
//                if (!stepResult.isSuccess()) {
//                    return "Workflow failed at step " + (results.size()) + ": " +
//                           stepResult.getErrorMessage();
//                }
//
//                // Update context for next step
//                currentContext = stepResult.getResult().toString();
//            }
//
//            // Generate final response
//            return synthesizeResults(userQuery, results);
//
//        } catch (Exception e) {
//            return "Workflow execution failed: " + e.getMessage();
//        }
//    }
//
//    private WorkflowPlan analyzeAndPlan(String userQuery) {
//        // Simplified workflow planning - in real implementation, use more sophisticated planning
//        List<WorkflowStep> steps = new ArrayList<>();
//
//        if (userQuery.toLowerCase().contains("search") && userQuery.toLowerCase().contains("calculate")) {
//            steps.add(new WorkflowStep("web_search", Map.of("query", userQuery)));
//            steps.add(new WorkflowStep("calculator", Map.of("expression", "derived_from_search")));
//        } else if (userQuery.toLowerCase().contains("weather") && userQuery.toLowerCase().contains("file")) {
//            steps.add(new WorkflowStep("weather", Map.of("location", "user_location")));
//            steps.add(new WorkflowStep("file_operations", Map.of("operation", "create", "filename", "weather_report.txt")));
//        } else {
//            // Single tool workflow
//            Optional<AgentTool> tool = toolRegistry.selectTool(userQuery);
//            if (tool.isPresent()) {
//                steps.add(new WorkflowStep(tool.get().getName(), Map.of()));
//            }
//        }
//
//        return new WorkflowPlan(steps);
//    }
//
//    private ToolResult executeWorkflowStep(WorkflowStep step, String context, List<ToolResult> previousResults) {
//        try {
//            Optional<AgentTool> toolOpt = toolRegistry.getTool(step.getToolName());
//            if (toolOpt.isEmpty()) {
//                return ToolResult.failure("Tool not found: " + step.getToolName());
//            }
//
//            AgentTool tool = toolOpt.get();
//
//            // Extract parameters considering context and previous results
//            Map<String, Object> parameters = extractContextualParameters(tool, step.getParameters(), context, previousResults);
//
//            // Execute tool
//            return tool.execute(parameters);
//
//        } catch (Exception e) {
//            return ToolResult.failure("Step execution failed: " + e.getMessage());
//        }
//    }
//
//    private Map<String, Object> extractContextualParameters(AgentTool tool, Map<String, Object> stepParams,
//                                                           String context, List<ToolResult> previousResults) {
//        // Combine step parameters with contextually derived parameters
//        Map<String, Object> parameters = new HashMap<>(stepParams);
//
//        // Use LLM to extract parameters from context if needed
//        if (parameters.isEmpty() || parameters.values().stream().anyMatch(v -> v.toString().startsWith("derived_"))) {
//            Map<String, Object> extractedParams = toolRegistry.extractParameters(tool, context);
//            parameters.putAll(extractedParams);
//        }
//
//        return parameters;
//    }
//
//    private String synthesizeResults(String originalQuery, List<ToolResult> results) {
//        StringBuilder synthesis = new StringBuilder();
//        synthesis.append("Workflow Results for: ").append(originalQuery).append("\n\n");
//
//        for (int i = 0; i < results.size(); i++) {
//            ToolResult result = results.get(i);
//            synthesis.append("Step ").append(i + 1).append(": ");
//
//            if (result.isSuccess()) {
//                synthesis.append(result.getResult()).append("\n");
//            } else {
//                synthesis.append("Failed - ").append(result.getErrorMessage()).append("\n");
//            }
//        }
//
//        // Use LLM to create coherent final response
//        String prompt = String.format(
//            "Based on these workflow results, provide a comprehensive answer to: '%s'\n\nResults:\n%s",
//            originalQuery, synthesis.toString()
//        );
//
//        try {
//            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//            return response.text();
//        } catch (Exception e) {
//            return synthesis.toString();
//        }
//    }
//}
//
///**
// * Workflow planning classes
// */
//class WorkflowPlan {
//    private final List<WorkflowStep> steps;
//
//    public WorkflowPlan(List<WorkflowStep> steps) {
//        this.steps = new ArrayList<>(steps);
//    }
//
//    public List<WorkflowStep> getSteps() {
//        return new ArrayList<>(steps);
//    }
//}
//
//class WorkflowStep {
//    private final String toolName;
//    private final Map<String, Object> parameters;
//
//    public WorkflowStep(String toolName, Map<String, Object> parameters) {
//        this.toolName = toolName;
//        this.parameters = new HashMap<>(parameters);
//    }
//
//    public String getToolName() { return toolName; }
//    public Map<String, Object> getParameters() { return new HashMap<>(parameters); }
//}
