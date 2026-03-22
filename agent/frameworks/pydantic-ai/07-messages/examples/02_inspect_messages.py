"""
02_inspect_messages.py - Inspecting Message History
=====================================================

After running an agent, you can inspect all messages that were exchanged.
Useful for debugging and understanding agent behavior.

Run: python 02_inspect_messages.py
"""

from pydantic_ai import Agent

agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful assistant. Be brief.'
)

print('=== Inspect Messages ===\n')

result = agent.run_sync('What is 2 + 2?')

# all_messages() returns every message exchanged
print('--- All Messages ---')
for i, msg in enumerate(result.all_messages()):
    print(f'Message {i}: {type(msg).__name__}')
    print(f'  Parts: {msg.parts}')
    print()

# new_messages() returns only messages from this run (excludes history)
print('--- New Messages Only ---')
for msg in result.new_messages():
    print(f'{type(msg).__name__}: {msg.parts}')

print(f'\nFinal answer: {result.data}')
print(f'Token usage: {result.usage()}')
