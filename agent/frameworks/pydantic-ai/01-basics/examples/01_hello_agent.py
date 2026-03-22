"""
01_hello_agent.py - Your First Pydantic AI Agent
=================================================

This is the simplest possible agent.
It creates an agent with a system prompt and asks it a question.

What happens internally:
1. Agent sends system prompt + user message to OpenAI
2. OpenAI returns a text response
3. Pydantic AI wraps it in a RunResult object
4. You access the response via result.data

Run: python 01_hello_agent.py
"""

from pydantic_ai import Agent

# Step 1: Create an Agent
# 'openai:gpt-4o-mini' = cheapest OpenAI model, perfect for learning
# system_prompt = tells the LLM how to behave (like a role description)
agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful assistant. Keep your answers short and simple.'
)

# Step 2: Run the Agent (synchronous)
# run_sync() sends the message and waits for the response
result = agent.run_sync('What is Python in one sentence?')

# Step 3: Access the Result
# result.data = the actual text response from the LLM
print('=== Your First Agent ===')
print(f'Question: What is Python in one sentence?')
print(f'Answer: {result.data}')

# Step 4: Check Token Usage (important for tracking costs)
# result.usage() tells how many tokens (input + output) were consumed
print(f'\nToken Usage: {result.usage()}')

# Step 5: Ask another question
# Each run_sync() is independent - agent has NO memory of previous call
result2 = agent.run_sync('What is 2 + 2?')
print(f'\nQuestion: What is 2 + 2?')
print(f'Answer: {result2.data}')
