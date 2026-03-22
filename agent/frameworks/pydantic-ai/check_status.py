import os

base = '/Users/nithiyanandhan/IdeaProjects/genai/agent/frameworks/pydantic-ai'
outfile = os.path.join(base, 'status.txt')

expected = [
    '.env.example', 'README.md', 'requirements.txt',
    '01-basics/README.md',
    '01-basics/examples/01_hello_agent.py',
    '01-basics/examples/02_model_config.py',
    '01-basics/examples/03_run_sync_vs_async.py',
    '02-agents/README.md',
    '02-agents/examples/01_agent_configuration.py',
    '02-agents/examples/02_model_settings.py',
    '02-agents/examples/03_agent_retries.py',
    '03-dependencies/README.md',
    '03-dependencies/examples/01_simple_deps.py',
    '03-dependencies/examples/02_dataclass_deps.py',
    '03-dependencies/examples/03_deps_in_tools.py',
    '04-system-prompts/README.md',
    '04-system-prompts/examples/01_static_prompt.py',
    '04-system-prompts/examples/02_dynamic_prompt.py',
    '04-system-prompts/examples/03_multiple_prompts.py',
    '05-tools/README.md',
    '05-tools/examples/01_plain_tools.py',
    '05-tools/examples/02_context_tools.py',
    '05-tools/examples/03_multiple_tools.py',
    '06-results/README.md',
    '06-results/examples/01_pydantic_result.py',
    '06-results/examples/02_union_results.py',
    '06-results/examples/03_result_validators.py',
    '07-messages/README.md',
    '07-messages/examples/01_conversation_history.py',
    '07-messages/examples/02_inspect_messages.py',
    '08-streaming/README.md',
    '08-streaming/examples/01_text_streaming.py',
    '08-streaming/examples/02_structured_streaming.py',
    '09-testing/README.md',
    '09-testing/examples/01_test_model.py',
    '09-testing/examples/02_function_model.py',
    '10-graphs/README.md',
    '10-graphs/examples/01_simple_graph.py',
    '10-graphs/examples/02_conditional_graph.py',
]

lines = []
missing = []
empty = []
for f in expected:
    p = os.path.join(base, f)
    if not os.path.exists(p):
        missing.append(f)
    elif os.path.getsize(p) == 0:
        empty.append(f)

lines.append('MISSING FILES:')
if missing:
    for m in missing:
        lines.append('  ' + m)
else:
    lines.append('  None')

lines.append('EMPTY FILES:')
if empty:
    for e in empty:
        lines.append('  ' + e)
else:
    lines.append('  None')

lines.append('TOTAL OK: ' + str(len(expected) - len(missing) - len(empty)))

with open(outfile, 'w') as f:
    f.write('\n'.join(lines) + '\n')
