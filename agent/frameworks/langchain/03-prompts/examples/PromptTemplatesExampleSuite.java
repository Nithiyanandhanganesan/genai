///**
// * Prompt Templates Examples - Complete Implementation
// * Demonstrates various prompt template types and usage patterns
// */
//package com.example.agent.langchain.prompts;
//
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.UserMessage;
//
//import java.util.*;
//import java.util.regex.Pattern;
//
///**
// * Comprehensive prompt templates demonstration
// */
//public class PromptTemplatesExampleSuite {
//
//    private static ChatLanguageModel llm;
//    private static PromptTemplateRegistry templateRegistry;
//
//    public static void main(String[] args) {
//        System.out.println("=== Prompt Templates Examples ===");
//
//        // Initialize LLM
//        String apiKey = System.getenv("OPENAI_API_KEY");
//        if (apiKey == null || apiKey.isEmpty()) {
//            System.out.println("Please set OPENAI_API_KEY environment variable");
//            return;
//        }
//
//        llm = OpenAiChatModel.builder()
//            .apiKey(apiKey)
//            .modelName("gpt-3.5-turbo")
//            .temperature(0.7)
//            .build();
//
//        // Initialize template registry
//        templateRegistry = new PromptTemplateRegistry();
//
//        try {
//            // Run all prompt template examples
//            runBasicTemplateExamples();
//            runChatTemplateExamples();
//            runFewShotExamples();
//            runConditionalTemplateExamples();
//            runCompositeTemplateExamples();
//            runValidationExamples();
//            runRegistryExamples();
//            runAdvancedPatterns();
//
//        } catch (Exception e) {
//            System.err.println("Error running prompt template examples: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Basic string template examples
//     */
//    private static void runBasicTemplateExamples() {
//        System.out.println("\n1. Basic String Template Examples:");
//
//        // Simple translation template
//        StringPromptTemplate translationTemplate = PromptTemplateBuilder
//            .string("Translate the following {source_language} text to {target_language}: {text}")
//            .description("Language translation template")
//            .build();
//
//        Map<String, Object> variables = Map.of(
//            "source_language", "English",
//            "target_language", "Spanish",
//            "text", "Hello, how are you today?"
//        );
//
//        try {
//            String prompt = translationTemplate.format(variables);
//            System.out.println("Translation prompt: " + prompt);
//
//            String response = generateLLMResponse(prompt);
//            System.out.println("Response: " + response);
//
//        } catch (Exception e) {
//            System.err.println("Translation template error: " + e.getMessage());
//        }
//
//        // Code explanation template
//        StringPromptTemplate codeTemplate = PromptTemplateBuilder
//            .string("Explain this {language} code step by step:\n\n{code}\n\nExplanation:")
//            .description("Code explanation template")
//            .optional("complexity_level", "beginner")
//            .build();
//
//        Map<String, Object> codeVars = Map.of(
//            "language", "Java",
//            "code", "public static void main(String[] args) { System.out.println(\"Hello World\"); }"
//        );
//
//        try {
//            String codePrompt = codeTemplate.format(codeVars);
//            System.out.println("\nCode explanation prompt: " + codePrompt);
//
//            String codeResponse = generateLLMResponse(codePrompt);
//            System.out.println("Response: " + codeResponse);
//
//        } catch (Exception e) {
//            System.err.println("Code template error: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Chat template examples with role management
//     */
//    private static void runChatTemplateExamples() {
//        System.out.println("\n2. Chat Template Examples:");
//
//        // Customer service chat template
//        ChatPromptTemplate customerServiceTemplate = PromptTemplateBuilder.chat()
//            .system("You are a helpful customer service representative for {company_name}. " +
//                   "Be professional, friendly, and try to resolve customer issues efficiently.")
//            .user("Customer issue: {customer_issue}")
//            .user("Customer mood: {mood}")
//            .description("Customer service chat template")
//            .optional("company_name", "TechCorp")
//            .build();
//
//        Map<String, Object> serviceVars = Map.of(
//            "company_name", "TechCorp Solutions",
//            "customer_issue", "My software license expired and I can't access my account",
//            "mood", "frustrated"
//        );
//
//        try {
//            String servicePrompt = customerServiceTemplate.format(serviceVars);
//            System.out.println("Customer service prompt:");
//            System.out.println(servicePrompt);
//
//            String serviceResponse = generateLLMResponse(servicePrompt);
//            System.out.println("Response: " + serviceResponse);
//
//        } catch (Exception e) {
//            System.err.println("Customer service template error: " + e.getMessage());
//        }
//
//        // Technical interview template
//        ChatPromptTemplate interviewTemplate = PromptTemplateBuilder.chat()
//            .system("You are conducting a technical interview for a {position} role. " +
//                   "Ask challenging but fair questions about {technology}.")
//            .assistant("I'll be asking you some technical questions today. Let's start with something fundamental.")
//            .user("I'm ready for the interview!")
//            .assistant("Great! Here's your first question: {question}")
//            .description("Technical interview template")
//            .build();
//
//        Map<String, Object> interviewVars = Map.of(
//            "position", "Senior Java Developer",
//            "technology", "Java and Spring Framework",
//            "question", "Explain the difference between ArrayList and LinkedList"
//        );
//
//        try {
//            String interviewPrompt = interviewTemplate.format(interviewVars);
//            System.out.println("\nTechnical interview prompt:");
//            System.out.println(interviewPrompt);
//
//        } catch (Exception e) {
//            System.err.println("Interview template error: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Few-shot learning examples
//     */
//    private static void runFewShotExamples() {
//        System.out.println("\n3. Few-Shot Template Examples:");
//
//        // Sentiment analysis with examples
//        List<Example> sentimentExamples = Arrays.asList(
//            Example.of("text", "I love this product! It's amazing!", "sentiment", "positive"),
//            Example.of("text", "This is the worst experience I've ever had.", "sentiment", "negative"),
//            Example.of("text", "The product is okay, nothing special.", "sentiment", "neutral"),
//            Example.of("text", "Fantastic quality and great customer service!", "sentiment", "positive"),
//            Example.of("text", "I hate waiting for so long.", "sentiment", "negative")
//        );
//
//        StringPromptTemplate exampleTemplate = new StringPromptTemplate(
//            "Text: {text}\nSentiment: {sentiment}",
//            "Example format"
//        );
//
//        StringPromptTemplate prefixTemplate = new StringPromptTemplate(
//            "Analyze the sentiment of the following texts. Classify as positive, negative, or neutral.\n\nExamples:",
//            "Prefix"
//        );
//
//        StringPromptTemplate suffixTemplate = new StringPromptTemplate(
//            "Text: {input_text}\nSentiment:",
//            "Task"
//        );
//
//        FewShotPromptTemplate sentimentTemplate = new FewShotPromptTemplate(
//            sentimentExamples, exampleTemplate, prefixTemplate, suffixTemplate,
//            "Few-shot sentiment analysis"
//        );
//
//        Map<String, Object> sentimentVars = Map.of(
//            "input_text", "The delivery was quick and the packaging was excellent!"
//        );
//
//        try {
//            String sentimentPrompt = sentimentTemplate.format(sentimentVars);
//            System.out.println("Few-shot sentiment analysis prompt:");
//            System.out.println(sentimentPrompt);
//
//            String sentimentResponse = generateLLMResponse(sentimentPrompt);
//            System.out.println("Predicted sentiment: " + sentimentResponse);
//
//        } catch (Exception e) {
//            System.err.println("Sentiment analysis error: " + e.getMessage());
//        }
//
//        // Code debugging examples
//        List<Example> debugExamples = Arrays.asList(
//            Example.of("buggy_code", "for(int i=0; i<=arr.length; i++)",
//                      "bug", "Array index out of bounds",
//                      "fixed_code", "for(int i=0; i<arr.length; i++)"),
//            Example.of("buggy_code", "String str = null; int len = str.length();",
//                      "bug", "Null pointer exception",
//                      "fixed_code", "String str = null; int len = (str != null) ? str.length() : 0;")
//        );
//
//        StringPromptTemplate debugExampleTemplate = new StringPromptTemplate(
//            "Buggy code: {buggy_code}\nBug: {bug}\nFixed code: {fixed_code}",
//            "Debug example"
//        );
//
//        StringPromptTemplate debugPrefix = new StringPromptTemplate(
//            "Find and fix bugs in Java code:\n\nExamples:",
//            "Debug prefix"
//        );
//
//        StringPromptTemplate debugSuffix = new StringPromptTemplate(
//            "Buggy code: {code_to_debug}\nBug:",
//            "Debug task"
//        );
//
//        FewShotPromptTemplate debugTemplate = new FewShotPromptTemplate(
//            debugExamples, debugExampleTemplate, debugPrefix, debugSuffix,
//            "Few-shot code debugging"
//        );
//
//        Map<String, Object> debugVars = Map.of(
//            "code_to_debug", "List<String> list = new ArrayList<>(); String first = list.get(0);"
//        );
//
//        try {
//            String debugPrompt = debugTemplate.format(debugVars);
//            System.out.println("\nFew-shot debugging prompt:");
//            System.out.println(debugPrompt);
//
//            String debugResponse = generateLLMResponse(debugPrompt);
//            System.out.println("Bug analysis: " + debugResponse);
//
//        } catch (Exception e) {
//            System.err.println("Debug template error: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Conditional template examples
//     */
//    private static void runConditionalTemplateExamples() {
//        System.out.println("\n4. Conditional Template Examples:");
//
//        // Create templates for different user experience levels
//        PromptTemplate beginnerTemplate = PromptTemplateBuilder
//            .string("Explain {concept} in very simple terms that a beginner can understand. " +
//                   "Use analogies and avoid technical jargon.")
//            .description("Beginner-friendly explanation")
//            .build();
//
//        PromptTemplate advancedTemplate = PromptTemplateBuilder
//            .string("Provide a detailed technical explanation of {concept}. " +
//                   "Include implementation details, edge cases, and best practices.")
//            .description("Advanced technical explanation")
//            .build();
//
//        PromptTemplate defaultTemplate = PromptTemplateBuilder
//            .string("Explain {concept} with moderate detail, suitable for someone with basic knowledge.")
//            .description("Standard explanation")
//            .build();
//
//        // Create conditional routing
//        List<ConditionTemplate> conditions = Arrays.asList(
//            new ConditionTemplate(
//                vars -> "beginner".equals(vars.get("experience_level")),
//                beginnerTemplate,
//                "Beginner level explanation"
//            ),
//            new ConditionTemplate(
//                vars -> "advanced".equals(vars.get("experience_level")),
//                advancedTemplate,
//                "Advanced level explanation"
//            )
//        );
//
//        ConditionalPromptTemplate conditionalTemplate = new ConditionalPromptTemplate(
//            conditions, defaultTemplate, "Experience-based conditional template"
//        );
//
//        // Test different experience levels
//        String[] experienceLevels = {"beginner", "intermediate", "advanced"};
//        String concept = "object-oriented programming";
//
//        for (String level : experienceLevels) {
//            Map<String, Object> vars = Map.of(
//                "concept", concept,
//                "experience_level", level
//            );
//
//            try {
//                String prompt = conditionalTemplate.format(vars);
//                System.out.println("\nPrompt for " + level + " level:");
//                System.out.println(prompt);
//
//                String response = generateLLMResponse(prompt);
//                System.out.println("Response snippet: " +
//                    (response.length() > 150 ? response.substring(0, 150) + "..." : response));
//
//            } catch (Exception e) {
//                System.err.println("Conditional template error for " + level + ": " + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Composite template examples
//     */
//    private static void runCompositeTemplateExamples() {
//        System.out.println("\n5. Composite Template Examples:");
//
//        // Create sections for a comprehensive analysis
//        StringPromptTemplate introSection = PromptTemplateBuilder
//            .string("Analyzing: {topic}\n" +
//                   "Analysis Date: {date}\n" +
//                   "Requested by: {requester}")
//            .description("Introduction section")
//            .build();
//
//        StringPromptTemplate summarySection = PromptTemplateBuilder
//            .string("EXECUTIVE SUMMARY:\nProvide a brief overview of {topic}")
//            .description("Summary section")
//            .build();
//
//        StringPromptTemplate detailsSection = PromptTemplateBuilder
//            .string("DETAILED ANALYSIS:\nAnalyze {topic} in depth, covering:\n" +
//                   "1. Current state\n2. Key challenges\n3. Opportunities")
//            .description("Details section")
//            .build();
//
//        StringPromptTemplate recommendationsSection = PromptTemplateBuilder
//            .string("RECOMMENDATIONS:\nProvide specific, actionable recommendations for {topic}")
//            .description("Recommendations section")
//            .build();
//
//        StringPromptTemplate conclusionSection = PromptTemplateBuilder
//            .string("CONCLUSION:\nSummarize the key takeaways and next steps for {topic}")
//            .description("Conclusion section")
//            .build();
//
//        // Create composite template
//        List<TemplateSection> sections = Arrays.asList(
//            new TemplateSection("intro", introSection),
//            new TemplateSection("summary", summarySection),
//            new TemplateSection("details", detailsSection,
//                vars -> Boolean.TRUE.equals(vars.get("include_details"))),
//            new TemplateSection("recommendations", recommendationsSection),
//            new TemplateSection("conclusion", conclusionSection,
//                vars -> Boolean.TRUE.equals(vars.get("include_conclusion")))
//        );
//
//        CompositePromptTemplate compositeTemplate = new CompositePromptTemplate(
//            sections, "Comprehensive analysis template", "\n\n"
//        );
//
//        Map<String, Object> compositeVars = Map.of(
//            "topic", "Remote Work Productivity",
//            "date", "March 2024",
//            "requester", "HR Department",
//            "include_details", true,
//            "include_conclusion", true
//        );
//
//        try {
//            String compositePrompt = compositeTemplate.format(compositeVars);
//            System.out.println("Composite analysis prompt:");
//            System.out.println(compositePrompt);
//
//            String compositeResponse = generateLLMResponse(compositePrompt);
//            System.out.println("\nComposite analysis response:");
//            System.out.println(compositeResponse.length() > 500 ?
//                compositeResponse.substring(0, 500) + "..." : compositeResponse);
//
//        } catch (Exception e) {
//            System.err.println("Composite template error: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Template validation examples
//     */
//    private static void runValidationExamples() {
//        System.out.println("\n6. Template Validation Examples:");
//
//        // Create validated template
//        Map<String, Validator> validators = Map.of(
//            "email", Validators.pattern("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"),
//            "age", value -> {
//                try {
//                    int age = Integer.parseInt(value.toString());
//                    if (age < 0 || age > 120) {
//                        return new PropertyValidationResult(false,
//                            List.of("Age must be between 0 and 120"), List.of());
//                    }
//                    return PropertyValidationResult.success();
//                } catch (NumberFormatException e) {
//                    return new PropertyValidationResult(false,
//                        List.of("Age must be a valid number"), List.of());
//                }
//            },
//            "message", Validators.notEmpty()
//        );
//
//        StringPromptTemplate baseTemplate = PromptTemplateBuilder
//            .string("Send a personalized message to {email}:\n" +
//                   "Recipient age: {age}\n" +
//                   "Message: {message}")
//            .description("Personalized message template")
//            .build();
//
//        ValidatedPromptTemplate validatedTemplate = new ValidatedPromptTemplate(
//            baseTemplate, validators
//        );
//
//        // Test with valid data
//        Map<String, Object> validData = Map.of(
//            "email", "john.doe@example.com",
//            "age", "30",
//            "message", "Happy birthday! Hope you have a wonderful day."
//        );
//
//        try {
//            String validPrompt = validatedTemplate.format(validData);
//            System.out.println("Valid template result: " + validPrompt);
//        } catch (Exception e) {
//            System.err.println("Validation error: " + e.getMessage());
//        }
//
//        // Test with invalid data
//        Map<String, Object> invalidData = Map.of(
//            "email", "invalid-email",
//            "age", "150",
//            "message", ""
//        );
//
//        try {
//            String invalidPrompt = validatedTemplate.format(invalidData);
//            System.out.println("This should not print: " + invalidPrompt);
//        } catch (Exception e) {
//            System.out.println("Expected validation error: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Template registry examples
//     */
//    private static void runRegistryExamples() {
//        System.out.println("\n7. Template Registry Examples:");
//
//        // Use built-in templates
//        try {
//            String translation = templateRegistry.format("translation", Map.of(
//                "text", "Good morning!",
//                "source_lang", "English",
//                "target_lang", "French"
//            ));
//            System.out.println("Registry translation: " + translation);
//
//            String response = generateLLMResponse(translation);
//            System.out.println("Translation result: " + response);
//
//        } catch (Exception e) {
//            System.err.println("Registry translation error: " + e.getMessage());
//        }
//
//        // Register custom template
//        StringPromptTemplate meetingTemplate = PromptTemplateBuilder
//            .string("Generate a meeting agenda for: {meeting_topic}\n" +
//                   "Duration: {duration} minutes\n" +
//                   "Participants: {participants}\n" +
//                   "Goals: {goals}")
//            .description("Meeting agenda generator")
//            .build();
//
//        templateRegistry.register("meeting_agenda", meetingTemplate, "business");
//
//        try {
//            String agenda = templateRegistry.format("meeting_agenda", Map.of(
//                "meeting_topic", "Q1 Planning Review",
//                "duration", "60",
//                "participants", "Development team, Product managers",
//                "goals", "Review Q1 objectives and plan Q2 initiatives"
//            ));
//            System.out.println("\nCustom meeting agenda template: " + agenda);
//
//        } catch (Exception e) {
//            System.err.println("Custom template error: " + e.getMessage());
//        }
//
//        // List templates by category
//        System.out.println("\nAvailable templates by category:");
//        List<String> businessTemplates = templateRegistry.getTemplatesByCategory("business");
//        System.out.println("Business templates: " + businessTemplates);
//    }
//
//    /**
//     * Advanced template patterns
//     */
//    private static void runAdvancedPatterns() {
//        System.out.println("\n8. Advanced Template Patterns:");
//
//        // Template inheritance simulation
//        StringPromptTemplate baseAnalysisTemplate = PromptTemplateBuilder
//            .string("Analyze the following {content_type}: {content}\n\n" +
//                   "Consider the following aspects:\n{analysis_aspects}")
//            .description("Base analysis template")
//            .build();
//
//        // Specialized templates that extend the base
//        Map<String, Object> codeAnalysisVars = Map.of(
//            "content_type", "Java code",
//            "content", "public void processData(List<String> data) { /* implementation */ }",
//            "analysis_aspects", "- Code structure\n- Performance implications\n- Best practices\n- Potential improvements"
//        );
//
//        Map<String, Object> textAnalysisVars = Map.of(
//            "content_type", "business document",
//            "content", "We are pleased to announce our Q1 results exceeded expectations...",
//            "analysis_aspects", "- Key messages\n- Tone and style\n- Target audience\n- Actionable items"
//        );
//
//        try {
//            System.out.println("Code analysis using base template:");
//            String codeAnalysis = baseAnalysisTemplate.format(codeAnalysisVars);
//            System.out.println(codeAnalysis);
//            System.out.println("Response: " + generateLLMResponse(codeAnalysis));
//
//            System.out.println("\nText analysis using base template:");
//            String textAnalysis = baseAnalysisTemplate.format(textAnalysisVars);
//            System.out.println(textAnalysis);
//            System.out.println("Response: " + generateLLMResponse(textAnalysis));
//
//        } catch (Exception e) {
//            System.err.println("Advanced pattern error: " + e.getMessage());
//        }
//
//        // Dynamic template generation
//        DynamicTemplateGenerator generator = new DynamicTemplateGenerator();
//
//        try {
//            PromptTemplate dynamicTemplate = generator.generateTemplate("email_response", Map.of(
//                "tone", "professional",
//                "purpose", "customer_support",
//                "length", "medium"
//            ));
//
//            String dynamicPrompt = dynamicTemplate.format(Map.of(
//                "customer_issue", "Product delivery delay",
//                "customer_name", "Sarah Johnson"
//            ));
//
//            System.out.println("\nDynamically generated template:");
//            System.out.println(dynamicPrompt);
//
//        } catch (Exception e) {
//            System.err.println("Dynamic template error: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Helper method to generate LLM response
//     */
//    private static String generateLLMResponse(String prompt) {
//        try {
//            // Truncate prompt if too long for demo
//            String truncatedPrompt = prompt.length() > 1000 ?
//                prompt.substring(0, 1000) + "..." : prompt;
//
//            AiMessage response = llm.generate(UserMessage.from(truncatedPrompt)).content();
//            return response.text();
//        } catch (Exception e) {
//            return "[LLM Error: " + e.getMessage() + "]";
//        }
//    }
//}
//
///**
// * Dynamic template generator for creating templates based on requirements
// */
//class DynamicTemplateGenerator {
//
//    public PromptTemplate generateTemplate(String templateType, Map<String, Object> requirements) {
//
//        switch (templateType.toLowerCase()) {
//            case "email_response":
//                return generateEmailTemplate(requirements);
//            case "code_review":
//                return generateCodeReviewTemplate(requirements);
//            case "content_creation":
//                return generateContentTemplate(requirements);
//            default:
//                return generateGenericTemplate(requirements);
//        }
//    }
//
//    private PromptTemplate generateEmailTemplate(Map<String, Object> requirements) {
//        String tone = (String) requirements.getOrDefault("tone", "professional");
//        String purpose = (String) requirements.getOrDefault("purpose", "general");
//        String length = (String) requirements.getOrDefault("length", "medium");
//
//        StringBuilder templateBuilder = new StringBuilder();
//
//        // Add tone instruction
//        switch (tone) {
//            case "friendly":
//                templateBuilder.append("Write a friendly and warm email response ");
//                break;
//            case "formal":
//                templateBuilder.append("Write a formal and professional email response ");
//                break;
//            default:
//                templateBuilder.append("Write a professional email response ");
//        }
//
//        // Add purpose context
//        switch (purpose) {
//            case "customer_support":
//                templateBuilder.append("for customer support regarding: {customer_issue}");
//                break;
//            case "business":
//                templateBuilder.append("for business communication about: {business_matter}");
//                break;
//            default:
//                templateBuilder.append("about: {subject}");
//        }
//
//        // Add length instruction
//        switch (length) {
//            case "brief":
//                templateBuilder.append("\nKeep the response brief and to the point.");
//                break;
//            case "detailed":
//                templateBuilder.append("\nProvide a detailed and comprehensive response.");
//                break;
//            default:
//                templateBuilder.append("\nProvide an appropriately detailed response.");
//        }
//
//        templateBuilder.append("\nRecipient: {customer_name}");
//
//        return new StringPromptTemplate(templateBuilder.toString(),
//            "Dynamically generated email template");
//    }
//
//    private PromptTemplate generateCodeReviewTemplate(Map<String, Object> requirements) {
//        String language = (String) requirements.getOrDefault("language", "Java");
//        String focus = (String) requirements.getOrDefault("focus", "general");
//
//        String template = String.format(
//            "Review this %s code with focus on %s:\n\n{code}\n\n" +
//            "Provide feedback on:\n" +
//            "1. Code quality\n" +
//            "2. Best practices\n" +
//            "3. Potential improvements\n" +
//            "4. Security considerations",
//            language, focus
//        );
//
//        return new StringPromptTemplate(template, "Dynamic code review template");
//    }
//
//    private PromptTemplate generateContentTemplate(Map<String, Object> requirements) {
//        String contentType = (String) requirements.getOrDefault("type", "article");
//        String audience = (String) requirements.getOrDefault("audience", "general");
//
//        String template = String.format(
//            "Create %s content for %s audience about: {topic}\n\n" +
//            "Requirements:\n" +
//            "- Engaging and informative\n" +
//            "- Appropriate for target audience\n" +
//            "- Well-structured and clear\n" +
//            "- Include relevant examples",
//            contentType, audience
//        );
//
//        return new StringPromptTemplate(template, "Dynamic content creation template");
//    }
//
//    private PromptTemplate generateGenericTemplate(Map<String, Object> requirements) {
//        return new StringPromptTemplate(
//            "Process the following request: {request}\n\nProvide a helpful and detailed response.",
//            "Generic dynamic template"
//        );
//    }
//}
//
///**
// * Mock implementations for demonstration
// */
//
//// StringPromptTemplate would be implemented as shown in the main README
//class StringPromptTemplate implements PromptTemplate {
//    private final String template;
//    private final String description;
//    private final Map<String, Object> optionalVars;
//
//    public StringPromptTemplate(String template, String description) {
//        this(template, description, Map.of());
//    }
//
//    public StringPromptTemplate(String template, String description, Map<String, Object> optionalVars) {
//        this.template = template;
//        this.description = description;
//        this.optionalVars = new HashMap<>(optionalVars);
//    }
//
//    @Override
//    public String format(Map<String, Object> variables) throws TemplateException {
//        String result = template;
//        Map<String, Object> allVars = new HashMap<>(optionalVars);
//        allVars.putAll(variables);
//
//        for (Map.Entry<String, Object> entry : allVars.entrySet()) {
//            result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
//        }
//        return result;
//    }
//
//    @Override
//    public Set<String> getRequiredVariables() {
//        Set<String> required = new HashSet<>();
//        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
//        java.util.regex.Matcher matcher = pattern.matcher(template);
//        while (matcher.find()) {
//            String var = matcher.group(1);
//            if (!optionalVars.containsKey(var)) {
//                required.add(var);
//            }
//        }
//        return required;
//    }
//
//    @Override
//    public Map<String, Object> getOptionalVariables() {
//        return new HashMap<>(optionalVars);
//    }
//
//    @Override
//    public String getDescription() {
//        return description;
//    }
//}
//
//// Additional mock classes would follow similar patterns...
//
///**
// * Simple prompt template registry
// */
//class PromptTemplateRegistry {
//    private final Map<String, PromptTemplate> templates = new HashMap<>();
//    private final Map<String, String> categories = new HashMap<>();
//
//    public PromptTemplateRegistry() {
//        initializeBuiltinTemplates();
//    }
//
//    private void initializeBuiltinTemplates() {
//        register("translation", new StringPromptTemplate(
//            "Translate the following text from {source_lang} to {target_lang}: {text}",
//            "Language translation"
//        ), "translation");
//
//        register("summarization", new StringPromptTemplate(
//            "Summarize this text in {max_sentences} sentences: {text}",
//            "Text summarization", Map.of("max_sentences", "3")
//        ), "analysis");
//    }
//
//    public void register(String name, PromptTemplate template, String category) {
//        templates.put(name, template);
//        categories.put(name, category);
//    }
//
//    public String format(String templateName, Map<String, Object> variables) throws TemplateException {
//        PromptTemplate template = templates.get(templateName);
//        if (template == null) {
//            throw new TemplateException("Template not found: " + templateName);
//        }
//        return template.format(variables);
//    }
//
//    public List<String> getTemplatesByCategory(String category) {
//        return categories.entrySet().stream()
//            .filter(entry -> category.equals(entry.getValue()))
//            .map(Map.Entry::getKey)
//            .collect(java.util.stream.Collectors.toList());
//    }
//}
