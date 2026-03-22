# Module 07 - Messages & Conversation History

## What You Will Learn
- What message types exist in Pydantic AI
- How to maintain conversation history across multiple calls
- How to inspect and manipulate message history

## Message Types

| Type | Who Creates It | Purpose |
|------|---------------|---------|
| `SystemPromptPart` | You (developer) | Instructions for the LLM |
| `UserPromptPart` | User | The user question |
| `TextPart` | LLM | Text response from LLM |
| `ToolCallPart` | LLM | When LLM wants to call a tool |
| `ToolReturnPart` | Tool | Result returned by a tool |
| `RetryPromptPart` | Pydantic AI | Sent on validation failure for retry |

## Conversation History

By default, each `run_sync()` call is independent - the agent forgets everything.
To maintain conversation, you pass the message history from one call to the next.

```
Call 1: result1 = agent.run_sync("Hi, my name is Nithiyan")
Call 2: result2 = agent.run_sync("What is my name?", message_history=result1.all_messages())
```

Without passing `message_history`, the agent would not remember the name from Call 1.

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_conversation_history.py` | Multi-turn conversations |
| `02_inspect_messages.py` | Looking at message internals |
