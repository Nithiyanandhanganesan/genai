"""
03_run_sync_vs_async.py - Sync vs Async Execution
===================================================

Pydantic AI supports two ways to run agents:
1. run_sync() - Synchronous (blocking) - simple, good for learning
2. run() - Asynchronous (non-blocking) - better for web apps

For learning, always use run_sync().

Run: python 03_run_sync_vs_async.py
"""

import asyncio
from pydantic_ai import Agent

agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='Reply in exactly one sentence.'
)


# Method 1: Synchronous (Simple)
# Blocks until response is ready - one question, wait, one answer
def sync_example():
    print('=== Sync Example ===')
    result = agent.run_sync('What is gravity?')
    print(f'Answer: {result.data}\n')


# Method 2: Asynchronous
# Uses await keyword - does not block
async def async_example():
    print('=== Async Example ===')
    result = await agent.run('What is magnetism?')
    print(f'Answer: {result.data}\n')


# Method 3: Parallel Async - ask multiple questions at once
# Faster because all questions sent simultaneously
async def parallel_example():
    print('=== Parallel Async Example ===')
    task1 = agent.run('What is the sun?')
    task2 = agent.run('What is the moon?')
    task3 = agent.run('What is a star?')

    results = await asyncio.gather(task1, task2, task3)
    for i, result in enumerate(results, 1):
        print(f'Answer {i}: {result.data}')


if __name__ == '__main__':
    sync_example()
    asyncio.run(async_example())
    asyncio.run(parallel_example())
