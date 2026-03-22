"""
01_conversation_history.py - Multi-turn Conversations
======================================================

By default, each agent.run_sync() is independent.
To maintain context, pass message_history from previous results.

Run: python 01_conversation_history.py
"""

from pydantic_ai import Agent

agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a friendly assistant. Keep answers short.'
)

print('=== Conversation History ===\n')

# Turn 1: Introduce yourself
result1 = agent.run_sync('Hi! My name is Nithiyan and I like Python.')
print(f'Turn 1 - User: Hi! My name is Nithiyan and I like Python.')
print(f'Turn 1 - Agent: {result1.data}\n')

# Turn 2: Ask something that requires memory of Turn 1
# Pass message_history so the agent remembers the conversation
result2 = agent.run_sync(
    'What is my name and what do I like?',
    message_history=result1.all_messages()  # Pass history from Turn 1
)
print(f'Turn 2 - User: What is my name and what do I like?')
print(f'Turn 2 - Agent: {result2.data}\n')

# Turn 3: Continue the conversation
result3 = agent.run_sync(
    'Suggest a project for me based on my interests.',
    message_history=result2.all_messages()  # Pass ALL history (Turn 1 + 2)
)
print(f'Turn 3 - User: Suggest a project based on my interests.')
print(f'Turn 3 - Agent: {result3.data}\n')

# Without message_history - agent forgets everything
result_no_history = agent.run_sync('What is my name?')
print(f'No history - User: What is my name?')
print(f'No history - Agent: {result_no_history.data}')
print('(Agent does not know the name without history!)')
