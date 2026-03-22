"""
01_agent_configuration.py - Agent Configuration Options
=========================================================

Shows all the ways you can configure an Agent.
An Agent is like a job description for an AI worker.

Run: python 01_agent_configuration.py
"""

from pydantic_ai import Agent

# Config 1: Minimal Agent - just a model, no system prompt
# Returns plain text (str) by default
minimal_agent = Agent(model='openai:gpt-4o-mini')

# Config 2: Agent with System Prompt
# system_prompt tells the LLM how to behave
helpful_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a friendly teacher. Explain things simply for beginners.'
)

# Config 3: Agent with Retries
# retries = how many times to retry if LLM gives invalid response
retry_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful assistant.',
    retries=3  # Retry up to 3 times if validation fails
)

# Config 4: Agent with Multi-line System Prompt
detailed_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt=(
        'You are a Python programming tutor. '
        'Always explain concepts with simple examples. '
        'Keep answers under 3 sentences. '
        'If the user asks something unrelated to Python, '
        'politely redirect them to Python topics.'
    )
)

print('=== Agent Configuration Examples ===\n')

print('--- Minimal Agent ---')
result = minimal_agent.run_sync('Say hello in 5 words')
print(f'Response: {result.data}\n')

print('--- Agent with System Prompt ---')
result = helpful_agent.run_sync('What is a variable?')
print(f'Response: {result.data}\n')

print('--- Agent with Detailed Prompt ---')
result = detailed_agent.run_sync('What is a list in Python?')
print(f'Response: {result.data}\n')

# You can override model at runtime
print('--- Override Model at Runtime ---')
result = helpful_agent.run_sync(
    'What is a function?',
    model='openai:gpt-4o-mini'  # Override for this call
)
print(f'Response: {result.data}')
