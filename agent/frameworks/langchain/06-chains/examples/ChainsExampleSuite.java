/**
 * LangChain Chains Examples - Complete Implementation
 * Demonstrates various chain types and composition patterns
 */
package com.example.agent.langchain.chains;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Comprehensive chains demonstration
 */
public class ChainsExampleSuite {

    private static ChatLanguageModel llm;

    public static void main(String[] args) {
        System.out.println("=== LangChain Chains Examples ===");

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

        try {
            // Run all chain examples
            runBasicChainExamples();
            runSequentialChainExamples();
            runConditionalChainExamples();
            runParallelChainExamples();
            runAdvancedChainExamples();

        } catch (Exception e) {
            System.err.println("Error running chain examples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Basic chain operations
     */
    private static void runBasicChainExamples() {
        System.out.println("\n1. Basic Chain Examples:");

        // Simple LLM Chain
        PromptTemplate template = new StringPromptTemplate(
            "Translate the following text to {target_language}: {text}",
            "Translation template"
        );

        LLMChain translationChain = new LLMChain(llm, template);

        Map<String, Object> variables = Map.of(
            "text", "Hello, how are you?",
            "target_language", "Spanish"
        );

        try {
            ChainResult<String> result = translationChain.execute(variables);
            System.out.println("Translation result: " + result.getResult());
            System.out.println("Execution time: " + result.getExecutionTimeMs() + "ms");
        } catch (Exception e) {
            System.err.println("Translation chain error: " + e.getMessage());
        }

        // Conversation Chain with Memory
        ConversationBufferMemory memory = new ConversationBufferMemory(10);
        PromptTemplate conversationTemplate = new StringPromptTemplate(
            "Previous conversation:\n{history}\n\nHuman: {input}\nAI:",
            "Conversation template"
        );

        ConversationChain conversationChain = new ConversationChain(llm, memory, conversationTemplate);

        String[] inputs = {
            "Hi, my name is Alice",
            "What's my name?",
            "What did we talk about?"
        };

        for (String input : inputs) {
            try {
                ChainResult<String> result = conversationChain.execute(input);
                System.out.println("Human: " + input);
                System.out.println("AI: " + result.getResult());
                System.out.println("---");
            } catch (Exception e) {
                System.err.println("Conversation chain error: " + e.getMessage());
            }
        }
    }

    /**
     * Sequential chain examples
     */
    private static void runSequentialChainExamples() {
        System.out.println("\n2. Sequential Chain Examples:");

        // Create individual chains
        Chain<String, String> summaryChain = new LLMChain(llm,
            new StringPromptTemplate("Summarize this text in one sentence: {input}", "Summary"));

        Chain<String, String> sentimentChain = new LLMChain(llm,
            new StringPromptTemplate("Analyze the sentiment of this text (positive/negative/neutral): {input}", "Sentiment"));

        Chain<String, String> keywordsChain = new LLMChain(llm,
            new StringPromptTemplate("Extract 3 key topics from this text: {input}", "Keywords"));

        // Create sequential chain
        SequentialChain sequentialChain = new SequentialChain(
            List.of(summaryChain, sentimentChain, keywordsChain), true);

        String longText = "Artificial Intelligence has revolutionized the way we work and live. " +
                         "From healthcare to transportation, AI is making significant improvements " +
                         "in efficiency and accuracy. However, there are also concerns about job " +
                         "displacement and ethical implications that need to be addressed.";

        try {
            ChainResult<String> result = sequentialChain.execute(longText);
            System.out.println("Sequential analysis result: " + result.getResult());
            System.out.println("Metadata: " + result.getMetadata());
        } catch (Exception e) {
            System.err.println("Sequential chain error: " + e.getMessage());
        }
    }

    /**
     * Conditional chain routing examples
     */
    private static void runConditionalChainExamples() {
        System.out.println("\n3. Conditional Chain Examples:");

        // Create specialized chains for different content types
        Chain<String, String> codeChain = new LLMChain(llm,
            new StringPromptTemplate("Explain this code and its purpose: {input}", "Code explanation"));

        Chain<String, String> textChain = new LLMChain(llm,
            new StringPromptTemplate("Provide a detailed analysis of this text: {input}", "Text analysis"));

        Chain<String, String> mathChain = new LLMChain(llm,
            new StringPromptTemplate("Solve this mathematical problem step by step: {input}", "Math solver"));

        Chain<String, String> generalChain = new LLMChain(llm,
            new StringPromptTemplate("Respond to this query: {input}", "General response"));

        // Create routing map
        Map<String, Chain<String, String>> routingMap = Map.of(
            "code", codeChain,
            "text", textChain,
            "math", mathChain
        );

        RouterChain routerChain = new RouterChain(llm, routingMap, generalChain);

        String[] testInputs = {
            "public class HelloWorld { public static void main(String[] args) { System.out.println(\"Hello\"); } }",
            "What is the capital of France?",
            "Solve: 2x + 5 = 15",
            "The quick brown fox jumps over the lazy dog. This is a sample text for analysis."
        };

        for (String input : testInputs) {
            try {
                ChainResult<String> result = routerChain.execute(input);
                System.out.println("Input: " + (input.length() > 50 ? input.substring(0, 50) + "..." : input));
                System.out.println("Routed response: " + result.getResult());
                System.out.println("---");
            } catch (Exception e) {
                System.err.println("Router chain error: " + e.getMessage());
            }
        }
    }

    /**
     * Parallel chain execution examples
     */
    private static void runParallelChainExamples() {
        System.out.println("\n4. Parallel Chain Examples:");

        // Create multiple analysis chains
        Chain<String, String> sentimentAnalysis = new LLMChain(llm,
            new StringPromptTemplate("Analyze sentiment: {input}", "Sentiment"));

        Chain<String, String> topicExtraction = new LLMChain(llm,
            new StringPromptTemplate("Extract main topics: {input}", "Topics"));

        Chain<String, String> languageDetection = new LLMChain(llm,
            new StringPromptTemplate("Detect the language: {input}", "Language"));

        Chain<String, String> lengthAnalysis = new TransformChain<>("length_analysis",
            "Calculate text statistics",
            text -> "Length: " + text.length() + " characters, Words: " + text.split("\\s+").length);

        // Create parallel chain
        ParallelChain parallelChain = new ParallelChain(
            List.of(sentimentAnalysis, topicExtraction, languageDetection, lengthAnalysis));

        String sampleText = "¡Hola! Este es un texto de ejemplo en español. " +
                           "Estoy muy emocionado de probar esta funcionalidad.";

        try {
            ChainResult<List<String>> result = parallelChain.execute(sampleText);
            System.out.println("Parallel analysis results:");
            for (int i = 0; i < result.getResult().size(); i++) {
                System.out.println("Analysis " + (i + 1) + ": " + result.getResult().get(i));
            }
            System.out.println("Total execution time: " + result.getExecutionTimeMs() + "ms");
        } catch (Exception e) {
            System.err.println("Parallel chain error: " + e.getMessage());
        }
    }

    /**
     * Advanced chain patterns
     */
    private static void runAdvancedChainExamples() {
        System.out.println("\n5. Advanced Chain Examples:");

        // Retry Chain Example
        Chain<String, String> unreliableChain = new LLMChain(llm,
            new StringPromptTemplate("Generate a creative story about: {input}", "Story generation"));

        RetryChain<String, String> retryChain = new RetryChain<>(unreliableChain, 3, 1000,
            exception -> !exception.getMessage().contains("timeout"));

        try {
            ChainResult<String> result = retryChain.execute("space exploration");
            System.out.println("Retry chain result: " +
                (result.getResult().length() > 100 ?
                 result.getResult().substring(0, 100) + "..." : result.getResult()));
        } catch (Exception e) {
            System.err.println("Retry chain error: " + e.getMessage());
        }

        // Chain Builder Example
        ChainBuilder builder = new ChainBuilder(llm);

        // Build complex chain using fluent API
        Chain<String, String> complexChain = builder.sequential(
            builder.llm(new StringPromptTemplate("Step 1 - Analyze: {input}", "Analysis")),
            new TransformChain<>("formatting", "Format output",
                text -> "PROCESSED: " + text.toUpperCase()),
            builder.llm(new StringPromptTemplate("Step 2 - Summarize: {input}", "Summary"))
        );

        try {
            ChainResult<String> result = complexChain.execute("The benefits of renewable energy");
            System.out.println("Complex chain result: " + result.getResult());
        } catch (Exception e) {
            System.err.println("Complex chain error: " + e.getMessage());
        }

        // Text Processing Chain Example
        TextProcessingChain textProcessor = new TextProcessingChain(llm);

        String documentText = "Climate change is one of the most pressing issues of our time. " +
                             "Scientists around the world are working on solutions including " +
                             "renewable energy, carbon capture, and sustainable transportation.";

        try {
            ChainResult<Map<String, Object>> result = textProcessor.execute(documentText);
            System.out.println("Text processing results:");
            result.getResult().forEach((key, value) ->
                System.out.println("  " + key + ": " + value));
        } catch (Exception e) {
            System.err.println("Text processing error: " + e.getMessage());
        }
    }
}

/**
 * Mock implementations for demonstration
 */
class MockPromptTemplate implements PromptTemplate {
    private final String template;
    private final String description;

    public MockPromptTemplate(String template, String description) {
        this.template = template;
        this.description = description;
    }

    @Override
    public String format(Map<String, Object> variables) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return result;
    }

    @Override
    public Set<String> getRequiredVariables() {
        Set<String> vars = new HashSet<>();
        // Simple extraction - in real implementation would be more sophisticated
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{(\\w+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return vars;
    }

    @Override
    public Map<String, Object> getOptionalVariables() {
        return new HashMap<>();
    }

    @Override
    public String getDescription() {
        return description;
    }
}

/**
 * Mock memory implementation
 */
class ConversationBufferMemory {
    private final List<String> messages;
    private final int maxMessages;

    public ConversationBufferMemory(int maxMessages) {
        this.maxMessages = maxMessages;
        this.messages = new ArrayList<>();
    }

    public void addMessage(UserMessage message) {
        messages.add("Human: " + message.text());
        trimMessages();
    }

    public void addMessage(AiMessage message) {
        messages.add("AI: " + message.text());
        trimMessages();
    }

    private void trimMessages() {
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    public String getFormattedHistory() {
        return String.join("\n", messages);
    }
}

/**
 * Example chain implementations for demonstration
 */
class ExampleChainImplementations {

    /**
     * Simple document analysis chain
     */
    public static class DocumentAnalysisChain extends BaseChain<String, Map<String, Object>> {

        private final ChatLanguageModel llm;

        public DocumentAnalysisChain(ChatLanguageModel llm) {
            super("DocumentAnalysis", "Comprehensive document analysis");
            this.llm = llm;
        }

        @Override
        protected Map<String, Object> executeInternal(String document) throws Exception {
            Map<String, Object> analysis = new HashMap<>();

            // Basic statistics
            analysis.put("length", document.length());
            analysis.put("word_count", document.split("\\s+").length);
            analysis.put("sentence_count", document.split("[.!?]+").length);

            // LLM-based analysis
            String summaryPrompt = "Summarize this document in one sentence: " + document;
            AiMessage summary = llm.generate(UserMessage.from(summaryPrompt)).content();
            analysis.put("summary", summary.text());

            String topicsPrompt = "List the main topics in this document: " + document;
            AiMessage topics = llm.generate(UserMessage.from(topicsPrompt)).content();
            analysis.put("topics", topics.text());

            return analysis;
        }
    }

    /**
     * Multi-language processing chain
     */
    public static class MultiLanguageChain extends BaseChain<Map<String, Object>, Map<String, Object>> {

        private final ChatLanguageModel llm;

        public MultiLanguageChain(ChatLanguageModel llm) {
            super("MultiLanguage", "Multi-language text processing");
            this.llm = llm;
        }

        @Override
        protected Map<String, Object> executeInternal(Map<String, Object> input) throws Exception {
            String text = (String) input.get("text");
            String targetLanguage = (String) input.getOrDefault("target_language", "English");

            Map<String, Object> result = new HashMap<>();

            // Detect source language
            String detectPrompt = "What language is this text written in? " + text;
            AiMessage detected = llm.generate(UserMessage.from(detectPrompt)).content();
            result.put("detected_language", detected.text());

            // Translate if needed
            if (!detected.text().toLowerCase().contains(targetLanguage.toLowerCase())) {
                String translatePrompt = "Translate this text to " + targetLanguage + ": " + text;
                AiMessage translated = llm.generate(UserMessage.from(translatePrompt)).content();
                result.put("translated_text", translated.text());
            } else {
                result.put("translated_text", text);
            }

            result.put("original_text", text);
            result.put("target_language", targetLanguage);

            return result;
        }
    }

    /**
     * Code analysis chain
     */
    public static class CodeAnalysisChain extends BaseChain<String, Map<String, Object>> {

        private final ChatLanguageModel llm;

        public CodeAnalysisChain(ChatLanguageModel llm) {
            super("CodeAnalysis", "Source code analysis and documentation");
            this.llm = llm;
        }

        @Override
        protected Map<String, Object> executeInternal(String code) throws Exception {
            Map<String, Object> analysis = new HashMap<>();

            // Basic code metrics
            analysis.put("lines_of_code", code.split("\n").length);
            analysis.put("contains_comments", code.contains("//") || code.contains("/*"));

            // Detect programming language
            String langPrompt = "What programming language is this code written in? " + code;
            AiMessage language = llm.generate(UserMessage.from(langPrompt)).content();
            analysis.put("language", language.text());

            // Explain functionality
            String explainPrompt = "Explain what this code does: " + code;
            AiMessage explanation = llm.generate(UserMessage.from(explainPrompt)).content();
            analysis.put("explanation", explanation.text());

            // Suggest improvements
            String improvePrompt = "Suggest improvements for this code: " + code;
            AiMessage improvements = llm.generate(UserMessage.from(improvePrompt)).content();
            analysis.put("improvements", improvements.text());

            return analysis;
        }
    }
}
