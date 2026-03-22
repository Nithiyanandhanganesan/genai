"""
01_simple_graph.py - Basic Graph (Multi-step Workflow)
=======================================================

A simple graph with three nodes that process a task step by step.
Each node does one thing, then passes control to the next.

Flow: Classify -> Process -> Respond

Run: python 01_simple_graph.py
"""

from __future__ import annotations
from dataclasses import dataclass
from pydantic_ai import Agent
from pydantic_ai.graph import Graph, Node, End, GraphRunContext


# ──────────────────────────────────────────────
# State: Data that flows through the graph
# ──────────────────────────────────────────────
# Every node can read and update this state
@dataclass
class TaskState:
    user_input: str = ''
    category: str = ''
    response: str = ''


# ──────────────────────────────────────────────
# Node 1: Classify the user input
# ──────────────────────────────────────────────
@dataclass
class ClassifyNode(Node[TaskState]):
    """Classify the user input into a category"""

    async def run(self, ctx: GraphRunContext[TaskState]) -> ProcessNode:
        agent = Agent(
            model='openai:gpt-4o-mini',
            result_type=str,
            system_prompt='Classify this input as either "question", "request", or "complaint". Reply with just the category.'
        )
        result = await agent.run(ctx.state.user_input)
        ctx.state.category = result.data
        print(f'  Step 1 - Classified as: {ctx.state.category}')
        return ProcessNode()


# ──────────────────────────────────────────────
# Node 2: Process based on classification
# ──────────────────────────────────────────────
@dataclass
class ProcessNode(Node[TaskState]):
    """Process the input based on its category"""

    async def run(self, ctx: GraphRunContext[TaskState]) -> RespondNode:
        agent = Agent(
            model='openai:gpt-4o-mini',
            system_prompt=f'You handle {ctx.state.category}s. Provide a brief helpful response.'
        )
        result = await agent.run(ctx.state.user_input)
        ctx.state.response = result.data
        print(f'  Step 2 - Processed: {ctx.state.response[:80]}...')
        return RespondNode()


# ──────────────────────────────────────────────
# Node 3: Format and return final response
# ──────────────────────────────────────────────
@dataclass
class RespondNode(Node[TaskState]):
    """Format the final response"""

    async def run(self, ctx: GraphRunContext[TaskState]) -> End[str]:
        final = f'[{ctx.state.category.upper()}] {ctx.state.response}'
        print(f'  Step 3 - Final response ready')
        return End(final)


# ──────────────────────────────────────────────
# Build and run the graph
# ──────────────────────────────────────────────
async def main():
    print('=== Simple Graph Example ===\n')

    # Create the graph with ClassifyNode as the starting point
    graph = Graph(nodes=[ClassifyNode, ProcessNode, RespondNode])

    # Run with initial state
    state = TaskState(user_input='How do I reset my password?')
    print(f'Input: "{state.user_input}"')
    print('Processing through graph...')
    result = await graph.run(ClassifyNode(), state=state)

    print(f'\nFinal output: {result.output}')


if __name__ == '__main__':
    import asyncio
    asyncio.run(main())
