"""
02_stream_graph.py - See state after every node using stream().
Run: python 02_stream_graph.py
"""
import time
from typing import TypedDict
from langgraph.graph import StateGraph, START, END


class State(TypedDict):
    text: str


def step1(state: State) -> dict:
    return {"text": state["text"] + " -> step1"}


def step2(state: State) -> dict:
    return {"text": state["text"] + " -> step2"}


g = StateGraph(State)
g.add_node("step1", step1)
g.add_node("step2", step2)
g.add_edge(START, "step1")
g.add_edge("step1", "step2")
g.add_edge("step2", END)
app = g.compile()

# stream_mode="updates": only what each node returned
for chunk in app.stream({"text": "start"}, stream_mode="updates"):
    print("  [step1 running...]", flush=True)
    time.sleep(2)  # simulate slow work
    print("update:", chunk)

# stream_mode="values": full state after each step
for chunk in app.stream({"text": "start"}, stream_mode="values"):
    print("  [step2 running...]", flush=True)
    time.sleep(2)  # simulate slow work
    print("state:", chunk)


# Print the graph as Mermaid text (paste into mermaid.live to see it)
print(app.get_graph().draw_mermaid())
