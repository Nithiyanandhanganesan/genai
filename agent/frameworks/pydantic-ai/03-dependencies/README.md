# Module 03 - Dependencies (Dependency Injection)

## What You Will Learn
- What dependency injection is and why it matters
- How to pass external services to your agent
- How tools and system prompts can access dependencies
- Why this makes your code testable

## What is Dependency Injection?

**Dependency Injection (DI)** means passing external services into your agent instead of creating them inside.

### Without DI (Bad)
```
Agent creates its own database connection inside a tool
-> Hard to test (need a real database every time)
-> Hard to change (database config buried in code)
```

### With DI (Good)
```
You pass a database connection TO the agent when you call run()
-> Easy to test (pass a mock database)
-> Easy to change (pass a different database)
```

### Real-World Analogy
A chef (agent) cooking a dish:
- **Without DI**: Chef goes to market to buy ingredients
- **With DI**: Ingredients are delivered to the chef

## How Dependencies Flow

```
agent.run_sync("question", deps=my_dependencies)
         |
         v
   Dependencies Available to:
   - System Prompts (customize prompt based on deps)
   - Tools (access services via deps)
   - Result Validators (validate using deps)
```

## Deps Type

When creating an agent, specify the type of dependencies:
- Simple types: `str`, `int`
- Dataclasses: custom objects with multiple fields
- Any Python object: database connections, API clients

## RunContext

Tools and system prompts access dependencies via **RunContext**:
- `ctx.deps` = the dependencies you passed in `run_sync()`

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_simple_deps.py` | Passing a simple string as dependency |
| `02_dataclass_deps.py` | Using a dataclass for multiple dependencies |
| `03_deps_in_tools.py` | How tools access dependencies |
