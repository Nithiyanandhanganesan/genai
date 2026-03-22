"""
03_agent_retries.py - How Retries Work
========================================

When you use a structured result_type (a Pydantic model),
the LLM might return data that doesn't match.
Pydantic AI automatically retries by sending the validation error
back to the LLM and asking it to fix the response.

Run: python 03_agent_retries.py
"""

from pydantic import BaseModel, Field
from pydantic_ai import Agent


# This Pydantic model defines EXACTLY what we want from the LLM
class CityInfo(BaseModel):
    """Information about a city"""
    name: str = Field(description='Name of the city')
    country: str = Field(description='Country the city is in')
    population: int = Field(gt=0, description='Population (must be positive)')
    famous_for: str = Field(description='What the city is famous for')


# result_type=CityInfo -> LLM MUST return data that fits CityInfo
# retries=3 -> if validation fails, retry up to 3 times
agent = Agent(
    model='openai:gpt-4o-mini',
    system_prompt='You provide accurate city information.',
    result_type=CityInfo,
    retries=3
)

print('=== Agent Retries Example ===\n')

# The agent returns a CityInfo object (not just text!)
result = agent.run_sync('Tell me about Tokyo.')

# Access fields with dot notation (type-safe!)
city = result.data
print(f'City: {city.name}')
print(f'Country: {city.country}')
print(f'Population: {city.population:,}')
print(f'Famous for: {city.famous_for}')
print(f'\nType: {type(city).__name__}')
print(f'As dict: {city.model_dump()}')

# Another city
print('\n--- Another City ---')
result2 = agent.run_sync('Tell me about Paris.')
city2 = result2.data
print(f'{city2.name}, {city2.country} - Pop: {city2.population:,}')
print(f'Famous for: {city2.famous_for}')
