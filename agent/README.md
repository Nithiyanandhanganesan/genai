# AI Agent Building - Complete Java Learning Guide

## 🌟 Overview
This comprehensive guide covers fundamental concepts and frameworks for building AI agents using **Java**. Focused on **LangChain4j** and **LangGraph** implementations with production-ready examples.

## 🚀 Quick Start
```bash
# Clone and setup
cd /Users/nithiyanandhan/IdeaProjects/genai/agent
export OPENAI_API_KEY="your-key-here"
mvn clean install

# Run your first example
mvn exec:java -Psession-examples
```

**👉 [Complete Setup Guide](GETTING_STARTED.md)**

## 📁 Project Structure

```
agent/
├── 🔧 pom.xml                    # Maven dependencies & build config
├── 📖 GETTING_STARTED.md         # Complete setup guide
├── 📋 README.md                  # This overview
├── 
├── 🏗️ src/main/java/com/example/agent/
│   ├── session/                  # Session management (Java examples)
│   ├── state/                    # State machines (Java examples)
│   ├── memory/                   # Memory systems (Java examples)
│   ├── graph/                    # LangGraph workflows (Java examples)
│   ├── tools/                    # Tool integration (Java examples)
│   └── agents/                   # Agent implementations (Java examples)
│
└── 📚 frameworks/                # Learning documentation
    ├── langchain/                # LangChain4j concepts & theory
    │   ├── session/              # Session management deep dive
    │   ├── state/                # State management patterns
    │   ├── memory/               # Memory system architecture
    │   ├── tools/                # Tool integration patterns
    │   ├── chains/               # Processing chains
    │   ├── agents/               # Agent architectures
    │   ├── prompts/              # Prompt engineering
    │   └── vectorstores/         # Vector databases & RAG
    │
    └── langgraph/                # LangGraph concepts & theory  
        ├── graph/                # Graph architecture patterns
        ├── state/                # Graph state management
        ├── nodes/                # Node implementation
        ├── edges/                # Conditional routing
        ├── memory/               # Graph-specific memory
        ├── tools/                # Tools in workflows
        ├── persistence/          # State persistence
        └── human-loop/           # Interactive workflows
```

## 🎯 Learning Progression

### 🟢 Phase 1: Foundations (Week 1-2)
**Core Concepts with Java Implementation**

| Concept | Theory | Java Examples | Run Command |
|---------|--------|---------------|-------------|
| **[Session Management](frameworks/langchain/session/)** | User interaction continuity | `SimpleSessionManager.java`<br/>`DatabaseSessionManager.java` | `mvn exec:java -Psession-examples` |
| **[State Management](frameworks/langchain/state/)** | Workflow state machines | `DocumentWorkflowStateMachine.java` | `mvn exec:java -Pstate-examples` |
| **[Memory Systems](frameworks/langchain/memory/)** | Conversation & context memory | `ConversationBufferMemory.java`<br/>`VectorMemory.java` | `mvn exec:java -Pmemory-examples` |

### 🟡 Phase 2: Advanced Workflows (Week 3-4)
**Complex Patterns and Integration**

| Concept | Theory | Java Examples | Run Command |
|---------|--------|---------------|-------------|
| **[Graph Architecture](frameworks/langgraph/graph/)** | Non-linear workflows | `StateGraph.java`<br/>`DocumentProcessingGraph.java` | `mvn exec:java -Pgraph-examples` |
| **[Tool Integration](frameworks/langchain/tools/)** | External system integration | `CustomTools.java`<br/>`ToolChaining.java` | `mvn exec:java -Ptools-examples` |
| **[Vector Stores](frameworks/langchain/vectorstores/)** | Semantic search & RAG | `ChromaVectorStore.java`<br/>`RAGPipeline.java` | `mvn exec:java -Pvector-examples` |

### 🔴 Phase 3: Production Ready (Week 5-6)
**Enterprise Patterns and Deployment**

| Concept | Theory | Java Examples | Run Command |
|---------|--------|---------------|-------------|
| **[Advanced Agents](frameworks/langchain/agents/)** | Autonomous decision-making | `ReActAgent.java`<br/>`PlanExecuteAgent.java` | `mvn exec:java -Pagent-examples` |
| **[Human-in-Loop](frameworks/langgraph/human-loop/)** | Interactive workflows | `ApprovalWorkflow.java`<br/>`QualityControl.java` | `mvn exec:java -Phuman-loop-examples` |
| **[Persistence](frameworks/langgraph/persistence/)** | Production deployment | `StatePersistence.java`<br/>`ScalableWorkflows.java` | `mvn exec:java -Ppersistence-examples` |

## 💻 Hands-On Examples

### 🤖 Session Management
```java
// Simple chatbot with memory across conversations
SimpleSessionManager sessionManager = new SimpleSessionManager(model);
String sessionId = sessionManager.createSession("user123");

String response1 = sessionManager.sendMessage(sessionId, "Hi, I'm Alice");
String response2 = sessionManager.sendMessage(sessionId, "What's my name?"); 
// AI remembers: "Your name is Alice"
```

### 🔄 State Machines  
```java
// Document processing workflow with state transitions
DocumentWorkflowStateMachine workflow = new DocumentWorkflowStateMachine();
String workflowId = workflow.createWorkflow("user123", "contract.pdf");

workflow.processEvent(workflowId, "DOCUMENT_UPLOADED", Map.of("fileSize", 1024000));
workflow.processEvent(workflowId, "ANALYSIS_COMPLETED", Map.of("confidence", 0.92));
// Automatic progression: CREATED → UPLOADED → ANALYZING → COMPLETED
```

### 🕸️ Graph Workflows
```java
// Complex branching logic with LangGraph
StateGraph graph = new StateGraph()
    .addNode("analyze", documentAnalyzer)
    .addNode("human_review", humanReviewNode)
    .addConditionalEdge("analyze", state -> 
        state.getData("confidence", Double.class).orElse(0.0) > 0.8 
            ? "auto_approve" : "human_review")
    .compile();
```

## 🛠️ Technology Stack

### Core Frameworks
- **[LangChain4j](https://docs.langchain4j.dev/)** - LLM application framework
- **[Spring Boot](https://spring.io/projects/spring-boot)** - Application framework  
- **[Maven](https://maven.apache.org/)** - Dependency management

### AI & ML Integration
- **OpenAI GPT** - Language model integration
- **Chroma/Pinecone** - Vector databases
- **Apache Tika** - Document processing

### Storage & Persistence  
- **H2/PostgreSQL** - Relational databases
- **Redis** - Caching and session storage
- **JPA/Hibernate** - Object-relational mapping

## 🎮 Interactive Learning

### Run Examples by Category
```bash
# Session management examples
mvn exec:java -Psession-examples

# State machine workflows  
mvn exec:java -Pstate-examples

# Memory system demonstrations
mvn exec:java -Pmemory-examples

# Graph-based workflows
mvn exec:java -Pgraph-examples
```

### Custom Example Execution
```bash
# Run specific class
mvn compile exec:java -Dexec.mainClass="com.example.agent.session.SimpleSessionManager"

# With custom parameters
mvn compile exec:java -Dexec.mainClass="com.example.agent.graph.DocumentProcessingGraph" -Dexec.args="contract.pdf"
```

## 📋 Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Maven 3.6+** 
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)
- **OpenAI API Key** (for LLM access)

## 🔧 Environment Setup

### 1. API Keys
```bash
export OPENAI_API_KEY="your-openai-api-key-here"
export ANTHROPIC_API_KEY="your-anthropic-key"     # optional
export PINECONE_API_KEY="your-pinecone-key"       # optional
```

### 2. Database (Optional)
```bash
# PostgreSQL for production examples
docker run --name postgres-agent -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres

# Redis for caching examples  
docker run --name redis-agent -p 6379:6379 -d redis
```

### 3. Build & Test
```bash
mvn clean install
mvn test
```

## 🚀 What You'll Build

### 🏆 Complete Projects Included

1. **📱 Conversational Chatbot**
   - Multi-session memory management
   - Database persistence
   - User preference tracking

2. **📄 Document Processing Pipeline** 
   - State machine workflow
   - AI-powered analysis
   - Human approval process

3. **🎯 Customer Support Agent**
   - Graph-based routing
   - Knowledge base integration
   - Escalation workflows

4. **🔍 RAG Question-Answering System**
   - Vector database integration  
   - Semantic search
   - Context-aware responses

## 📊 Framework Comparison

| Feature | LangChain4j | LangGraph | Best For |
|---------|-------------|-----------|----------|
| **Learning Curve** | Easy | Medium | LangChain4j for beginners |
| **Workflow Type** | Linear chains | Complex graphs | LangGraph for complex logic |
| **State Management** | Simple | Advanced | LangGraph for stateful apps |
| **Human Integration** | Basic | Advanced | LangGraph for approval workflows |
| **Production Ready** | ✅ | ✅ | Both enterprise-ready |

## 🎯 Learning Outcomes

After completing this guide, you'll be able to:

- ✅ **Build production-ready AI agents** in Java
- ✅ **Implement complex workflows** with state management
- ✅ **Create memory systems** for context-aware applications  
- ✅ **Integrate external tools** and APIs seamlessly
- ✅ **Deploy scalable agent systems** with proper persistence
- ✅ **Handle human-in-the-loop** workflows effectively

## 📚 Additional Resources

- **[Complete Setup Guide](GETTING_STARTED.md)** - Detailed installation and configuration
- **[LangChain4j Documentation](https://docs.langchain4j.dev/)** - Official framework docs
- **[Framework Comparison](frameworks/)** - Detailed framework analysis
- **[Best Practices Guide](frameworks/best-practices.md)** - Production deployment tips

## 🤝 Contributing

Found an issue or want to add examples? Contributions welcome!

1. Fork the repository
2. Create a feature branch
3. Add your examples with documentation
4. Submit a pull request

---

**🚀 Ready to start? → [Complete Setup Guide](GETTING_STARTED.md)**

*This learning framework provides everything you need to master AI agent development in Java, from basic concepts to production deployment.*
