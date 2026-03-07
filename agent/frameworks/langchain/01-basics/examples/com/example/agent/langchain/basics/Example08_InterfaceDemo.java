/**
 * Example 8: Interface and Implementation Demo
 *
 * This class demonstrates how ChatLanguageModel (interface) and OpenAiChatModel
 * (implementation) work together, and why this design is so powerful.
 *
 * Learning Objectives:
 * - Understand interfaces vs implementations
 * - See polymorphism in action
 * - Learn why this design allows model swapping
 * - Understand the builder pattern
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.output.Response;
import java.util.List;

/**
 * Executable class demonstrating interface and implementation concepts
 */
public class Example08_InterfaceDemo {

    public static void main(String[] args) {
        System.out.println("=== Interface and Implementation Demo ===\n");

        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        try {
            // Demo 1: Show interface vs implementation
            demonstrateInterfaceVsImplementation();

            System.out.println("\n" + "=".repeat(60) + "\n");

            // Demo 2: Show polymorphism (one interface, multiple implementations)
            demonstratePolymorphism();

            System.out.println("\n" + "=".repeat(60) + "\n");

            // Demo 3: Show why this design is powerful
            demonstrateFlexibility();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Demonstrate the difference between interface and implementation
     */
    private static void demonstrateInterfaceVsImplementation() {
        System.out.println("🔍 INTERFACE vs IMPLEMENTATION");
        System.out.println();

        System.out.println("Step 1: Creating an OpenAI model using the builder pattern");

        // This shows the builder pattern in action
        System.out.println("OpenAiChatModel.builder() // Returns a builder");
        System.out.println("  .apiKey(...) // Configure the builder");
        System.out.println("  .modelName(...) // More configuration");
        System.out.println("  .build(); // Create the actual model");
        System.out.println();

        // The actual creation
        OpenAiChatModel concreteModel = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-4o-mini")
            .temperature(0.7)
            .build();

        System.out.println("✅ Created: OpenAiChatModel (concrete implementation)");
        System.out.println("Type: " + concreteModel.getClass().getSimpleName());
        System.out.println();

        System.out.println("Step 2: Converting to interface reference");

        // This is what usually happens automatically
        ChatLanguageModel interfaceReference = concreteModel;

        System.out.println("✅ Converted: ChatLanguageModel (interface reference)");
        System.out.println("Type: " + interfaceReference.getClass().getSimpleName());
        System.out.println("Interface: " + ChatLanguageModel.class.getSimpleName());
        System.out.println();

        System.out.println("🔑 Key Insight:");
        System.out.println("- OpenAiChatModel is the actual implementation");
        System.out.println("- ChatLanguageModel is the interface it implements");
        System.out.println("- Java automatically converts implementation to interface");
        System.out.println("- You can use either reference to call the same methods!");
    }

    /**
     * Demonstrate polymorphism - one interface, multiple implementations
     */
    private static void demonstratePolymorphism() {
        System.out.println("🎭 POLYMORPHISM IN ACTION");
        System.out.println();

        System.out.println("Creating different models that all implement ChatLanguageModel:");
        System.out.println();

        // All these variables have the same type (ChatLanguageModel)
        // but point to different implementations

        System.out.println("1. Creating OpenAI model...");
        ChatLanguageModel openAiModel = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-4o-mini")
            .temperature(0.7)
            .build();
        System.out.println("   ✅ Type: " + openAiModel.getClass().getSimpleName());

        // Note: We're not actually creating other models here since we'd need their API keys
        // This is just to show the concept
        System.out.println();
        System.out.println("2. Could create Anthropic model (if we had API key):");
        System.out.println("   ChatLanguageModel claudeModel = AnthropicChatModel.builder()...");
        System.out.println("   Would be type: AnthropicChatModel");

        System.out.println();
        System.out.println("3. Could create Google model (if we had API key):");
        System.out.println("   ChatLanguageModel geminiModel = GoogleAiGeminiChatModel.builder()...");
        System.out.println("   Would be type: GoogleAiGeminiChatModel");

        System.out.println();
        System.out.println("🔑 Key Insight:");
        System.out.println("All these different implementations can be used the same way:");

        // Demonstrate that the same code works with any implementation
        String question = "What is 2+2?";
        System.out.println();
        System.out.println("Asking: \"" + question + "\"");

        try {
            String response = askModel(openAiModel, question);
            System.out.println("Response: " + response);
        } catch (Exception e) {
            System.out.println("Response: Error - " + e.getMessage());
        }

        System.out.println();
        System.out.println("The askModel() method works with ANY ChatLanguageModel implementation!");
    }

    /**
     * Demonstrate the flexibility this design provides
     */
    private static void demonstrateFlexibility() {
        System.out.println("🚀 FLEXIBILITY BENEFITS");
        System.out.println();

        System.out.println("1. Easy Model Switching:");
        System.out.println("   ChatService service = new ChatService();");
        System.out.println("   service.setModel(openAiModel);    // Use OpenAI");
        System.out.println("   service.setModel(claudeModel);    // Switch to Claude");
        System.out.println("   service.setModel(geminiModel);    // Switch to Gemini");
        System.out.println("   // Same service code works with all models!");
        System.out.println();

        System.out.println("2. Configuration-Based Model Selection:");
        ChatService service = new ChatService();

        // Simulate different configurations
        String[] providers = {"openai", "anthropic", "google"};
        for (String provider : providers) {
            System.out.println("   Config: provider=" + provider);
            try {
                ChatLanguageModel model = createModelForProvider(provider);
                service.setModel(model);
                System.out.println("   ✅ Service configured with " +
                    model.getClass().getSimpleName());
            } catch (Exception e) {
                System.out.println("   ❌ " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("3. Testing Benefits:");
        System.out.println("   // Easy to create mock models for testing");
        System.out.println("   ChatLanguageModel mockModel = new MockChatModel();");
        System.out.println("   service.setModel(mockModel);");
        System.out.println("   // Test your logic without API calls!");

        System.out.println();
        System.out.println("🎯 This design pattern allows:");
        System.out.println("✅ Switching AI providers without code changes");
        System.out.println("✅ Testing without real API calls");
        System.out.println("✅ Adding new providers easily");
        System.out.println("✅ Configuring models at runtime");
        System.out.println("✅ Writing provider-independent code");
    }

    /**
     * Helper method that works with ANY ChatLanguageModel implementation
     * This demonstrates polymorphism - same method, different implementations
     */
    private static String askModel(ChatLanguageModel model, String question) {
        try {
            return model.generate(UserMessage.from(question)).content().text();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Factory method to create different model types
     * This shows how you might select models based on configuration
     */
    private static ChatLanguageModel createModelForProvider(String provider) {
        switch (provider.toLowerCase()) {
            case "openai":
                return OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("gpt-4o-mini")
                    .build();

            case "anthropic":
                throw new RuntimeException("Anthropic API key not configured");

            case "google":
                throw new RuntimeException("Google API key not configured");

            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }
}

/**
 * Simple service class that demonstrates how to use ChatLanguageModel interface
 */
class ChatService {
    private ChatLanguageModel model;

    public void setModel(ChatLanguageModel model) {
        this.model = model;
        System.out.println("     Service now using: " + model.getClass().getSimpleName());
    }

    public String ask(String question) {
        if (model == null) {
            return "No model configured";
        }
        try {
            return model.generate(UserMessage.from(question)).content().text();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

/**
 * Example of a mock implementation for testing
 */
class MockChatModel implements ChatLanguageModel {

    public Response<AiMessage> generate(UserMessage userMessage) {
        return new Response<>(
            AiMessage.from("Mock response to: " + userMessage.singleText())
        );
    }

    public Response<AiMessage> generate(List<ChatMessage> messages) {
        return new Response<>(
            AiMessage.from("Mock response to conversation")
        );
    }
}
