# Module 06 - Results (Structured Output)

## What You Will Learn
- How to get structured data (not just text) from the LLM
- Using Pydantic models as result types
- Union types - LLM chooses which format to return
- Result validators for custom validation

## Why Structured Results?

By default, the agent returns a plain text string. But in real applications, you need structured data:
- Extract a name, email, and phone from text -> use a Pydantic model
- Classify text into categories -> use an Enum or Literal type
- Get a list of items -> use a list type

## How It Works

1. You define a Pydantic model (e.g., MovieReview with title, rating, summary)
2. Set `result_type=MovieReview` on the agent
3. Pydantic AI tells the LLM the expected output structure
4. The LLM returns data matching that structure
5. Pydantic validates the data automatically
6. If invalid, it retries (sends error back to LLM)

## Result Types You Can Use

| Type | Example | When To Use |
|------|---------|-------------|
| `str` (default) | Plain text | General Q&A |
| Pydantic Model | `MovieReview` | Structured extraction |
| `bool` | True/False | Yes/No classification |
| `int` / `float` | Numbers | Scoring, counting |
| `list[str]` | List of strings | Multiple items |
| Union types | `Success \| Failure` | Multiple possible formats |

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_pydantic_result.py` | Basic structured output with Pydantic model |
| `02_union_results.py` | LLM choosing between multiple result types |
| `03_result_validators.py` | Custom validation on LLM output |
