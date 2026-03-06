# Getting Started with AI Agent Development (Java)

## 🚀 Quick Start Guide

This comprehensive learning framework provides in-depth coverage of AI agent development using Java, focusing on LangChain4j and LangGraph concepts.

## 📋 Prerequisites

### System Requirements
- Java 17 or higher
- Maven 3.6+ 
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Git

### API Keys Required
- OpenAI API Key (for LLM access)
- Optional: Anthropic, Google, or other LLM provider keys
- Optional: Pinecone API Key (for vector storage)

## 🔧 Project Setup

### 1. Clone and Build
```bash
cd /Users/nithiyanandhan/IdeaProjects/genai/agent
mvn clean install
```

### 2. Environment Configuration
Create `application.properties` in `src/main/resources/`:
```properties
# LLM Configuration
langchain4j.open-ai.api-key=${OPENAI_API_KEY}
langchain4j.open-ai.model-name=gpt-3.5-turbo
langchain4j.open-ai.temperature=0.7

# Database Configuration  
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Redis Configuration (optional)
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Logging
logging.level.com.example.agent=DEBUG
logging.level.dev.langchain4j=INFO
```

### 3. Set Environment Variables
```bash
export OPENAI_API_KEY="your-openai-api-key-here"
export ANTHROPIC_API_KEY="your-anthropic-api-key" # optional
export PINECONE_API_KEY="your-pinecone-api-key"   # optional
```

## 📚 Learning Path

### Phase 1: Fundamentals (Week 1-2)
Start with core concepts and basic implementations:

1. **[Session Management](frameworks/langchain/session/)**
   ```bash
   mvn exec:java -Psession-examples
   ```

2. **[Basic Memory Systems](frameworks/langchain/memory/)**
   ```bash
   mvn exec:java -Pmemory-examples  
   ```

3. **[Simple State Machines](frameworks/langchain/state/)**
   ```bash
   mvn exec:java -Pstate-examples
   ```

### Phase 2: Advanced Patterns (Week 3-4)
Explore complex workflows and integrations:

1. **[Graph-Based Workflows](frameworks/langgraph/graph/)**
   ```bash
   mvn exec:java -Pgraph-examples
   ```

2. **[Tool Integration](frameworks/langchain/tools/)**
   - Web search capabilities
   - API integrations
   - Custom tool development

3. **[Vector Stores & RAG](frameworks/langchain/vectorstores/)**
   - Document processing
   - Semantic search
   - Retrieval-augmented generation

### Phase 3: Production Ready (Week 5-6)
Build production-ready applications:

1. **[Advanced Agents](frameworks/langchain/agents/)**
   - ReAct agents
   - Plan-and-execute agents
   - Multi-agent systems

2. **[Persistence & Scaling](frameworks/langgraph/persistence/)**
   - Database integration
   - State persistence
   - Error handling

3. **[Human-in-the-Loop](frameworks/langgraph/human-loop/)**
   - Approval workflows
   - Interactive systems
   - Quality control

## 🎮 Interactive Examples

### Running Session Management Examples
```bash
# Simple in-memory session management
mvn compile exec:java -Dexec.mainClass="com.example.agent.session.SimpleSessionManager"

# Database-backed session management  
mvn compile exec:java -Dexec.mainClass="com.example.agent.session.DatabaseSessionManager"
```

### Running State Management Examples
```bash
# Document processing workflow
mvn compile exec:java -Dexec.mainClass="com.example.agent.state.DocumentWorkflowStateMachine"
```

### Running Memory Examples
```bash
# Composite memory system demonstration
mvn compile exec:java -Dexec.mainClass="com.example.agent.memory.CompositeMemoryExample"
```

### Running Graph Examples
```bash
# Document processing graph
mvn compile exec:java -Dexec.mainClass="com.example.agent.graph.DocumentProcessingGraph"

# Customer support workflow
mvn compile exec:java -Dexec.mainClass="com.example.agent.graph.CustomerSupportGraph"
```

## 🏗️ Project Structure

```
agent/
├── src/main/java/com/example/agent/
│   ├── session/          # Session management examples
│   ├── state/            # State machine examples  
│   ├── memory/           # Memory system examples
│   ├── graph/            # LangGraph examples
│   ├── tools/            # Tool integration examples
│   ├── agents/           # Agent implementation examples
│   ├── vectorstores/     # Vector storage examples
│   └── utils/            # Utility classes
├── src/main/resources/
│   ├── application.properties
│   └── logback-spring.xml
├── src/test/java/        # Unit and integration tests
├── frameworks/           # Learning documentation
│   ├── langchain/        # LangChain concepts and examples
│   └── langgraph/        # LangGraph concepts and examples  
├── pom.xml              # Maven dependencies
└── README.md            # This file
```

## 🔍 Example Walkthrough

### Simple Chatbot with Memory
```java
// Create a basic chatbot with conversation memory
public class SimpleChatbot {
    public static void main(String[] args) {
        // Initialize LLM
        ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-3.5-turbo")
            .build();
            
        // Create session manager
        SimpleSessionManager sessionManager = new SimpleSessionManager(model);
        
        // Start conversation
        String sessionId = sessionManager.createSession("user123");
        
        // Chat with memory
        String response1 = sessionManager.sendMessage(sessionId, "Hi, I'm Alice");
        System.out.println("AI: " + response1);
        
        String response2 = sessionManager.sendMessage(sessionId, "What's my name?");
        System.out.println("AI: " + response2); // Should remember "Alice"
    }
}
```

### Document Processing Workflow
```java
// Create a state machine for document processing
public class DocumentProcessor {
    public static void main(String[] args) {
        DocumentWorkflowStateMachine stateMachine = new DocumentWorkflowStateMachine();
        
        // Create workflow
        String workflowId = stateMachine.createWorkflow("user123", "contract.pdf");
        
        // Process through states
        stateMachine.processEvent(workflowId, "DOCUMENT_UPLOADED", 
            Map.of("fileSize", 1024000));
            
        stateMachine.processEvent(workflowId, "ANALYSIS_STARTED", 
            Map.of("analysisType", "contract"));
            
        // Continue through workflow...
    }
}
```

## 🧪 Testing Your Setup

### Basic Connectivity Test
```java
public class SetupTest {
    public static void main(String[] args) {
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-3.5-turbo")
                .build();
                
            AiMessage response = model.generate(UserMessage.from("Hello!")).content();
            System.out.println("Setup successful! Response: " + response.text());
        } catch (Exception e) {
            System.err.println("Setup failed: " + e.getMessage());
        }
    }
}
```

Run this test:
```bash
mvn compile exec:java -Dexec.mainClass="com.example.agent.SetupTest"
```

## 🚨 Troubleshooting

### Common Issues

1. **API Key Not Found**
   ```
   Error: API key not found
   Solution: Ensure OPENAI_API_KEY environment variable is set
   ```

2. **Maven Build Failures**
   ```
   Error: Could not resolve dependencies
   Solution: Check internet connection and Maven repositories
   ```

3. **Java Version Issues**
   ```
   Error: Unsupported Java version
   Solution: Ensure Java 17+ is installed and JAVA_HOME is set
   ```

### Debug Mode
Enable debug logging in `application.properties`:
```properties
logging.level.com.example.agent=DEBUG
logging.level.dev.langchain4j=DEBUG
```

## 📖 Next Steps

1. **Complete the Learning Path**: Follow the structured progression through all concepts
2. **Build Your Own Agent**: Start with a simple use case and gradually add complexity
3. **Explore Advanced Topics**: Vector stores, multi-agent systems, production deployment
4. **Join the Community**: Participate in LangChain4j discussions and contribute examples

## 🔗 Additional Resources

- **[LangChain4j Documentation](https://docs.langchain4j.dev/)**
- **[OpenAI API Documentation](https://platform.openai.com/docs)**
- **[Spring Boot Documentation](https://spring.io/projects/spring-boot)**
- **[Maven Documentation](https://maven.apache.org/guides/)**

## 📞 Support

If you encounter issues or have questions:
1. Check the troubleshooting section
2. Review the example documentation
3. Test with the provided examples
4. Check API key configuration

---

*Happy learning! Start with the fundamentals and gradually work your way through the advanced concepts.*
