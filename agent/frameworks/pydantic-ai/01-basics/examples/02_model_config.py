"""
02_model_config.py - Configuring Different LLM Models
======================================================

Shows how to use different model providers with Pydantic AI.
Switch models by just changing the model string - no other code changes needed!

Run: python 02_model_config.py
"""

import os
from pydantic_ai import Agent

# Option 1: OpenAI Models (most common)
# Needs: OPENAI_API_KEY environment variable
# gpt-4o-mini -> cheapest, good for learning
openai_agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful assistant.'
)

# Option 2: Anthropic (Claude) - uncomment to use
# Needs: ANTHROPIC_API_KEY
# claude_agent = Agent(model='anthropic:claude-3-5-sonnet-latest', system_prompt='You are a helpful assistant.')

# Option 3: Groq (Fast + Free Tier) - uncomment to use
# Needs: GROQ_API_KEY
# groq_agent = Agent(model='groq:llama-3.3-70b-versatile', system_prompt='You are a helpful assistant.')

print('=== Model Configuration Example ===\n')

question = 'What is the capital of France? Reply in one word.'

if os.getenv('OPENAI_API_KEY'):
    result = openai_agent.run_sync(question)
    print(f'OpenAI (gpt-4o-mini): {result.data}')
    print(f'Tokens used: {result.usage()}')
else:
    print('OPENAI_API_KEY not set. Run: export OPENAI_API_KEY="your-key-here"')

# You can also override the model at runtime:
# result = agent.run_sync("question", model="openai:gpt-4o")
