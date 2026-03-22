# Module 05 - Tools

## What You Will Learn
- What tools are and why agents need them
- How to create tools using decorators
- How the LLM decides when to call a tool
- Plain tools vs tools with context (dependencies)

## What are Tools?

Tools are **Python functions** that the LLM can call during a conversation.
The LLM cannot access the internet, databases, or APIs by itself.
Tools give it those abilities.

### How Tools Work

```
User: "What is the weather in Tokyo?"

Agent thinks: "I need weather data. I have a get_weather tool."

Agent calls: get_weather(city="Tokyo")

Tool returns: "Tokyo: 22C, Sunny"

Agent responds: "The weather in Tokyo is 22 degrees and sunny!"
```

The LLM decides WHEN and HOW to call tools based on the user question.

## Two Types of Tools

### 1. Plain Tools (`@agent.tool_plain`)
- Simple functions with no access to dependencies
- Good for: calculations, formatting, static data

### 2. Context Tools (`@agent.tool`)
- Functions that receive RunContext (access to dependencies)
- Good for: database queries, API calls, user-specific data

## Tool Description

The LLM reads your function docstring to understand what the tool does.
Write clear docstrings - they are instructions FOR the LLM.

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_plain_tools.py` | Simple tools without dependencies |
| `02_context_tools.py` | Tools that use dependencies |
| `03_multiple_tools.py` | Agent with multiple tools |
