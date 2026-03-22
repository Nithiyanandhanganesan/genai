"""
02_structured_streaming.py - Streaming Structured Output
==========================================================

You can also stream structured (Pydantic model) responses.
The model fields get populated progressively as data arrives.

Run: python 02_structured_streaming.py
"""

import asyncio
from pydantic import BaseModel, Field
from pydantic_ai import Agent


class BookInfo(BaseModel):
    title: str = Field(description='Book title')
    author: str = Field(description='Author name')
    summary: str = Field(description='Brief summary in 2 sentences')


agent = Agent(
    model='openai:gpt-4o-mini',
    result_type=BookInfo,
    system_prompt='You are a book expert.'
)


async def structured_stream_example():
    print('=== Structured Streaming Example ===\n')

    async with agent.run_stream('Tell me about 1984 by George Orwell') as response:
        # stream() yields partial results as fields get populated
        async for partial in response.stream():
            print(f'Partial: {partial}')

    # Get the final validated result
    result = response.get_data()
    print(f'\n--- Final Result ---')
    print(f'Title: {result.title}')
    print(f'Author: {result.author}')
    print(f'Summary: {result.summary}')


if __name__ == '__main__':
    asyncio.run(structured_stream_example())
