# LangChain Basics (Java)

## 🎯 Overview
This section covers the fundamental concepts of LangChain4j, providing the foundation for building AI-powered applications. You'll learn about core components, basic usage patterns, and essential concepts that form the building blocks of more advanced features.

## 🧠 Core LangChain Concepts

### What is LangChain4j?
LangChain4j is a Java framework that simplifies building applications with Large Language Models (LLMs). It provides:
- **Standardized Interfaces**: Common APIs for different LLM providers
- **Chain Composition**: Link multiple operations together
- **Memory Management**: Maintain context across interactions
- **Tool Integration**: Extend LLM capabilities with external functions
- **Production Features**: Error handling, retries, monitoring

### Key Components
1. **Language Models**: Interface with various LLM providers
2. **Prompts**: Template and manage prompts effectively
3. **Memory**: Store and retrieve conversation context
4. **Chains**: Combine multiple components into workflows
5. **Agents**: Autonomous entities that can reason and act
6. **Tools**: External capabilities that agents can use

## 🏗️ Basic Architecture

### Simple LLM Call
```
User Input → Prompt → LLM → Response → User
```

### Chain Pattern
```
Input → Component 1 → Component 2 → Component 3 → Output
```

### Agent Pattern
```
User Query → Agent → Tool Selection → Tool Execution → Response
```

## 💻 Java Implementation

### Setting Up LangChain4j
First, ensure your Maven project has the necessary dependencies (already included in our main pom.xml):

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>0.25.0</version>
</dependency>

<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>0.25.0</version>
</dependency>
```

### Basic Components
```java
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.Response;

/**
 * Basic LangChain4j concepts and usage patterns
 */
public class LangChainBasics {
    
    /**
     * Initialize a basic chat model
     */
    public static ChatLanguageModel createChatModel() {
        return OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-3.5-turbo")
            .temperature(0.7)
            .maxTokens(1000)
            .build();
    }
    
    /**
     * Simple LLM interaction
     */
    public static String simpleChat(ChatLanguageModel model, String userMessage) {
        try {
            UserMessage message = UserMessage.from(userMessage);
            Response<AiMessage> response = model.generate(message);
            return response.content().text();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Multi-turn conversation
     */
    public static String multiTurnChat(ChatLanguageModel model, String... messages) {
        try {
            List<dev.langchain4j.data.message.ChatMessage> conversation = new ArrayList<>();
            
            // Add all messages alternating between user and AI
            for (int i = 0; i < messages.length; i++) {
                if (i % 2 == 0) {
                    conversation.add(UserMessage.from(messages[i]));
                } else {
                    conversation.add(AiMessage.from(messages[i]));
                }
            }
            
            // If last message is from user, add a new user message
            if (messages.length % 2 != 0) {
                // Last message was user message, generate response
                Response<AiMessage> response = model.generate(conversation);
                return response.content().text();
            } else {
                return "Conversation should end with a user message";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

/**
 * LangChain error handling patterns
 */
class ErrorHandlingPatterns {
    
    /**
     * Robust LLM call with retry logic
     */
    public static String robustLLMCall(ChatLanguageModel model, String prompt, int maxRetries) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            try {
                Response<AiMessage> response = model.generate(UserMessage.from(prompt));
                return response.content().text();
            } catch (Exception e) {
                lastException = e;
                attempts++;
                
                // Wait before retry with exponential backoff
                try {
                    Thread.sleep(1000L * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        return "Failed after " + maxRetries + " attempts. Last error: " + 
               (lastException != null ? lastException.getMessage() : "Unknown error");
    }
    
    /**
     * LLM call with timeout
     */
    public static String llmCallWithTimeout(ChatLanguageModel model, String prompt, long timeoutMs) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Response<AiMessage> response = model.generate(UserMessage.from(prompt));
                return response.content().text();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "Request timed out after " + timeoutMs + "ms";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

/**
 * Configuration and environment management
 */
class ConfigurationManager {
    
    private final Map<String, String> config;
    
    public ConfigurationManager() {
        this.config = loadConfiguration();
    }
    
    /**
     * Load configuration from environment variables and properties
     */
    private Map<String, String> loadConfiguration() {
        Map<String, String> config = new HashMap<>();
        
        // Load from environment variables
        config.put("openai.api.key", getEnvOrDefault("OPENAI_API_KEY", ""));
        config.put("openai.model", getEnvOrDefault("OPENAI_MODEL", "gpt-3.5-turbo"));
        config.put("openai.temperature", getEnvOrDefault("OPENAI_TEMPERATURE", "0.7"));
        config.put("openai.max.tokens", getEnvOrDefault("OPENAI_MAX_TOKENS", "1000"));
        
        // Load from system properties (overrides env vars)
        config.putAll(loadFromSystemProperties());
        
        return config;
    }
    
    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
    
    private Map<String, String> loadFromSystemProperties() {
        Map<String, String> props = new HashMap<>();
        
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("langchain.") || keyStr.startsWith("openai.")) {
                props.put(keyStr, value.toString());
            }
        });
        
        return props;
    }
    
    /**
     * Create configured chat model
     */
    public ChatLanguageModel createConfiguredChatModel() {
        String apiKey = config.get("openai.api.key");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        
        return OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(config.get("openai.model"))
            .temperature(Double.parseDouble(config.get("openai.temperature")))
            .maxTokens(Integer.parseInt(config.get("openai.max.tokens")))
            .build();
    }
    
    public String getConfig(String key) {
        return config.get(key);
    }
    
    public void setConfig(String key, String value) {
        config.put(key, value);
    }
}

/**
 * Utility classes for common operations
 */
class LangChainUtils {
    
    /**
     * Validate LLM response quality
     */
    public static boolean isValidResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }
        
        // Check for common error patterns
        String lower = response.toLowerCase();
        if (lower.contains("i cannot") || 
            lower.contains("i don't know") ||
            lower.contains("error") ||
            lower.length() < 10) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Extract key information from LLM response
     */
    public static Map<String, String> extractKeyValuePairs(String response) {
        Map<String, String> pairs = new HashMap<>();
        
        // Simple regex pattern for key: value pairs
        Pattern pattern = Pattern.compile("(\\w+):\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase();
            String value = matcher.group(2).trim();
            pairs.put(key, value);
        }
        
        return pairs;
    }
    
    /**
     * Clean and format text for LLM processing
     */
    public static String cleanText(String text) {
        if (text == null) return "";
        
        return text
            .replaceAll("\\s+", " ")  // Normalize whitespace
            .replaceAll("[\\x00-\\x1F\\x7F]", "") // Remove control characters
            .trim();
    }
    
    /**
     * Truncate text to fit token limits
     */
    public static String truncateToTokenLimit(String text, int maxTokens) {
        // Rough estimation: 1 token ≈ 4 characters
        int maxChars = maxTokens * 4;
        
        if (text.length() <= maxChars) {
            return text;
        }
        
        // Find a good breaking point (end of sentence)
        int breakPoint = text.lastIndexOf('.', maxChars);
        if (breakPoint == -1) {
            breakPoint = text.lastIndexOf(' ', maxChars);
        }
        
        if (breakPoint == -1) {
            breakPoint = maxChars;
        }
        
        return text.substring(0, breakPoint) + "...";
    }
}

/**
 * Logging and monitoring utilities
 */
class LangChainMonitoring {
    
    private static final Logger logger = LoggerFactory.getLogger(LangChainMonitoring.class);
    
    /**
     * Log LLM interactions for debugging
     */
    public static void logInteraction(String prompt, String response, long durationMs) {
        logger.info("LLM Interaction - Duration: {}ms, Prompt length: {}, Response length: {}", 
                   durationMs, prompt.length(), response.length());
        
        if (logger.isDebugEnabled()) {
            logger.debug("Prompt: {}", truncateForLogging(prompt, 200));
            logger.debug("Response: {}", truncateForLogging(response, 200));
        }
    }
    
    /**
     * Monitor token usage
     */
    public static void logTokenUsage(int promptTokens, int responseTokens, double cost) {
        logger.info("Token Usage - Prompt: {}, Response: {}, Total: {}, Cost: ${:.4f}", 
                   promptTokens, responseTokens, promptTokens + responseTokens, cost);
    }
    
    /**
     * Log errors with context
     */
    public static void logError(String operation, String context, Exception error) {
        logger.error("LangChain Error - Operation: {}, Context: {}, Error: {}", 
                    operation, context, error.getMessage(), error);
    }
    
    private static String truncateForLogging(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
```

## 🚀 Getting Started Examples

### Example 1: Simple Chat
```java
public class SimpleChatExample {
    public static void main(String[] args) {
        // Create model
        ChatLanguageModel model = LangChainBasics.createChatModel();
        
        // Simple interaction
        String response = LangChainBasics.simpleChat(model, "Hello, how are you?");
        System.out.println("AI: " + response);
    }
}
```

### Example 2: Configuration-Based Setup
```java
public class ConfiguredChatExample {
    public static void main(String[] args) {
        try {
            // Use configuration manager
            ConfigurationManager config = new ConfigurationManager();
            ChatLanguageModel model = config.createConfiguredChatModel();
            
            // Chat with configured model
            String response = LangChainBasics.simpleChat(model, 
                "Explain the concept of artificial intelligence in simple terms.");
            System.out.println("AI: " + response);
            
        } catch (Exception e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }
}
```

### Example 3: Robust Error Handling
```java
public class RobustChatExample {
    public static void main(String[] args) {
        ConfigurationManager config = new ConfigurationManager();
        ChatLanguageModel model = config.createConfiguredChatModel();
        
        String prompt = "What are the benefits of renewable energy?";
        
        // Try with retry logic
        String response = ErrorHandlingPatterns.robustLLMCall(model, prompt, 3);
        System.out.println("Response: " + response);
        
        // Validate response quality
        if (LangChainUtils.isValidResponse(response)) {
            System.out.println("Response quality: Good");
        } else {
            System.out.println("Response quality: Poor - may need retry");
        }
    }
}
```

## 🔧 Best Practices

1. **Configuration Management**
   - Use environment variables for API keys
   - Implement configuration validation
   - Support multiple environments (dev, staging, prod)
   - Use secure credential storage

2. **Error Handling**
   - Implement retry logic with exponential backoff
   - Set appropriate timeouts
   - Log errors with sufficient context
   - Provide meaningful fallback responses

3. **Performance**
   - Monitor token usage and costs
   - Cache responses where appropriate
   - Use appropriate model sizes for tasks
   - Implement connection pooling

4. **Security**
   - Never hardcode API keys
   - Validate and sanitize inputs
   - Implement rate limiting
   - Log security-relevant events

5. **Monitoring**
   - Log all LLM interactions
   - Track performance metrics
   - Monitor error rates
   - Set up alerts for anomalies

## 🔗 Integration Patterns

### Spring Boot Integration
```java
@Configuration
public class LangChainConfiguration {
    
    @Value("${langchain.openai.api-key}")
    private String openAiApiKey;
    
    @Bean
    public ChatLanguageModel chatModel() {
        return OpenAiChatModel.builder()
            .apiKey(openAiApiKey)
            .modelName("gpt-3.5-turbo")
            .temperature(0.7)
            .build();
    }
}
```

### Service Layer Pattern
```java
@Service
public class AIService {
    
    private final ChatLanguageModel chatModel;
    
    public AIService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }
    
    public String generateResponse(String prompt) {
        return LangChainBasics.simpleChat(chatModel, prompt);
    }
}
```

## 📚 What's Next?

After mastering these basics, you can explore:
- **[Session Management](../02-session/)** - Managing user conversations
- **[Prompt Templates](../03-prompts/)** - Structured prompt creation
- **[Memory Systems](../04-memory/)** - Adding context and history
- **[Tools Integration](../05-tools/)** - Extending LLM capabilities

---

*This foundation will prepare you for building sophisticated AI applications with LangChain4j.*
