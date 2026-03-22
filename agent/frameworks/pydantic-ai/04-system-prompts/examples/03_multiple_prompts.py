"""
03_multiple_prompts.py - Combining Static and Dynamic Prompts
===============================================================

You can have BOTH static and dynamic system prompts.
Pydantic AI combines them into one system prompt sent to the LLM.

Order: Static prompt -> Dynamic prompt 1 -> Dynamic prompt 2 -> ...

Run: python 03_multiple_prompts.py
"""

from pydantic_ai import Agent, RunContext

# Agent with static prompt (base behavior)
agent = Agent(
    model='openai:gpt-4o-mini',
    deps_type=str,  # Just a username
    system_prompt='You are a helpful coding assistant. Always provide examples.'
)


# Dynamic prompt 1: Add the user name
@agent.system_prompt
def add_user_context(ctx: RunContext[str]) -> str:
    return f'You are helping {ctx.deps}. Address them by name.'


# Dynamic prompt 2: Add formatting rules
@agent.system_prompt
def add_formatting_rules(ctx: RunContext[str]) -> str:
    return 'Keep your response under 100 words. Use bullet points for lists.'


# The final system prompt the LLM receives:
# "You are a helpful coding assistant. Always provide examples.
#  You are helping Nithiyan. Address them by name.
#  Keep your response under 100 words. Use bullet points for lists."

print('=== Combined Static + Dynamic Prompts ===\n')

result = agent.run_sync('What are Python data types?', deps='Nithiyan')
print(f'Response: {result.data}')
