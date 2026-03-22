"""
03_result_validators.py - Custom Result Validation
=====================================================

Result validators let you add custom checks on LLM output.
If validation fails, the error is sent back to the LLM for retry.

Run: python 03_result_validators.py
"""

from pydantic import BaseModel, Field
from pydantic_ai import Agent, RunContext, ModelRetry


class Haiku(BaseModel):
    """A haiku poem - must have exactly 3 lines"""
    line1: str = Field(description='First line (5 syllables)')
    line2: str = Field(description='Second line (7 syllables)')
    line3: str = Field(description='Third line (5 syllables)')


agent = Agent(
    model='openai:gpt-4o-mini',
    result_type=Haiku,
    system_prompt='You write haiku poems. Follow the 5-7-5 syllable pattern strictly.',
    retries=3
)


# @agent.result_validator runs AFTER the LLM returns a result
# If it raises ModelRetry, the error message is sent back to the LLM
@agent.result_validator
def validate_haiku(ctx: RunContext, result: Haiku) -> Haiku:
    """Custom validation - check that no line is empty"""
    if not result.line1.strip() or not result.line2.strip() or not result.line3.strip():
        raise ModelRetry('All three lines must have content. Please try again.')
    return result


print('=== Result Validator Example ===\n')

result = agent.run_sync('Write a haiku about programming')
haiku = result.data
print(f'{haiku.line1}')
print(f'{haiku.line2}')
print(f'{haiku.line3}')

print(f'\nResult validated successfully!')
print(f'Type: {type(haiku).__name__}')
