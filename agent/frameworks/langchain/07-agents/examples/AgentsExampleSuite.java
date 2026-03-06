/**
 * LangChain Agents Examples - Complete Implementation
 * Demonstrates ReAct, Plan-Execute, and Conversational agents
 */
package com.example.agent.langchain.agents;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive agents demonstration
 */
public class AgentsExampleSuite {

    private static ChatLanguageModel llm;
    private static ToolRegistry toolRegistry;

    public static void main(String[] args) {
        System.out.println("=== LangChain Agents Examples ===");

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

        // Initialize tools
        setupTools();

        try {
            // Run all agent examples
            runReActAgentExample();
            runPlanExecuteAgentExample();
            runConversationalAgentExample();
            runSpecializedAgentExamples();
            runMultiAgentExample();

        } catch (Exception e) {
            System.err.println("Error running agent examples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Setup tool registry with example tools
     */
    private static void setupTools() {
        List<AgentTool> tools = Arrays.asList(
            new CalculatorTool(),
            new WebSearchTool(),
            new FileOperationsTool(),
            new WeatherTool(),
            new TimezoneTool()
        );

        toolRegistry = new ToolRegistry(tools, llm);
    }

    /**
     * ReAct Agent demonstration
     */
    private static void runReActAgentExample() {
        System.out.println("\n1. ReAct Agent Example:");

        ReActAgent reactAgent = new ReActAgent(llm, toolRegistry);
        AgentContext context = new AgentContext("session_1", "user_123");

        String[] queries = {
            "What's 15% of 250?",
            "What's the weather like in San Francisco?",
            "Calculate 25 * 30 and tell me what time it is in Tokyo"
        };

        for (String query : queries) {
            try {
                System.out.println("\nQuery: " + query);
                AgentResponse response = reactAgent.execute(query, context);

                if (response.isSuccess()) {
                    System.out.println("Response: " + response.getResponse());
                    System.out.println("Actions taken: " + response.getActions().size());

                    for (AgentAction action : response.getActions()) {
                        System.out.println("  - Used " + action.getTool() +
                                         " with result: " + action.getResult());
                    }
                } else {
                    System.out.println("Error: " + response.getMetadata().get("error"));
                }

            } catch (Exception e) {
                System.err.println("ReAct agent error: " + e.getMessage());
            }
        }
    }

    /**
     * Plan-Execute Agent demonstration
     */
    private static void runPlanExecuteAgentExample() {
        System.out.println("\n2. Plan-Execute Agent Example:");

        PlanExecuteAgent planAgent = new PlanExecuteAgent(llm, toolRegistry);
        AgentContext context = new AgentContext("session_2", "user_456");

        String complexQuery = "I need to research the population of Tokyo, calculate what 5% of that would be, " +
                             "and then find out the current weather there. Can you help me with this multi-step task?";

        try {
            System.out.println("Complex Query: " + complexQuery);
            AgentResponse response = planAgent.execute(complexQuery, context);

            if (response.isSuccess()) {
                System.out.println("\nPlan-Execute Result:");
                System.out.println(response.getResponse());

                System.out.println("\nReasoning Process:");
                System.out.println(response.getReasoning());

                System.out.println("\nActions Executed:");
                for (int i = 0; i < response.getActions().size(); i++) {
                    AgentAction action = response.getActions().get(i);
                    System.out.println("Step " + (i + 1) + ": " + action.getReasoning());
                    System.out.println("  Tool: " + action.getTool());
                    System.out.println("  Result: " + action.getResult());
                }
            } else {
                System.out.println("Plan-Execute failed: " + response.getMetadata().get("error"));
            }

        } catch (Exception e) {
            System.err.println("Plan-Execute agent error: " + e.getMessage());
        }
    }

    /**
     * Conversational Agent demonstration
     */
    private static void runConversationalAgentExample() {
        System.out.println("\n3. Conversational Agent Example:");

        ConversationalAgent chatAgent = new ConversationalAgent(llm,
            "You are a helpful and friendly AI assistant with a warm personality. " +
            "You enjoy helping users and always try to be encouraging.");

        AgentContext context = new AgentContext("session_3", "user_789");

        String[] conversation = {
            "Hi there! I'm feeling a bit overwhelmed with work lately.",
            "I have a big presentation coming up next week and I'm nervous.",
            "Do you have any tips for managing presentation anxiety?",
            "Thank you! That's really helpful advice."
        };

        for (String userInput : conversation) {
            try {
                System.out.println("\nUser: " + userInput);
                AgentResponse response = chatAgent.execute(userInput, context);

                if (response.isSuccess()) {
                    System.out.println("AI: " + response.getResponse());
                } else {
                    System.out.println("Error: " + response.getMetadata().get("error"));
                }

            } catch (Exception e) {
                System.err.println("Conversational agent error: " + e.getMessage());
            }
        }

        // Show conversation metadata
        System.out.println("\nConversation Metadata:");
        context.getVariables().forEach((key, value) ->
            System.out.println("  " + key + ": " + value));
    }

    /**
     * Specialized agent examples
     */
    private static void runSpecializedAgentExamples() {
        System.out.println("\n4. Specialized Agent Examples:");

        // Code Analysis Agent
        CodeAnalysisAgent codeAgent = new CodeAnalysisAgent(llm);
        AgentContext codeContext = new AgentContext("session_4", "user_dev");

        String javaCode = """
            public class Calculator {
                public static int add(int a, int b) {
                    return a + b;
                }
                
                public static void main(String[] args) {
                    int result = add(5, 3);
                    System.out.println("Result: " + result);
                }
            }
            """;

        try {
            System.out.println("\nCode Analysis:");
            AgentResponse codeResponse = codeAgent.execute(javaCode, codeContext);

            if (codeResponse.isSuccess()) {
                System.out.println(codeResponse.getResponse());
            }

        } catch (Exception e) {
            System.err.println("Code analysis error: " + e.getMessage());
        }

        // Content Creation Agent
        ContentCreationAgent contentAgent = new ContentCreationAgent(llm);
        AgentContext contentContext = new AgentContext("session_5", "user_writer");

        try {
            System.out.println("\nContent Creation:");
            String contentRequest = "Create a blog post outline about the benefits of remote work";
            AgentResponse contentResponse = contentAgent.execute(contentRequest, contentContext);

            if (contentResponse.isSuccess()) {
                System.out.println(contentResponse.getResponse());
            }

        } catch (Exception e) {
            System.err.println("Content creation error: " + e.getMessage());
        }
    }

    /**
     * Multi-agent coordination example
     */
    private static void runMultiAgentExample() {
        System.out.println("\n5. Multi-Agent Coordination Example:");

        // Create specialized agents
        ResearchAgent researcher = new ResearchAgent(llm, toolRegistry);
        AnalysisAgent analyst = new AnalysisAgent(llm);
        ReportingAgent reporter = new ReportingAgent(llm);

        // Create coordinator
        MultiAgentCoordinator coordinator = new MultiAgentCoordinator(
            Map.of(
                "researcher", researcher,
                "analyst", analyst,
                "reporter", reporter
            )
        );

        String complexTask = "Research current trends in artificial intelligence, " +
                            "analyze their potential impact on the job market, " +
                            "and create a summary report";

        try {
            AgentContext coordinatorContext = new AgentContext("multi_session", "user_manager");
            AgentResponse finalResponse = coordinator.executeTask(complexTask, coordinatorContext);

            if (finalResponse.isSuccess()) {
                System.out.println("Multi-Agent Task Completed:");
                System.out.println(finalResponse.getResponse());

                System.out.println("\nAgent Coordination Details:");
                finalResponse.getMetadata().forEach((key, value) ->
                    System.out.println("  " + key + ": " + value));
            } else {
                System.out.println("Multi-agent coordination failed: " +
                                 finalResponse.getMetadata().get("error"));
            }

        } catch (Exception e) {
            System.err.println("Multi-agent coordination error: " + e.getMessage());
        }
    }
}

/**
 * Specialized Code Analysis Agent
 */
class CodeAnalysisAgent extends BaseAgent {

    public CodeAnalysisAgent(ChatLanguageModel llm) {
        super(llm, "CodeAnalysisAgent", "Specialized agent for code analysis and review");
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
            "Code structure analysis",
            "Best practices review",
            "Performance suggestions",
            "Documentation generation",
            "Bug detection"
        );
    }

    @Override
    protected AgentResponse executeInternal(String input, AgentContext context) throws Exception {
        List<AgentAction> actions = new ArrayList<>();
        StringBuilder analysis = new StringBuilder();

        // Analyze code structure
        String structurePrompt = "Analyze the structure of this code and identify its components:\n" + input;
        String structure = generateLLMResponse(structurePrompt);
        analysis.append("Code Structure:\n").append(structure).append("\n\n");

        actions.add(new AgentAction("analysis", "structure_analyzer", Map.of(),
                                   "Analyzing code structure", structure, true));

        // Review best practices
        String practicesPrompt = "Review this code for best practices and coding standards:\n" + input;
        String practices = generateLLMResponse(practicesPrompt);
        analysis.append("Best Practices Review:\n").append(practices).append("\n\n");

        actions.add(new AgentAction("analysis", "best_practices_reviewer", Map.of(),
                                   "Reviewing best practices", practices, true));

        // Suggest improvements
        String improvementsPrompt = "Suggest specific improvements for this code:\n" + input;
        String improvements = generateLLMResponse(improvementsPrompt);
        analysis.append("Improvement Suggestions:\n").append(improvements);

        actions.add(new AgentAction("analysis", "improvement_suggester", Map.of(),
                                   "Suggesting improvements", improvements, true));

        return AgentResponse.success(analysis.toString(), "Comprehensive code analysis completed",
                                   actions, System.currentTimeMillis());
    }
}

/**
 * Content Creation Agent
 */
class ContentCreationAgent extends BaseAgent {

    public ContentCreationAgent(ChatLanguageModel llm) {
        super(llm, "ContentCreationAgent", "Specialized agent for content creation and writing");
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
            "Blog post creation",
            "Article outlines",
            "Creative writing",
            "Technical documentation",
            "Marketing copy"
        );
    }

    @Override
    protected AgentResponse executeInternal(String input, AgentContext context) throws Exception {
        List<AgentAction> actions = new ArrayList<>();
        StringBuilder content = new StringBuilder();

        // Analyze content requirements
        String requirementsPrompt = "Analyze what type of content is being requested: " + input;
        String requirements = generateLLMResponse(requirementsPrompt);

        actions.add(new AgentAction("analysis", "requirements_analyzer", Map.of(),
                                   "Analyzing content requirements", requirements, true));

        // Create outline
        String outlinePrompt = "Create a detailed outline for: " + input;
        String outline = generateLLMResponse(outlinePrompt);
        content.append("Content Outline:\n").append(outline).append("\n\n");

        actions.add(new AgentAction("creation", "outline_generator", Map.of(),
                                   "Creating content outline", outline, true));

        // Generate key points
        String keyPointsPrompt = "Generate key points and talking points for: " + input;
        String keyPoints = generateLLMResponse(keyPointsPrompt);
        content.append("Key Points:\n").append(keyPoints).append("\n\n");

        actions.add(new AgentAction("creation", "key_points_generator", Map.of(),
                                   "Generating key points", keyPoints, true));

        // Create introduction
        String introPrompt = "Write an engaging introduction for content about: " + input;
        String introduction = generateLLMResponse(introPrompt);
        content.append("Sample Introduction:\n").append(introduction);

        actions.add(new AgentAction("creation", "introduction_writer", Map.of(),
                                   "Writing introduction", introduction, true));

        return AgentResponse.success(content.toString(), "Content creation package completed",
                                   actions, System.currentTimeMillis());
    }
}

/**
 * Research Agent for information gathering
 */
class ResearchAgent extends BaseAgent {

    private final ToolRegistry toolRegistry;

    public ResearchAgent(ChatLanguageModel llm, ToolRegistry toolRegistry) {
        super(llm, "ResearchAgent", "Specialized agent for research and information gathering");
        this.toolRegistry = toolRegistry;
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("Web research", "Data gathering", "Fact verification", "Source compilation");
    }

    @Override
    protected AgentResponse executeInternal(String input, AgentContext context) throws Exception {
        List<AgentAction> actions = new ArrayList<>();
        StringBuilder research = new StringBuilder();

        // Use web search tool if available
        Optional<AgentTool> webSearch = toolRegistry.getTool("web_search");
        if (webSearch.isPresent()) {
            try {
                ToolResult searchResult = webSearch.get().execute(Map.of("query", input));
                if (searchResult.isSuccess()) {
                    research.append("Research Findings:\n").append(searchResult.getResult()).append("\n\n");
                    actions.add(new AgentAction("research", "web_search", Map.of("query", input),
                                               "Conducting web research", searchResult.getResult(), true));
                }
            } catch (Exception e) {
                research.append("Web search unavailable, using general knowledge.\n\n");
            }
        }

        // Generate research summary using LLM knowledge
        String summaryPrompt = "Provide a comprehensive research summary about: " + input;
        String summary = generateLLMResponse(summaryPrompt);
        research.append("Knowledge Summary:\n").append(summary);

        actions.add(new AgentAction("research", "knowledge_synthesis", Map.of(),
                                   "Synthesizing available knowledge", summary, true));

        return AgentResponse.success(research.toString(), "Research completed",
                                   actions, System.currentTimeMillis());
    }
}

/**
 * Analysis Agent for data analysis
 */
class AnalysisAgent extends BaseAgent {

    public AnalysisAgent(ChatLanguageModel llm) {
        super(llm, "AnalysisAgent", "Specialized agent for data analysis and insights");
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("Data analysis", "Trend identification", "Impact assessment", "Statistical reasoning");
    }

    @Override
    protected AgentResponse executeInternal(String input, AgentContext context) throws Exception {
        List<AgentAction> actions = new ArrayList<>();
        StringBuilder analysis = new StringBuilder();

        // Analyze trends
        String trendsPrompt = "Analyze trends and patterns in this information: " + input;
        String trends = generateLLMResponse(trendsPrompt);
        analysis.append("Trend Analysis:\n").append(trends).append("\n\n");

        actions.add(new AgentAction("analysis", "trend_analyzer", Map.of(),
                                   "Analyzing trends", trends, true));

        // Assess impact
        String impactPrompt = "Assess the potential impact and implications of: " + input;
        String impact = generateLLMResponse(impactPrompt);
        analysis.append("Impact Assessment:\n").append(impact);

        actions.add(new AgentAction("analysis", "impact_assessor", Map.of(),
                                   "Assessing impact", impact, true));

        return AgentResponse.success(analysis.toString(), "Analysis completed",
                                   actions, System.currentTimeMillis());
    }
}

/**
 * Reporting Agent for creating summaries and reports
 */
class ReportingAgent extends BaseAgent {

    public ReportingAgent(ChatLanguageModel llm) {
        super(llm, "ReportingAgent", "Specialized agent for creating reports and summaries");
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("Report generation", "Executive summaries", "Data visualization planning", "Presentation creation");
    }

    @Override
    protected AgentResponse executeInternal(String input, AgentContext context) throws Exception {
        List<AgentAction> actions = new ArrayList<>();
        StringBuilder report = new StringBuilder();

        // Create executive summary
        String summaryPrompt = "Create an executive summary for this information: " + input;
        String summary = generateLLMResponse(summaryPrompt);
        report.append("Executive Summary:\n").append(summary).append("\n\n");

        actions.add(new AgentAction("reporting", "summary_generator", Map.of(),
                                   "Creating executive summary", summary, true));

        // Generate recommendations
        String recommendationsPrompt = "Provide actionable recommendations based on: " + input;
        String recommendations = generateLLMResponse(recommendationsPrompt);
        report.append("Recommendations:\n").append(recommendations);

        actions.add(new AgentAction("reporting", "recommendation_generator", Map.of(),
                                   "Generating recommendations", recommendations, true));

        return AgentResponse.success(report.toString(), "Report completed",
                                   actions, System.currentTimeMillis());
    }
}

/**
 * Multi-Agent Coordinator
 */
class MultiAgentCoordinator {

    private final Map<String, BaseAgent> agents;

    public MultiAgentCoordinator(Map<String, BaseAgent> agents) {
        this.agents = new HashMap<>(agents);
    }

    public AgentResponse executeTask(String task, AgentContext context) throws Exception {
        List<AgentAction> allActions = new ArrayList<>();
        StringBuilder finalResult = new StringBuilder();
        Map<String, Object> metadata = new HashMap<>();

        // Step 1: Research
        if (agents.containsKey("researcher")) {
            AgentResponse researchResult = agents.get("researcher").execute(task, context);
            if (researchResult.isSuccess()) {
                finalResult.append("=== RESEARCH PHASE ===\n");
                finalResult.append(researchResult.getResponse()).append("\n\n");
                allActions.addAll(researchResult.getActions());
                context.setVariable("research_data", researchResult.getResponse());
            }
        }

        // Step 2: Analysis
        if (agents.containsKey("analyst")) {
            String analysisInput = context.getVariable("research_data", String.class)
                .orElse(task);
            AgentResponse analysisResult = agents.get("analyst").execute(analysisInput, context);
            if (analysisResult.isSuccess()) {
                finalResult.append("=== ANALYSIS PHASE ===\n");
                finalResult.append(analysisResult.getResponse()).append("\n\n");
                allActions.addAll(analysisResult.getActions());
                context.setVariable("analysis_data", analysisResult.getResponse());
            }
        }

        // Step 3: Reporting
        if (agents.containsKey("reporter")) {
            String reportInput = context.getVariable("analysis_data", String.class)
                .orElse(context.getVariable("research_data", String.class).orElse(task));
            AgentResponse reportResult = agents.get("reporter").execute(reportInput, context);
            if (reportResult.isSuccess()) {
                finalResult.append("=== FINAL REPORT ===\n");
                finalResult.append(reportResult.getResponse());
                allActions.addAll(reportResult.getActions());
            }
        }

        // Add coordination metadata
        metadata.put("agents_used", agents.keySet());
        metadata.put("total_actions", allActions.size());
        metadata.put("coordination_strategy", "sequential");

        return new AgentResponse(true, finalResult.toString(), "Multi-agent task coordination",
                               allActions, metadata, System.currentTimeMillis());
    }
}

/**
 * Mock tool implementations for demonstration
 */
class TimezoneTool implements AgentTool {

    @Override
    public String getName() {
        return "timezone";
    }

    @Override
    public String getDescription() {
        return "Get current time in different timezones";
    }

    @Override
    public Map<String, ToolParameter> getParameters() {
        return Map.of(
            "timezone", new ToolParameter("timezone", "string", "Timezone or city name", true)
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) throws ToolExecutionException {
        String timezone = (String) parameters.get("timezone");

        // Mock implementation
        Map<String, String> timezones = Map.of(
            "tokyo", "3:30 PM JST",
            "new york", "2:30 AM EST",
            "london", "7:30 AM GMT",
            "sydney", "5:30 PM AEDT"
        );

        String time = timezones.getOrDefault(timezone.toLowerCase(), "12:00 PM UTC");
        return ToolResult.success("Current time in " + timezone + ": " + time);
    }
}

/**
 * Simple tool registry implementation
 */
class ToolRegistry {
    private final Map<String, AgentTool> tools;
    private final ChatLanguageModel llm;

    public ToolRegistry(List<AgentTool> tools, ChatLanguageModel llm) {
        this.llm = llm;
        this.tools = tools.stream()
            .collect(java.util.stream.Collectors.toMap(AgentTool::getName, t -> t));
    }

    public Optional<AgentTool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Collection<AgentTool> getAllTools() {
        return tools.values();
    }
}
