"""
01_hello_graph.py - Minimal LangGraph: START -> greet -> shout -> END
No API key needed.
Run: python 01_hello_graph.py
"""
from typing import TypedDict
from langgraph.graph import StateGraph, START, END


# 1. STATE: the shared data every node can read/write
class State(TypedDict):
    name: str
    message: str
    test : str


# 2. NODES: plain functions. Take state, return a PARTIAL update (dict)
def greet(state: State) -> dict:
    return {"message": f"Hello, {state['name']}"}


def shout(state: State) -> dict:
    return {"message": state["message"].upper() + "!"}


# 3. BUILD the graph and wire edges
builder = StateGraph(State)
builder.add_node("greet", greet)
builder.add_node("shout", shout)
builder.add_edge(START, "greet")   # entry
builder.add_edge("greet", "shout")  # fixed order
builder.add_edge("shout", END)      # exit

# 4. COMPILE then run
app = builder.compile()
result = app.invoke({"name": "Nithi" , "test": "this is a test"})
print(result)  # {'name': 'Nithi', 'message': 'HELLO, NITHI!'}

#creates the initial state: {"name": "Nithi"}. message is not set yet.
#START -> greet: greet reads state['name'] and returns {"message": "Hello, Nithi"}. LangGraph merges this into the state: {"name": "Nithi", "message": "Hello, Nithi"}
#greet -> shout: shout reads state["message"], calls .upper() and adds !. It returns {"message": "HELLO, NITHI!"}. The merge overwrites message: {"name": "Nithi", "message": "HELLO, NITHI!"}
#shout -> END: the graph stops and invoke returns the final state, which is the printed result.
