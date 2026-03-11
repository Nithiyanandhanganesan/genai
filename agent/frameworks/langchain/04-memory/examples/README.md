# Memory Examples

This folder contains simple, focused examples of different memory types in LangChain4j.

## 🎯 Examples Overview

### 1. `BufferMemoryExample.java` ⭐ **START HERE**
- **What it shows**: MessageWindowChatMemory (sliding window)
- **Key concept**: Keeps last N messages with FIFO removal
- **Best for**: Learning the basics, short conversations
- **Run**: `java BufferMemoryExample`

### 2. `TokenWindowMemoryExample.java`
- **What it shows**: TokenWindowChatMemory (token-based limits)
- **Key concept**: Manages memory by token count instead of message count
- **Best for**: Cost-controlled conversations, precise token management
- **Run**: `java TokenWindowMemoryExample`

### 3. `SummaryMemoryExample.java`
- **What it shows**: Custom conversation summarization using AI
- **Key concept**: Compresses old conversations into summaries while keeping recent messages
- **Best for**: Long conversations, memory efficiency, preserving context themes
- **Run**: `java SummaryMemoryExample`

### 4. `EntityMemoryExample.java`
- **What it shows**: Custom entity extraction and tracking
- **Key concept**: Remembers facts about users, preferences, and context
- **Best for**: Personalization, long-term user relationships
- **Run**: `java EntityMemoryExample`

### 5. `CompositeMemoryExample.java`
- **What it shows**: Combining multiple memory types
- **Key concept**: Recent memory + summaries + user profile + facts
- **Best for**: Production applications, complex use cases
- **Run**: `java CompositeMemoryExample`

### 6. `MemoryComparisonExample.java`
- **What it shows**: Side-by-side comparison of memory types
- **Key concept**: Same conversation processed by different memory approaches
- **Best for**: Understanding trade-offs and choosing the right approach
- **Run**: `java MemoryComparisonExample`

### 7. `MemoryCheckpointerExample.java` 🆕
- **What it shows**: Conversation checkpointing and state persistence
- **Key concept**: Save/restore conversation state across app restarts
- **Best for**: Long-running conversations, session recovery, multi-user management
- **Run**: `java MemoryCheckpointerExample`

## 🚀 Quick Start

### Prerequisites
1. Set your OpenAI API key: `export OPENAI_API_KEY=your-api-key`
2. Compile the project: `mvn compile`

### Run Examples
```bash
# Start with the basic buffer memory example
java -cp "target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.example.agent.langchain.memory.BufferMemoryExample

# Try token-based memory
java -cp "target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.example.agent.langchain.memory.TokenWindowMemoryExample

# See AI-powered conversation summarization
java -cp "target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.example.agent.langchain.memory.SummaryMemoryExample

# See entity extraction in action
java -cp "target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.example.agent.langchain.memory.EntityMemoryExample

# Advanced composite memory
java -cp "target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.example.agent.langchain.memory.CompositeMemoryExample

# Compare all approaches
java -cp "target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.example.agent.langchain.memory.MemoryComparisonExample

# Try conversation checkpointing
java -cp "target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.example.agent.langchain.memory.MemoryCheckpointerExample
```

## 📚 What You'll Learn

### Memory Types Covered:
1. **Buffer Memory** - Simple recent message storage
2. **Token Memory** - Cost-aware token-based management  
3. **Summary Memory** - AI-powered conversation compression
4. **Entity Memory** - Structured fact extraction and storage
5. **Composite Memory** - Multi-layered memory architecture
6. **Checkpointer Memory** - Persistent state management and recovery

### Key Concepts:
- **Token efficiency** - Managing costs in long conversations
- **Context preservation** - Maintaining relevant information
- **Memory trade-offs** - Understanding benefits and limitations
- **Real-world patterns** - Practical memory management strategies
- **State persistence** - Saving/restoring conversation across sessions
- **Checkpoint recovery** - Handling application restarts and failures

### LangChain4j Classes Used:
- `MessageWindowChatMemory` - Core sliding window memory
- `TokenWindowChatMemory` - Token-aware memory management
- `OpenAiTokenizer` - Token counting for cost management
- Custom memory implementations for advanced patterns

## 🎯 Choosing the Right Memory Type

**Use Buffer Memory (MessageWindowChatMemory) when:**
- Conversations are short (< 20 messages)
- You want simple, predictable behavior
- Perfect recent recall is important

**Use Token Memory (TokenWindowChatMemory) when:**
- Cost control is important
- Conversations might get long
- You need precise token management

**Use Entity Memory when:**
- Personalization is important
- You need to remember facts about users
- Long-term relationships matter

**Use Composite Memory when:**
- Production applications
- Multiple use cases need support
- Optimal performance is required

## 💡 Tips for Learning

1. **Start with BufferMemoryExample** - It's the simplest
2. **Run MemoryComparisonExample** - See differences side-by-side
3. **Experiment with limits** - Change max messages/tokens and see the effects
4. **Check token usage** - Watch how different approaches manage costs
5. **Try your own conversations** - Modify the examples with your own scenarios

## 🔗 Integration Notes

These examples use:
- **ConfigurationUtil** for consistent API key management
- **Real LangChain4j memory classes** - Not custom implementations
- **Simple, focused demonstrations** - One concept per file
- **Practical scenarios** - Learning conversations, user interactions

The examples show real-world patterns you can use in your own applications!
