# Module 04 - System Prompts

## What You Will Learn
- What system prompts are and how they work
- Difference between static and dynamic system prompts
- How to combine multiple system prompts

## What is a System Prompt?

A **system prompt** is an instruction you give to the LLM before the user's question. It tells the LLM **how to behave**. The user never sees it.

```
Messages sent to LLM:
  1. System: "You are a teacher"     <- System Prompt
  2. User: "What is Python?"         <- User Message
  3. Assistant: "Python is..."       <- LLM Response
```

## Static vs Dynamic

| Type | When Set | Changes? | Use For |
|------|----------|----------|---------|
| Static | Agent creation | Never | General behavior, personality |
| Dynamic | Every run() call | Yes | Personalization, context-aware |

You can stack multiple prompts. Pydantic AI combines them:
`Final Prompt = Static + Dynamic1 + Dynamic2 + ...`

## Best Practices
1. Be specific: "Reply in 2 sentences" > "Be brief"
2. Give examples: Show what good output looks like
3. Set boundaries: Tell the LLM what NOT to do
4. Use role-playing: "You are a Python tutor" sets the tone

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_static_prompt.py` | Basic static system prompts |
| `02_dynamic_prompt.py` | Prompts that change at runtime |
| `03_multiple_prompts.py` | Combining static and dynamic prompts |
