"""
02_model_settings.py - Controlling LLM Behavior
=================================================

ModelSettings lets you fine-tune how the LLM generates responses.
Most important settings:
- temperature: how creative/random the response is
- max_tokens: maximum length of the response

Run: python 02_model_settings.py
"""

from pydantic_ai import Agent
from pydantic_ai.settings import ModelSettings

# Temperature 0 = always same answer (deterministic)
factual_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a factual assistant. Be precise.',
    model_settings=ModelSettings(temperature=0.0)
)

# Temperature 1.5 = creative and varied answers
creative_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a creative storyteller.',
    model_settings=ModelSettings(temperature=1.5)
)

# max_tokens limits response length
short_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful assistant.',
    model_settings=ModelSettings(temperature=0.5, max_tokens=50)
)

print('=== Model Settings Examples ===\n')

# Temperature 0 - same question twice, same answer
print('--- Temperature 0 (Deterministic) ---')
for i in range(2):
    result = factual_agent.run_sync('Name one planet.')
    print(f'  Attempt {i+1}: {result.data}')

# Temperature 1.5 - same question twice, different answers
print('\n--- Temperature 1.5 (Creative) ---')
for i in range(2):
    result = creative_agent.run_sync('Name one planet.')
    print(f'  Attempt {i+1}: {result.data}')

# Max tokens - short response
print('\n--- Max Tokens = 50 ---')
result = short_agent.run_sync('Explain quantum physics.')
print(f'  Response: {result.data}')

# Override settings at runtime
print('\n--- Override at Runtime ---')
result = factual_agent.run_sync(
    'Write a haiku about coding.',
    model_settings=ModelSettings(temperature=1.8)
)
print(f'  Creative haiku: {result.data}')
