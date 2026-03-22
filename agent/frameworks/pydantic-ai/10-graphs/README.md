# Module 10 - Graphs (Workflows)

## What You Will Learn
- What Pydantic AI Graphs are
- How to build multi-step workflows
- Nodes, edges, and state in graphs
- When to use graphs vs simple agents

## What is a Graph?

A **Graph** is a way to build multi-step AI workflows. Instead of one agent doing everything, you break the task into **nodes** (steps) connected by **edges** (transitions).

### Simple Agent vs Graph

| Simple Agent | Graph |
|-------------|-------|
| One step: question -> answer | Multiple steps: question -> research -> analyze -> answer |
| Good for simple Q&A | Good for complex workflows |
| One system prompt | Different prompts per step |
| One set of tools | Different tools per step |

## Graph Concepts

### Node
A single step in the workflow. Each node:
- Has its own logic (can be an agent, a function, or any code)
- Receives state as input
- Returns updated state + which node to go to next

### Edge
A connection between nodes. Edges define the flow:
- Node A -> Node B (always go to B after A)
- Node A -> Node B or Node C (conditional, based on result)

### State
Data that flows through the graph. Each node can read and update it.
Think of it like a form being passed from desk to desk in an office.

## Graph Flow

```
START -> [Classify] -> [Research] -> [Draft Response] -> [Review] -> END
              |                                              |
              +--- if simple ---> [Quick Answer] --> END     |
                                                             |
              <---- if needs revision ----------------------+
```

## When to Use Graphs
- Multi-step reasoning (research -> analyze -> conclude)
- Approval workflows (draft -> review -> approve)
- Conditional logic (classify input, then route to different handlers)
- Retry loops (generate -> validate -> retry if invalid)

## Examples

| File | What It Demonstrates |
|------|---------------------|
| `01_simple_graph.py` | Basic graph with three nodes |
| `02_conditional_graph.py` | Graph with conditional edges |
