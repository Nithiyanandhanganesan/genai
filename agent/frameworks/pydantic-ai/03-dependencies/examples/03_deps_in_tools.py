"""
03_deps_in_tools.py - Accessing Dependencies in Tools
=======================================================

Tools can access dependencies via ctx.deps.
This is where DI shines - your tool uses injected services.

Run: python 03_deps_in_tools.py
"""

from dataclasses import dataclass
from pydantic_ai import Agent, RunContext

# Simulated database
FAKE_ORDER_DB = {
    'nithiyan': ['MacBook Pro', 'Python Book', 'Keyboard'],
    'alice': ['iPad', 'Headphones'],
}


@dataclass
class AppDeps:
    user_id: str
    db: dict  # In real apps, this would be a DB connection


agent = Agent(
    model='openai:gpt-4o-mini',
    deps_type=AppDeps,
    system_prompt='You are a shopping assistant. Use tools to look up orders.'
)


# @agent.tool registers a tool WITH RunContext (has dependencies)
@agent.tool
def get_order_history(ctx: RunContext[AppDeps]) -> str:
    """Get the current user order history from the database."""
    user_id = ctx.deps.user_id
    orders = ctx.deps.db.get(user_id, [])
    if not orders:
        return f'No orders found for {user_id}.'
    return f'Orders for {user_id}: {", ".join(orders)} ({len(orders)} items)'


@agent.tool
def get_order_count(ctx: RunContext[AppDeps]) -> str:
    """Get how many orders the current user has."""
    orders = ctx.deps.db.get(ctx.deps.user_id, [])
    return f'{ctx.deps.user_id} has {len(orders)} orders.'


print('=== Dependencies in Tools ===\n')

nithiyan_deps = AppDeps(user_id='nithiyan', db=FAKE_ORDER_DB)

result = agent.run_sync('What have I ordered recently?', deps=nithiyan_deps)
print(f'Nithiyan: {result.data}\n')

# Different user, same agent
alice_deps = AppDeps(user_id='alice', db=FAKE_ORDER_DB)
result = agent.run_sync('Show me my orders.', deps=alice_deps)
print(f'Alice: {result.data}\n')

print('Same agent code, different data based on who is logged in!')
