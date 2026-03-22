"""
02_dataclass_deps.py - Dataclass Dependencies
=================================================

When you need multiple dependencies, use a dataclass.
Example: user name, role, and language.

Run: python 02_dataclass_deps.py
"""

from dataclasses import dataclass
from pydantic_ai import Agent, RunContext


@dataclass
class UserContext:
    """All context about the current user"""
    username: str
    role: str           # 'admin' or 'user'
    language: str       # preferred language


agent = Agent(
    model='openai:gpt-4o-mini',
    deps_type=UserContext,
    system_prompt='You are a helpful assistant.'
)


@agent.system_prompt
def personalize_prompt(ctx: RunContext[UserContext]) -> str:
    """Access multiple fields from the dependency dataclass"""
    user = ctx.deps
    return (
        f'User: {user.username} (Role: {user.role}). '
        f'Respond in {user.language}. '
        f'If admin, provide detailed answers. If user, keep it simple.'
    )


print('=== Dataclass Dependencies ===\n')

# Admin gets detailed answers
admin = UserContext(username='Nithiyan', role='admin', language='English')
result1 = agent.run_sync('How does caching work?', deps=admin)
print(f'Admin: {result1.data}\n')

# Regular user gets simple answers
user = UserContext(username='NewUser', role='user', language='English')
result2 = agent.run_sync('How does caching work?', deps=user)
print(f'User: {result2.data}')
