"""
02_union_results.py - Union Result Types
==========================================

The LLM can return different types of results based on the question.
Use Union types to let the LLM choose the appropriate response format.

Run: python 02_union_results.py
"""

from typing import Union
from pydantic import BaseModel, Field
from pydantic_ai import Agent


# Two possible result types
class SuccessResult(BaseModel):
    """When the request can be fulfilled"""
    message: str = Field(description='Success message')
    data: str = Field(description='The requested data')


class ErrorResult(BaseModel):
    """When the request cannot be fulfilled"""
    error_message: str = Field(description='What went wrong')
    suggestion: str = Field(description='What the user should try instead')


# Agent can return EITHER SuccessResult or ErrorResult
# The LLM decides which one is appropriate
agent = Agent(
    model='openai:gpt-4o-mini',
    result_type=Union[SuccessResult, ErrorResult],  # Union = one of these types
    system_prompt=(
        'You help users find capital cities. '
        'If you know the capital, return a success. '
        'If the country does not exist, return an error.'
    )
)

print('=== Union Result Types ===\n')

# Valid request - should return SuccessResult
result = agent.run_sync('What is the capital of Japan?')
print(f'Type: {type(result.data).__name__}')
if isinstance(result.data, SuccessResult):
    print(f'Success: {result.data.message}')
    print(f'Data: {result.data.data}')
elif isinstance(result.data, ErrorResult):
    print(f'Error: {result.data.error_message}')

# Invalid request - should return ErrorResult
print()
result2 = agent.run_sync('What is the capital of Narnia?')
print(f'Type: {type(result2.data).__name__}')
if isinstance(result2.data, SuccessResult):
    print(f'Success: {result2.data.message}')
elif isinstance(result2.data, ErrorResult):
    print(f'Error: {result2.data.error_message}')
    print(f'Suggestion: {result2.data.suggestion}')
