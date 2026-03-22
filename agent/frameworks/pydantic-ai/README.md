# Pydantic AI Framework - Complete Learning Guide

## 🌟 Overview
Pydantic AI is a Python agent framework built by the creators of Pydantic (the most widely used data validation library for Python). It is designed to make it easy, productive, and type-safe to build production-grade applications with Generative AI.

Think of it as **FastAPI for AI agents** — just like FastAPI revolutionized building web APIs with Python, Pydantic AI aims to do the same for AI agent development.

## 🧠 Why Pydantic AI?

### Problem It Solves
- Most AI frameworks lack **type safety** — you pass strings around and hope for the best
- **Dependency injection** is missing — hard to test and manage external services
- **Structured outputs** are painful — parsing LLM text responses into usable data is fragile
- **Testing AI code** is difficult — most frameworks don't think about testability

### Key Design Principles
1. **Type Safety**: Uses Pydantic models for inputs, outputs, and validation
2. **Model Agnostic**: Works with OpenAI, Anthropic, Google Gemini, Groq, Mistral, and more
3. **Dependency Injection**: First-class support for injecting services (databases, APIs, etc.)
4. **Structured Responses**: LLM outputs are validated against Pydantic models automatically
5. **Testability**: Built-in support for testing with mock models
6. **Pythonic**: Feels natural to Python developers — decorators, type hints, async support

## 🏗️ Architecture

```
┌──────────────────────────────────────────────┐
│                  YOUR APP                     │
├──────────────────────────────────────────────┤
│                                              │
│   Agent                                      │
│   ├── Model (OpenAI, Anthropic, etc.)        │
│   ├── System Prompt (static + dynamic)       │
│   ├── Tools (functions the agent can call)   │
│   ├── Result Type (what the agent returns)   │
│   └── Dependencies (injected services)       │
│                                              │
├──────────────────────────────────────────────┤
│            Pydantic AI Core                   │
│   ├── Message History                        │
│   ├── Retry Logic                            │
│   ├── Validation (Pydantic models)           │
│   ├── Streaming                              │
│   └── Usage Tracking (tokens)                │
├──────────────────────────────────────────────┤
│          LLM Provider (API)                   │
│   OpenAI │ Anthropic │ Gemini │ Groq │ ...   │
└──────────────────────────────────────────────┘
```

## 📦 How Agent Works (Simplified Flow)

```
1. You create an Agent with a model, system prompt, tools, and result type
2. You call agent.run("user message") with optional dependencies
3. Pydantic AI sends the system prompt + user message to the LLM
4. LLM responds — it may call tools or return a final answer
5. If tools are called → Pydantic AI executes them and sends results back to LLM
6. LLM returns final answer → Pydantic AI validates it against your result type
7. You get a validated, typed result back
```

## 📚 Module Guide

| # | Module | What You'll Learn |
|---|--------|-------------------|
| 01 | [Basics](01-basics/) | Installation, setup, first agent, model configuration |
| 02 | [Agents](02-agents/) | Agent creation, configuration, lifecycle |
| 03 | [Dependencies](03-dependencies/) | Dependency injection for services |
| 04 | [System Prompts](04-system-prompts/) | Static and dynamic system prompts |
| 05 | [Tools](05-tools/) | Giving agents the ability to call functions |
| 06 | [Results](06-results/) | Structured outputs, validation, union types |
| 07 | [Messages](07-messages/) | Conversation history, message types |
| 08 | [Streaming](08-streaming/) | Streaming text and structured responses |
| 09 | [Testing](09-testing/) | Testing agents with mock models |
| 10 | [Graphs](10-graphs/) | Building workflows with Pydantic AI Graphs |

## 🔑 Key Concepts at a Glance

### Agent
The central class. It wraps an LLM model and adds system prompts, tools, dependency injection, and result validation. Think of it as the "brain" that coordinates everything.

### Model
The LLM provider. Pydantic AI supports: OpenAI, Anthropic, Google Gemini, Groq, Mistral, Ollama (local). You can switch models without changing any other code.

### System Prompt
Instructions that tell the agent how to behave. Can be static (fixed text) or dynamic (changes based on dependencies or context at runtime).

### Tools
Functions that the agent can call during a conversation. For example: search a database, call an API, do a calculation. The agent decides when and how to call them.

### Dependencies
External services your agent needs — database connections, API clients, user info. Injected at runtime so your agent code stays clean and testable.

### Result Type
What the agent returns. Can be plain text (`str`), or a structured Pydantic model (e.g., `MovieReview` with `title`, `rating`, `summary` fields). Pydantic AI validates the output automatically.

### Messages
The conversation history — user messages, model responses, tool calls, tool results. You can pass message history back to maintain multi-turn conversations.

### Streaming
Get responses token by token as they're generated, instead of waiting for the full response. Works for both plain text and structured results.

## ⚙️ Quick Setup

```bash
# Install uv (if not installed)
brew install uv

# Navigate to pydantic-ai folder
cd agent/frameworks/pydantic-ai

# Create virtual environment and install dependencies
uv venv
source .venv/bin/activate
uv pip install pydantic-ai

# Set your API key
export OPENAI_API_KEY="your-key-here"
```

## 🆚 Pydantic AI vs LangChain

| Feature | Pydantic AI | LangChain |
|---------|-------------|-----------|
| **Philosophy** | Type-safe, Pythonic | Flexible, chain-based |
| **Structured Output** | Built-in with Pydantic models | Requires output parsers |
| **Dependency Injection** | First-class support | Not built-in |
| **Testing** | Built-in mock models | Manual mocking |
| **Learning Curve** | Simpler if you know Python | More concepts to learn |
| **Ecosystem** | Growing | Very large |
| **Language** | Python only | Python, JavaScript, Java |
| **Best For** | Type-safe agents, structured data | Complex chains, RAG pipelines |

## 💡 When to Use Pydantic AI
- You want **type-safe** AI applications
- You need **structured outputs** from LLMs (e.g., JSON objects)
- You value **testability** and clean architecture
- You're building **production Python** applications
- You want **dependency injection** for managing services

## 📖 Learning Path
Start from module 01 and go sequentially. Each module builds on the previous one. Run each example to see the concepts in action.
