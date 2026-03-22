"""
02_conditional_graph.py - Graph with Conditional Edges
========================================================

Shows how a graph can take different paths based on conditions.
Like an if/else but for AI workflows.

Flow:
  Classify -> if question -> AnswerNode -> End
           -> if complaint -> EscalateNode -> End

Run: python 02_conditional_graph.py
"""

from __future__ import annotations
from dataclasses import dataclass
from pydantic_ai import Agent
from pydantic_ai.graph import Graph, Node, End, GraphRunContext


@dataclass
class SupportState:
    user_input: str = ''
    category: str = ''


# Node 1: Classify (routes to different nodes)
@dataclass
class ClassifyNode(Node[SupportState]):
    async def run(self, ctx: GraphRunContext[SupportState]) -> AnswerNode | EscalateNode:
        agent = Agent(
            model='openai:gpt-4o-mini',
            result_type=str,
            system_prompt='Classify as "question" or "complaint". Reply with just the word.'
        )
        result = await agent.run(ctx.state.user_input)
        ctx.state.category = result.data.strip().lower()
        print(f'  Classified as: {ctx.state.category}')

        # Conditional routing - different paths based on classification
        if 'complaint' in ctx.state.category:
            return EscalateNode()
        else:
            return AnswerNode()


# Node 2a: Answer questions
@dataclass
class AnswerNode(Node[SupportState]):
    async def run(self, ctx: GraphRunContext[SupportState]) -> End[str]:
        agent = Agent(
            model='openai:gpt-4o-mini',
            system_prompt='You are a helpful support agent. Answer questions briefly.'
        )
        result = await agent.run(ctx.state.user_input)
        return End(f'[ANSWERED] {result.data}')


# Node 2b: Escalate complaints
@dataclass
class EscalateNode(Node[SupportState]):
    async def run(self, ctx: GraphRunContext[SupportState]) -> End[str]:
        return End(
            f'[ESCALATED] Your complaint has been forwarded to a human agent. '
            f'Reference: TICKET-{hash(ctx.state.user_input) % 10000}'
        )


async def main():
    print('=== Conditional Graph Example ===\n')

    graph = Graph(nodes=[ClassifyNode, AnswerNode, EscalateNode])

    # Test with a question
    state1 = SupportState(user_input='How do I change my email address?')
    print(f'Input: "{state1.user_input}"')
    result1 = await graph.run(ClassifyNode(), state=state1)
    print(f'Output: {result1.output}\n')

    # Test with a complaint
    state2 = SupportState(user_input='I am very unhappy with your service. Nothing works!')
    print(f'Input: "{state2.user_input}"')
    result2 = await graph.run(ClassifyNode(), state=state2)
    print(f'Output: {result2.output}')

    print('\nNotice: Different inputs take different paths through the graph!')


if __name__ == '__main__':
    import asyncio
    asyncio.run(main())
