"""
02_function_model.py - Testing with FunctionModel
====================================================

FunctionModel lets you provide a custom function that generates responses.
You have full control over what the model returns.

Run: python 02_function_model.py
"""

from pydantic_ai import Agent
from pydantic_ai.models.function import FunctionModel, AgentInfo
from pydantic_ai.messages import ModelResponse, TextPart


# Custom function that acts as our mock LLM
# It receives the messages and returns a ModelResponse
def my_mock_model(messages: list, info: AgentInfo) -> ModelResponse:
    """
    This function replaces the real LLM.
    You can inspect the messages and return whatever you want.
    """
    # Get the last user message
    last_message = str(messages[-1])

    # Return different responses based on input
    if 'python' in last_message.lower():
        response_text = 'Python is a programming language.'
    elif 'java' in last_message.lower():
        response_text = 'Java is another programming language.'
    else:
        response_text = 'I am a mock model. I only know about Python and Java.'

    return ModelResponse(parts=[TextPart(content=response_text)])


agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You are a helpful assistant.'
)

print('=== FunctionModel Example ===\n')

# Override with our custom function model
with agent.override(model=FunctionModel(my_mock_model)):
    result1 = agent.run_sync('Tell me about Python')
    print(f'Q: Tell me about Python')
    print(f'A: {result1.data}\n')

    result2 = agent.run_sync('Tell me about Java')
    print(f'Q: Tell me about Java')
    print(f'A: {result2.data}\n')

    result3 = agent.run_sync('Tell me about cooking')
    print(f'Q: Tell me about cooking')
    print(f'A: {result3.data}')

print('\nAll responses came from our custom function. No API calls made.')
