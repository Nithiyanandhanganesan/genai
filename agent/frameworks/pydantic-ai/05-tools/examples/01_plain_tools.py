"""
01_plain_tools.py - Plain Tools (No Dependencies)
===================================================

Plain tools are simple functions the LLM can call.
They do NOT have access to dependencies (RunContext).
Use for: calculations, string manipulation, static lookups.

The LLM reads the function docstring and parameter types
to understand what the tool does and when to call it.

Run: python 01_plain_tools.py
"""

from pydantic_ai import Agent

agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful math assistant. Use tools when needed.'
)


# @agent.tool_plain = register a tool WITHOUT RunContext (no dependencies)
# The LLM sees: function name, docstring, and parameter types
@agent.tool_plain
def add_numbers(a: float, b: float) -> float:
    """Add two numbers together. Use this for addition."""
    return a + b


@agent.tool_plain
def multiply_numbers(a: float, b: float) -> float:
    """Multiply two numbers. Use this for multiplication."""
    return a * b


@agent.tool_plain
def get_square_root(number: float) -> float:
    """Calculate the square root of a number."""
    import math
    return math.sqrt(number)


print('=== Plain Tools Example ===\n')

# The LLM will automatically call add_numbers tool
result = agent.run_sync('What is 25 + 37?')
print(f'25 + 37 = {result.data}\n')

# The LLM will call multiply_numbers
result = agent.run_sync('What is 12 times 8?')
print(f'12 x 8 = {result.data}\n')

# The LLM will call get_square_root
result = agent.run_sync('What is the square root of 144?')
print(f'sqrt(144) = {result.data}\n')

# The LLM might call multiple tools
result = agent.run_sync('Add 10 and 20, then multiply the result by 3')
print(f'(10+20) x 3 = {result.data}')
