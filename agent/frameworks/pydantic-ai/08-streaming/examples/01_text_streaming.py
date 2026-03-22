"""
01_text_streaming.py - Streaming Text Responses
=================================================

Stream the LLM response token by token instead of waiting for the full response.
Requires async (cannot use run_sync for streaming).

Run: python 01_text_streaming.py
"""

import asyncio
from pydantic_ai import Agent

agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a storyteller. Tell short stories in 3-4 sentences.'
)


async def stream_example():
    print('=== Text Streaming Example ===\n')
    print('Streaming response token by token:\n')

    # run_stream() returns a context manager
    # You must use 'async with' to use it
    async with agent.run_stream('Tell me a short story about a robot') as response:
        # stream_text() yields text chunks as they arrive
        async for chunk in response.stream_text():
            # Print each chunk without newline (builds up the text)
            print(chunk, end='', flush=True)

    # Print newline after streaming is done
    print('\n\n--- Streaming complete ---')


if __name__ == '__main__':
    asyncio.run(stream_example())
