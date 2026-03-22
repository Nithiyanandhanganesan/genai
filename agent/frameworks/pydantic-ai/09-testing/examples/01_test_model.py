"""
01_test_model.py - Testing with TestModel
============================================

TestModel is a mock model that returns predictable responses.
No API calls, no cost, fast execution - perfect for testing.

Run: python 01_test_model.py
"""

from pydantic_ai import Agent
from pydantic_ai.models.test import TestModel

# Create agent with a real model name (used in production)
agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful assistant.'
)

print('=== TestModel Example ===\n')

# Override the model with TestModel for testing
# TestModel returns a simple predictable response
with agent.override(model=TestModel()):
    result = agent.run_sync('What is Python?')
    print(f'TestModel response: {result.data}')
    print(f'(This is a canned response, no API call was made)\n')

# You can also set custom text for TestModel to return
with agent.override(model=TestModel(custom_result_text='Python is great!')):
    result = agent.run_sync('What is Python?')
    print(f'Custom TestModel response: {result.data}')

print('\nNo API calls were made. No tokens consumed. No cost.')
print('This is how you test agents without spending money.')
