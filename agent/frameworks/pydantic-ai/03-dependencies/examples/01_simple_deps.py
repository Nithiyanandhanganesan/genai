"""
01_simple_deps.py - Simple Dependency Injection
=================================================

Pass a username (string) as a dependency.
The system prompt uses it to personalize responses.

Run: python 01_simple_deps.py
"""

from pydantic_ai import Agent, RunContext

# deps_type=str means we will pass a string as dependency
agent = Agent(
    model='openai:gpt-4o-mini',
    deps_type=str,
    system_prompt='You are a helpful assistant.'
)


# @agent.system_prompt adds a dynamic system prompt
# ctx.deps contains whatever we pass as 'deps' in run_sync()
@agent.system_prompt
def add_user_name(ctx: RunContext[str]) -> str:
    """Runs BEFORE sending to LLM. Personalizes the prompt."""
    return f"The user's name is {ctx.deps}. Address them by name."


print('=== Simple Dependency Injection ===\n')

# Pass 'Nithiyan' as dependency
result1 = agent.run_sync('Hello! What can you help me with?', deps='Nithiyan')
print(f'Response to Nithiyan: {result1.data}\n')

# Pass 'Alice' - same agent, different personalization
result2 = agent.run_sync('Hello! What can you help me with?', deps='Alice')
print(f'Response to Alice: {result2.data}\n')

print('Same agent, different responses based on who the user is!')
