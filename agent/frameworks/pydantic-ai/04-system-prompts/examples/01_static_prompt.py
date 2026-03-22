"""
01_static_prompt.py - Static System Prompts
=============================================

A static system prompt is set once when creating the agent.
It never changes. Use for: general behavior, personality, format.

Run: python 01_static_prompt.py
"""

from pydantic_ai import Agent

# Simple role-based prompt
tutor_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a Python programming tutor for beginners. '
                  'Explain with real-world analogies. Keep answers under 3 sentences.'
)

# Prompt with strict rules
strict_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt=(
        'You are a customer support agent for TechStore. '
        'Rules: '
        '1. Always be polite and professional. '
        '2. If you dont know, say "Let me connect you with a specialist." '
        '3. Never discuss competitor products. '
        '4. Always end with "Is there anything else I can help with?"'
    )
)

# Prompt that controls output format
format_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt=(
        'You are a fact checker. For every question: '
        'Line 1: State the claim. '
        'Line 2: Say TRUE or FALSE. '
        'Line 3: One-sentence explanation.'
    )
)

print('=== Static System Prompts ===\n')

print('--- Tutor Agent ---')
result = tutor_agent.run_sync('What is a list?')
print(f'{result.data}\n')

print('--- Support Agent ---')
result = strict_agent.run_sync('Do you sell laptops?')
print(f'{result.data}\n')

print('--- Format Agent ---')
result = format_agent.run_sync('The Earth is flat.')
print(f'{result.data}')
