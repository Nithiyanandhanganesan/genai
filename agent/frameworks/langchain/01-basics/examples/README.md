# LangChain4j Basics Examples

Welcome to the LangChain4j basics examples! This directory contains **5 separate executable Java classes**, each focusing on a specific concept, plus a **shared utility class** for configuration. This makes it much easier to learn one concept at a time.

## 🎯 How to Use These Examples

Each example is a complete, runnable Java program that you can execute independently. They all use the **`ConfigurationUtil.java`** class for clean, reusable configuration management.

### Prerequisites
1. Set your OpenAI API key as an environment variable:
   ```bash
   export OPENAI_API_KEY=your_api_key_here
   ```
2. Make sure you have LangChain4j dependencies in your project (already included in our pom.xml)

## 🔧 Configuration Utility Class

**`ConfigurationUtil.java`** - This is a shared utility class that handles:
- ✅ **API Key Management** - Safely loads API keys from environment variables
- ⚙️ **Model Configuration** - Creates and configures ChatLanguageModel instances  
- 🔍 **Validation** - Validates all settings (temperature, tokens, timeouts)
- 🎯 **Convenience Methods** - Static methods for quick model creation
- 🛡️ **Security** - Never exposes sensitive information like API keys

**Why use a utility class?**
- **Consistent Configuration** - All examples use the same configuration logic
- **Less Code Duplication** - Write configuration once, use everywhere
- **Easy Maintenance** - Update configuration in one place
- **Better Organization** - Separates configuration from learning examples

## 📚 Example Classes Overview

### 1. **Example01_ConfigurationManager.java** 
🔧 **Configuration Basics**
- **What you'll learn**: How to use the ConfigurationUtil class for managing API keys and settings
- **Key concepts**: Environment variables, configuration validation, utility class usage
- **Run this first** to understand how configuration works across all examples

**What it demonstrates:**
- Using ConfigurationUtil for API key validation
- Creating models with default vs custom settings
- Static methods vs instance methods for configuration
- How utility classes organize code better

### 2. **Example02_SimpleChat.java**
💬 **Your First AI Conversation**
- **What you'll learn**: Basic request-response pattern with AI
- **Key concepts**: Using ConfigurationUtil, sending messages, receiving responses
- **Perfect starting point** for understanding how AI chat works

**What it demonstrates:**
- Quick API key checking with ConfigurationUtil.isApiKeyAvailable()
- Creating chat models with ConfigurationUtil.createDefaultChatModel()
- Sending questions to AI models
- Measuring response times and analyzing responses

### 3. **Example03_MultiTurnConversation.java**
🔄 **Conversational AI**
- **What you'll learn**: How to maintain context across multiple AI interactions
- **Key concepts**: Conversation history, context management, system messages
- **Build on Example 2** to create more sophisticated conversations

**What it demonstrates:**
- Managing conversation history
- System messages to guide AI behavior
- Building context across multiple turns
- Conversation analysis and statistics
- How AI uses previous context for better responses

### 4. **Example04_ErrorHandling.java**
🛡️ **Making Your Code Robust**
- **What you'll learn**: How to handle errors and make reliable AI applications
- **Key concepts**: Retry logic, timeouts, circuit breakers, input validation
- **Essential for production applications**

**What it demonstrates:**
- Retry logic with exponential backoff
- Timeout handling for long requests
- Circuit breaker pattern for reliability
- Input validation and sanitization
- Graceful error recovery

### 5. **Example05_UtilitiesAndMonitoring.java**
📊 **Advanced Text Processing & Monitoring**
- **What you'll learn**: How to analyze AI responses and monitor performance
- **Key concepts**: Text analysis, performance metrics, cost monitoring, data extraction
- **Advanced topics** for optimizing your AI applications

**What it demonstrates:**
- Text cleaning and processing
- Response quality analysis
- Performance monitoring and statistics
- Cost estimation and tracking
- Structured data extraction from AI responses

## 🚀 How to Run the Examples

### Option 1: Run from IDE (Recommended for learning)
1. Open your IDE (IntelliJ IDEA, Eclipse, etc.)
2. Navigate to any example file (e.g., `Example01_ConfigurationManager.java`)
3. Right-click and select "Run" or click the play button
4. Watch the console output to see what happens!

### Option 2: Run from Command Line
```bash
# Navigate to the project directory
cd /path/to/your/project

# Compile and run a specific example
javac -cp "target/classes:path/to/langchain4j/jars/*" src/main/java/com/example/agent/langchain/basics/Example01_ConfigurationManager.java
java -cp "target/classes:path/to/langchain4j/jars/*" com.example.agent.langchain.basics.Example01_ConfigurationManager
```

## 📖 Learning Path Recommendation

**Start here for beginners:**

1. **Start with Example01** - Set up your configuration properly
2. **Then Example02** - Learn basic AI chat
3. **Next Example03** - Understand conversations
4. **Then Example04** - Learn error handling (important!)
5. **Finally Example05** - Advanced monitoring and utilities

## 🔍 What to Look For

As you run each example, pay attention to:

### 📝 **Extensive Comments**
Every example has detailed comments explaining:
- **What** each code section does
- **Why** it's important
- **How** it works
- **When** you might use it

### 🎯 **Learning Objectives**
Each file starts with clear learning objectives so you know what to focus on.

### 📊 **Console Output**
The programs print detailed information showing:
- Step-by-step execution
- Results and statistics  
- Error messages (when they occur)
- Performance metrics

### 🧩 **Modular Design**
Each example is broken into logical sections:
- Main demonstration code
- Helper classes
- Utility functions
- Clear separation of concerns

## 💡 Tips for Learning

1. **Read the comments first** - They explain what you're about to see
2. **Run the examples multiple times** - Try different inputs
3. **Experiment with the code** - Change parameters and see what happens
4. **Check your console output** - It shows you exactly what's happening
5. **Try breaking things** - Remove your API key to see error handling in action

## 🐛 Troubleshooting

**"API key not found"**
- Set your environment variable: `export OPENAI_API_KEY=your_key`
- Restart your IDE after setting the environment variable

**"Connection timeout"**
- Check your internet connection
- The examples include retry logic to handle temporary issues

**"Compilation errors"**
- Make sure all LangChain4j dependencies are in your classpath
- Check that you're using Java 8 or higher

## 🎓 What's Next?

After completing these basic examples, you'll be ready for:
- **Session Management** (Example 02 session folder)
- **Prompt Templates** (Example 03 prompts folder)  
- **Memory Systems** (Example 04 memory folder)
- **Tool Integration** (Example 05 tools folder)

## 📞 Need Help?

If you get stuck:
1. Check the console output for error messages
2. Review the comments in the code
3. Try the simpler examples first (Example01, Example02)
4. Make sure your API key is set correctly

Happy learning! 🚀
