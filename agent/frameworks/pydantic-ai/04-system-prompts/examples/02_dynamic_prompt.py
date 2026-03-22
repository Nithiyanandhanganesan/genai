"""
02_dynamic_prompt.py - Dynamic System Prompts
===============================================

Dynamic system prompts are generated at runtime using a function.
They can use dependencies to personalize the prompt.

Use for: user-specific instructions, time-based behavior, role-based access.

Run: python 02_dynamic_prompt.py
"""

from dataclasses import dataclass
from datetime import datetime
from pydantic_ai import Agent, RunContext


@dataclass
class UserInfo:
    name: str
    expertise_level: str  # 'beginner', 'intermediate', 'expert'


# Create agent with NO static system prompt
agent = Agent(
    model='openai:gpt-4o-mini',
    deps_type=UserInfo
)


# Dynamic prompt 1: Personalization based on user
# This function runs every time agent.run_sync() is called
@agent.system_prompt
def personalize(ctx: RunContext[UserInfo]) -> str:
    """Generate system prompt based on who the user is"""
    user = ctx.deps
    if user.expertise_level == 'beginner':
        style = 'Use simple words, avoid jargon, give analogies.'
    elif user.expertise_level == 'intermediate':
        style = 'Use technical terms but explain complex ones.'
    else:
        style = 'Be concise and technical. Skip basics.'

    return (
        f'You are talking to {user.name} ({user.expertise_level} level). '
        f'{style}'
    )


# Dynamic prompt 2: Time-based behavior
# You can have MULTIPLE dynamic prompts - they all get combined
@agent.system_prompt
def time_based_greeting(ctx: RunContext[UserInfo]) -> str:
    """Add time-aware instructions"""
    hour = datetime.now().hour
    if hour < 12:
        return 'Start your response with "Good morning!"'
    elif hour < 18:
        return 'Start your response with "Good afternoon!"'
    else:
        return 'Start your response with "Good evening!"'


print('=== Dynamic System Prompts ===\n')

# Beginner user - gets simple explanations
beginner = UserInfo(name='Student', expertise_level='beginner')
result = agent.run_sync('What is an API?', deps=beginner)
print(f'Beginner answer: {result.data}\n')

# Expert user - gets technical explanations
expert = UserInfo(name='SeniorDev', expertise_level='expert')
result = agent.run_sync('What is an API?', deps=expert)
print(f'Expert answer: {result.data}\n')

print('Same question, different responses based on expertise level!')
