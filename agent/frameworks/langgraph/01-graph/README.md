# 01 - Graph Basics

## What is a graph?
A set of **nodes** (steps) connected by **edges** (order). LangGraph passes a shared **state** from node to node.

## The 4 steps
1. **Define state**: the data schema.
2. **Add nodes**: functions that do work.
3. **Add edges**: connect `START` -> nodes -> `END`.
4. **Compile and invoke**: `compile()` validates the graph and returns a runnable.

## Special nodes
- `START`: virtual entry point.
- `END`: virtual terminal point.

## Execution model
Runs in "super-steps": ready nodes execute, their updates merge into state, then the next set runs. Stops at `END`.

## Key points
- Nodes never call each other; edges decide the flow.
- `invoke()` returns final state; `stream()` yields after each step.
- `app.get_graph().draw_mermaid()` gives a diagram.

## Examples
- `01_hello_graph.py` - minimal 2-node graph.
- `02_stream_graph.py` - watch each step with `stream()`.
