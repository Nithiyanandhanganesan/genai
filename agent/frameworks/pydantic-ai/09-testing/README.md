# Module 09 - Testing

## What You Will Learn
- How to test agents without making real API calls
- Using TestModel and FunctionModel for mocking
- Why dependency injection makes testing easy

## The Testing Problem

Real LLM calls are:
- **Expensive**: Each call costs money (tokens)
- **Slow**: Network latency
- **Non-deterministic**: Same question can give different answers
- **Rate-limited**: APIs have usage limits

For testing, you need **mock models** that are free, fast, and predictable.

## Mock Models in Pydantic AI

### TestModel
- Returns canned/predictable responses
- No API calls, no cost
- Perfect for unit tests

### FunctionModel
- You provide a Python function that generates responses
- Full control over what the model returns
- Good for testing specific scenarios

## Why DI Makes Testing Easy

Because dependencies are injected:
1. In production: inject real database, real API client
2. In testing: inject fake database, mock API client

No code changes needed in the agent itself.

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_test_model.py` | Using TestModel for basic testing |
| `02_function_model.py` | Using FunctionModel for custom mock responses |
