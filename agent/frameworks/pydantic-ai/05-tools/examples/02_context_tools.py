"""
02_context_tools.py - Tools with Dependencies (Context)
=========================================================

Context tools receive RunContext, which gives access to dependencies.
Use for: database queries, API calls, user-specific data.

Run: python 02_context_tools.py
"""

from dataclasses import dataclass
from pydantic_ai import Agent, RunContext

# Simulated database
USERS_DB = {
    'nithiyan': {'name': 'Nithiyan', 'email': 'nithiyan@example.com', 'plan': 'premium'},
    'alice': {'name': 'Alice', 'email': 'alice@example.com', 'plan': 'free'},
}


@dataclass
class SupportDeps:
    """Dependencies for our support agent"""
    user_id: str
    db: dict


agent = Agent(
    model='openai:gpt-4o-mini',
    deps_type=SupportDeps,
    system_prompt='You are a customer support agent. Use tools to look up user info.'
)


# @agent.tool = register a tool WITH RunContext (has dependencies)
# ctx.deps gives access to SupportDeps
@agent.tool
def get_user_info(ctx: RunContext[SupportDeps]) -> str:
    """Look up the current user information from the database."""
    user = ctx.deps.db.get(ctx.deps.user_id)
    if user:
        return f"Name: {user['name']}, Email: {user['email']}, Plan: {user['plan']}"
    return "User not found."


@agent.tool
def check_plan(ctx: RunContext[SupportDeps]) -> str:
    """Check the current user subscription plan."""
    user = ctx.deps.db.get(ctx.deps.user_id)
    if user:
        return f"Current plan: {user['plan']}"
    return "User not found."


print('=== Context Tools Example ===\n')

# Create deps for nithiyan
deps = SupportDeps(user_id='nithiyan', db=USERS_DB)

result = agent.run_sync('What is my email address?', deps=deps)
print(f'Nithiyan asks: What is my email? -> {result.data}\n')

result = agent.run_sync('What plan am I on?', deps=deps)
print(f'Nithiyan asks: What plan? -> {result.data}\n')

# Different user, same agent
deps2 = SupportDeps(user_id='alice', db=USERS_DB)
result = agent.run_sync('What plan am I on?', deps=deps2)
print(f'Alice asks: What plan? -> {result.data}')
