"""
01_pydantic_result.py - Structured Output with Pydantic Models
================================================================

Instead of getting plain text, get structured data from the LLM.
The LLM returns data that matches your Pydantic model exactly.

Run: python 01_pydantic_result.py
"""

from pydantic import BaseModel, Field
from pydantic_ai import Agent


# Define what we want the LLM to return
# This is a Pydantic model - it defines the STRUCTURE of the output
class MovieReview(BaseModel):
    """Structured movie review"""
    title: str = Field(description='Title of the movie')
    year: int = Field(description='Year the movie was released')
    rating: float = Field(ge=1, le=10, description='Rating from 1 to 10')
    genre: str = Field(description='Primary genre of the movie')
    one_line_summary: str = Field(description='One sentence summary')


# Create agent with result_type = MovieReview
# The agent will ALWAYS return a MovieReview object (not text)
agent = Agent(
    model='openai:gpt-4o-mini',
    result_type=MovieReview,
    system_prompt='You are a movie expert. Provide accurate movie information.'
)

print('=== Structured Output Example ===\n')

# Ask about a movie - result.data will be a MovieReview object
result = agent.run_sync('Tell me about The Matrix')

# Access fields with dot notation (type-safe!)
movie = result.data
print(f'Title: {movie.title}')
print(f'Year: {movie.year}')
print(f'Rating: {movie.rating}/10')
print(f'Genre: {movie.genre}')
print(f'Summary: {movie.one_line_summary}')
print(f'\nType: {type(movie).__name__}')
print(f'As dict: {movie.model_dump()}')

# Another movie
print('\n--- Another Movie ---')
result2 = agent.run_sync('Tell me about Inception')
movie2 = result2.data
print(f'{movie2.title} ({movie2.year}) - {movie2.rating}/10')
print(f'Genre: {movie2.genre}')
print(f'Summary: {movie2.one_line_summary}')
