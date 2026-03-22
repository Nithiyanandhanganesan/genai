# Module 02 - Agents

## What You Will Learn
- How to create and configure agents in detail
- Agent lifecycle - what happens when you call run()
- How to set retries for invalid responses
- How to pass model settings (temperature, max tokens)

## What is an Agent?

An **Agent** is the core class in Pydantic AI. It holds:

| Component | Purpose |
|-----------|---------|
| **Model** | Which LLM to use |
| **System Prompt** | Instructions for the LLM |
| **Tools** | Functions the LLM can call |
| **Result Type** | What type of data the LLM should return |
| **Retries** | How many times to retry if validation fails |

## Agent Lifecycle

```
agent.run_sync("user question")
    1. Build Messages (system prompt + user message)
    2. Send to LLM (API call)
    3. LLM Responds (text or tool call)
       - If tool call -> execute tool -> send result back to LLM
    4. Validate Result (check against result_type)
       - If invalid -> retry (up to max_retries)
    5. Return typed RunResult object
```

## Model Settings

| Setting | What It Does | Range |
|---------|-------------|-------|
| `temperature` | Randomness. 0=deterministic, 2=creative | 0 to 2 |
| `max_tokens` | Maximum response length | Varies by model |
| `top_p` | Alternative to temperature | 0 to 1 |

### Temperature Explained
- **0.0** = Always same answer (good for factual tasks)
- **0.5** = Balanced (good default)
- **1.0** = Creative and varied
- **2.0** = Very random (often incoherent)

## Retries

When LLM response does not match your `result_type`, Pydantic AI can automatically retry.
It sends the validation error back to the LLM asking it to fix the response.

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_agent_configuration.py` | Creating agents with different configs |
| `02_model_settings.py` | Temperature, max_tokens settings |
| `03_agent_retries.py` | How retries work with validation |
