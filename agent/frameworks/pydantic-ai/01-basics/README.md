# Module 01 - Basics

## What You Will Learn
- How to install pydantic-ai
- How to configure different LLM models
- How to create your first agent
- How to run an agent and get a response

## Installation

```bash
uv pip install pydantic-ai
```

For specific providers:
```bash
uv pip install pydantic-ai[openai]      # OpenAI models
uv pip install pydantic-ai[anthropic]   # Claude models
uv pip install pydantic-ai[groq]        # Groq models
```

## Core Concept: Agent

The **Agent** is the main building block. It wraps:
- A **model** (which LLM to use)
- A **system prompt** (instructions for the LLM)
- **Tools** (functions the LLM can call)
- A **result type** (what the LLM should return)

When you call `agent.run_sync("question")`:
1. Sends system prompt + your question to the LLM
2. Gets the response
3. Validates it against the result type
4. Returns a typed result

## Model Configuration

API keys are read from environment variables automatically:
- `OPENAI_API_KEY` for OpenAI models
- `ANTHROPIC_API_KEY` for Claude models
- `GROQ_API_KEY` for Groq models

### Model Names
| Provider | Model Name | Notes |
|----------|-----------|-------|
| OpenAI | `openai:gpt-4o-mini` | Cheapest, great for learning |
| OpenAI | `openai:gpt-4o` | More capable, costs more |
| Anthropic | `anthropic:claude-3-5-sonnet-latest` | Very capable |
| Groq | `groq:llama-3.3-70b-versatile` | Fast, free tier |

## Sync vs Async

- `agent.run_sync("question")` blocks until response (simpler, good for learning)
- `await agent.run("question")` async, non-blocking (better for web apps)

For learning, use `run_sync()`.

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_hello_agent.py` | Simplest possible agent |
| `02_model_config.py` | How to configure different models |
| `03_run_sync_vs_async.py` | Sync vs Async execution |
