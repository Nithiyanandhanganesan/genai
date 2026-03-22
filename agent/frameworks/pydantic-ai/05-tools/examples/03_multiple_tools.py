"""
03_multiple_tools.py - Agent with Multiple Tools
==================================================

An agent can have many tools. The LLM picks which tool(s) to call
based on the user question. It may call multiple tools in sequence.

Run: python 03_multiple_tools.py
"""

from pydantic_ai import Agent
from datetime import datetime

agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful assistant with access to various tools.'
)


@agent.tool_plain
def get_current_time() -> str:
    """Get the current date and time."""
    return datetime.now().strftime('%Y-%m-%d %H:%M:%S')


@agent.tool_plain
def get_word_count(text: str) -> int:
    """Count the number of words in a text."""
    return len(text.split())


@agent.tool_plain
def reverse_string(text: str) -> str:
    """Reverse a string."""
    return text[::-1]


@agent.tool_plain
def celsius_to_fahrenheit(celsius: float) -> float:
    """Convert temperature from Celsius to Fahrenheit."""
    return (celsius * 9/5) + 32


print('=== Multiple Tools Example ===\n')

# LLM picks get_current_time
result = agent.run_sync('What time is it now?')
print(f'Time: {result.data}\n')

# LLM picks get_word_count
result = agent.run_sync('How many words are in: "Python is a great programming language"?')
print(f'Word count: {result.data}\n')

# LLM picks celsius_to_fahrenheit
result = agent.run_sync('Convert 30 degrees Celsius to Fahrenheit')
print(f'Temperature: {result.data}\n')

# LLM might use no tools if the question is simple
result = agent.run_sync('What is Python?')
print(f'No tool needed: {result.data}')
