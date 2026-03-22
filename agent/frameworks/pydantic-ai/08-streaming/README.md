# Module 08 - Streaming

## What You Will Learn
- How to stream text responses token by token
- How to stream structured (Pydantic model) responses
- When to use streaming vs non-streaming

## What is Streaming?

Without streaming: You wait for the ENTIRE response, then see it all at once.
With streaming: You see the response appear word by word as it is generated.

### When to Use Streaming
- **Chat interfaces**: Users see responses being typed out
- **Long responses**: Don't make users wait for the full answer
- **Real-time feedback**: Show progress as the LLM generates

### When NOT to Use Streaming
- **Structured output**: Wait for full result, then validate
- **Background processing**: No user watching the output
- **Simple scripts**: Simpler to just use run_sync()

## Two Streaming Methods

| Method | Returns | Use For |
|--------|---------|---------|
| `run_stream()` | Text chunks | Chat interfaces |
| `run_stream()` with `result_type` | Partial structured data | Progressive UI updates |

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_text_streaming.py` | Stream text word by word |
| `02_structured_streaming.py` | Stream structured output progressively |
