<h1 align="center">fabsim</h1>

<p align="center"><i>A Java-based simulator for semiconductor fabrication facilities.</i></p>

<p align="center">
  <a href="#repository-structure"><b>Repository Structure</b></a> ·
  <a href="#supported-benchmarks"><b>Supported Benchmarks</b></a> ·
  <a href="#installation-and-usage"><b>Installation & Usage</b></a> ·
  <a href="#dispatch-log-format"><b>Dispatch Logs</b></a> ·
  <a href="#development"><b>Development</b></a>
</p>

---

`fabsim` is a Java-based discrete-event simulator for modelling semiconductor fabrication facilities.
It is primarily intended to be embedded from Python so external dispatching logic, such as custom
heuristics, optimization routines, or machine learning agents, can drive dispatch decisions through [JPype](https://jpype.readthedocs.io/en/latest/). 
The project provides reusable simulation primitives for fab environments and includes
out-of-the-box benchmark implementations such as MiniFab. A Docker-based runner is also available as a
convenience path for baseline simulations and dispatch-log data generation with predefined local rules.

## Repository Structure

The repository is organized as a multi-module Maven project:

- [`de.terministic.fabsimcore`](de.terministic.fabsimcore/): core discrete-event simulation infrastructure,
  including events, event-list managers, simulation end conditions, duration distributions, and base
  simulation interfaces.
- [`de.terministic.fabsimmetamodel`](de.terministic.fabsimmetamodel/): fab-specific metamodel built on top
  of the core simulator, including components for lots, wafers, tools, routing, setup, batching,
  dispatching rules, statistics, and external dispatch integration.
- [`de.terministic.fabsimbenchmarks`](de.terministic.fabsimbenchmarks/): runnable benchmark implementations,
  the benchmark CLI entrypoint, and benchmark result aggregation.

## Supported Benchmarks

Benchmark implementations are located in
[`benchmarks/`](de.terministic.fabsimbenchmarks/src/main/java/de/terministic/fabsim/benchmarks/).

Currently supported:

- MiniFab: an implementation of the Intel five-machine, six-step benchmark introduced in
  James C. Spier and Karl G. Kempf,
  ["Simulation of emergent behavior in manufacturing systems"](https://ieeexplore.ieee.org/document/484347).

## Installation and Usage

fabsim supports two common execution modes:

- [JPype](https://jpype.readthedocs.io/en/latest/) execution from Python, useful when dispatch decisions should come from an external Python policy,
  such as a reinforcement-learning agent or a custom heuristic.
- Docker execution with predefined local dispatch rules (`random`, `fifo`), useful as a convenience option
  for baseline simulations and dispatch-log data generation.

### Build the jar and run via JPype

This setup lets MiniFab delegate dispatch decisions to an external Python-based service through
[JPype](https://jpype.readthedocs.io/en/latest/). The Java simulation sends each dispatch request to Python,
and Python returns the selected candidate.

Use Maven from the repository root to build the shaded jar:

```bash
mvn clean package -Dmaven.test.skip=true
```

Copy the resulting single jar into your Python project, for example into a `libs/` folder inside that
project:

- `de.terministic.fabsimbenchmarks/target/fabsim-benchmarks.jar`

This jar bundles the project classes and runtime dependencies needed by JPype.

The Java side exposes a `DispatchProvider` interface that Python implements through JPype. Java passes a
`DispatchDecisionRequest` containing `fab_state` and `candidates`, and Python returns a
`DispatchDecisionResponse` with the selected flow item id. The request methods available inside
`selectDispatchCandidate` are described in [Dispatch Request API](#dispatch-request-api).

Example flow:

```python
from pathlib import Path

import jpype
from jpype import JImplements, JOverride

python_project_root = Path("/path/to/your/python/project")
jar_dir = python_project_root / "libs"
jpype.startJVM(
    jpype.getDefaultJVMPath(),
    "--enable-native-access=ALL-UNNAMED",
    classpath=[
        str(jar_dir / "fabsim-benchmarks.jar"),
    ],
)

DispatchProvider = jpype.JClass("de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider")
MiniFab = jpype.JClass("de.terministic.fabsim.benchmarks.implementations.MiniFab")
DispatchDecisionResponse = jpype.JClass("de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionResponse")

@JImplements(DispatchProvider)
class Provider:
    @JOverride
    def selectDispatchCandidate(self, request):
        candidates = request.getCandidates()
        selected = candidates.get(0).getId()
        return DispatchDecisionResponse.of(selected)

provider = Provider()
mini_fab = MiniFab(provider)
simulation_time_hours = 168
warmup_time_hours = 24
seed = 123

result = mini_fab.run(
    simulation_time_hours,
    warmup_time_hours,
    seed,
)

completed_wafers_per_day = result.getCompletedWafersPerDay()
```

All result methods available on `result` are listed in [Run Result Methods](#run-result-methods).

### Build the container and run via Docker

Build the image from the repository root:

```bash
docker build -t fabsim .
```

Run fabsim in Docker when you want to use predefined dispatch rules and optionally write dispatch logs to
a mounted host path. This mode is intended as a convenience option for data generation and baseline runs;
use the JPype flow above when dispatch decisions should come from external Python logic.

The Docker launcher requires selecting a fab implementation with `--fab`; `minifab` is currently the only
available implementation.

```bash
docker run --rm \
  fabsim \
  --fab minifab \
  --simulation-time 168 \
  --warmup-time 24 \
  --seed 123 \
  --dispatch-rule fifo
```

To write the dispatch log to disk in the format described in
[Dispatch Log Format](#dispatch-log-format), mount a host directory and pass `--log-file`.

```bash
mkdir -p logs

docker run --rm \
  -v "$PWD/logs:/logs" \
  fabsim \
  --fab minifab \
  --simulation-time 168 \
  --warmup-time 24 \
  --seed 123 \
  --dispatch-rule fifo \
  --log-file /logs/minifab.jsonl
```

#### CLI reference

Required arguments:

- `--fab minifab`
- `--simulation-time <hours>`
- `--seed <long>`
- `--dispatch-rule random|fifo`

Optional arguments:

- `--warmup-time <hours>`: warmup period excluded from reported metrics.
- `--log-file <path>`: JSONL dispatch-log output path.
- `--help`, `-h`: print CLI usage.

**NOTE**: The `--seed` controls benchmark-internal stochastic model inputs, such as generated duration distributions. Dispatching remains outside this seed: for example, `--dispatch-rule random` still makes unseeded random dispatch decisions.

After the simulation finishes, the launcher prints metrics for:

- completed wafers per day
- tardiness per wafer in minutes
- completed wafers
- tardy wafers
- flow factor

These metrics exclude any time spent in the warmup window.

## Dispatch Log Format

When `--log-file <path>` is passed to the Docker/CLI runner, `LocalLogWriter` writes dispatch decisions as
JSONL. Each line is one complete JSON object for one dispatch decision.

All time values are in milliseconds unless stated otherwise.

| Field | Type | Description |
| --- | --- | --- |
| `fab_state` | object | Fab snapshot before the dispatch decision. |
| `fab_state.simulation_time` | integer | Current simulation timestamp. |
| `fab_state.tool_groups` | array | Snapshot of every tool group in the fab. |
| `fab_state.cost_snapshot` | object | Aggregate cost and WIP snapshot. |
| `fab_state.cost_snapshot.total_projected_tardiness` | integer | Projected wafer-level tardiness, in minutes, over queued and in-process work. |
| `fab_state.cost_snapshot.work_in_progress` | integer | Current wafer-level work in progress. |
| `fab_state.tool_groups[].name` | string | Tool-group name. |
| `fab_state.tool_groups[].waiting_for_dispatch` | boolean | Whether this is the tool group currently requesting a dispatch decision. |
| `fab_state.tool_groups[].tools` | array | Tools belonging to the tool group. |
| `fab_state.tool_groups[].tools[].id` | integer | Tool identifier. |
| `fab_state.tool_groups[].tools[].current_tool_state` | string | Current tool state, such as `STANDBY`, `SETUP`, `PROCESSING`, `MAINTENANCE`, or `BREAKDOWN`. |
| `fab_state.tool_groups[].queued_items` | array | Items waiting in the tool-group queue. |
| `fab_state.tool_groups[].queued_items[].remaining_cycle_time` | integer | Projected remaining cycle time. |
| `fab_state.tool_groups[].queued_items[].processing_time` | integer | Processing time required at the current operation. |
| `fab_state.tool_groups[].queued_items[].expected_setup_time` | integer | Expected setup time before processing can start. |
| `fab_state.tool_groups[].queued_items[].time_since_arrival` | integer | Time since the item arrived in the current queue. |
| `fab_state.tool_groups[].queued_items[].priority` | integer | Item priority. |
| `fab_state.tool_groups[].queued_items[].lateness` | integer | Current lateness relative to the item's due-date target. |
| `fab_state.tool_groups[].queued_items[].recipe` | string | Recipe required for the current operation. |
| `fab_state.tool_groups[].in_process_items` | array | Items currently being processed by the tool group. |
| `fab_state.tool_groups[].in_process_items[].remaining_cycle_time` | integer | Projected remaining cycle time. |
| `fab_state.tool_groups[].in_process_items[].processing_time_left` | integer | Remaining processing time on the current tool. |
| `fab_state.tool_groups[].in_process_items[].priority` | integer | Item priority. |
| `fab_state.tool_groups[].in_process_items[].lateness` | integer | Current lateness relative to the item's due-date target. |
| `dispatch_decision` | object | Decision recorded by the selected dispatch rule. |
| `dispatch_decision.chosen_flow_item` | object | Snapshot of the selected queued item. |
| `dispatch_decision.chosen_flow_item.*` | mixed | Same fields as a queued item in `fab_state.tool_groups[].queued_items[]`. |

Example line:

```json
{"fab_state":{"simulation_time":86400000,"tool_groups":[],"cost_snapshot":{"total_projected_tardiness":0,"work_in_progress":0}},"dispatch_decision":{"chosen_flow_item":{"remaining_cycle_time":120000,"processing_time":60000,"expected_setup_time":0,"time_since_arrival":30000,"priority":0,"lateness":0,"recipe":"recipe-a"}}}
```

## Dispatch Request API

Inside a JPype `selectDispatchCandidate(self, request)` implementation, the `request` object provides the
same fab-state shape as the log format plus candidate IDs needed to return a decision.

```python
@JImplements(DispatchProvider)
class Provider:
    @JOverride
    def selectDispatchCandidate(self, request):
        fab_state = request.getFabState()
        cost = fab_state.getCostSnapshot()
        wip = cost.getWorkInProgress()

        for i in range(fab_state.getToolGroups().size()):
            tool_group = fab_state.getToolGroups().get(i)
            if tool_group.getWaitingForDispatch():
                queue_length = tool_group.getQueuedItems().size()

        candidates = request.getCandidates()
        selected = candidates.get(0)
        for i in range(1, candidates.size()):
            candidate = candidates.get(i)
            if candidate.getProcessingTime() < selected.getProcessingTime():
                selected = candidate

        return DispatchDecisionResponse.of(selected.getId())
```

| Java object | Methods |
| --- | --- |
| `DispatchDecisionRequest` | `getFabState()`, `getCandidates()`, `getSimulationTime()`, `getProjectedCycleTimeFactor()` |
| `FabStateSnapshot` | `getSimulationTime()`, `getToolGroups()`, `getCostSnapshot()` |
| `CostSnapshot` | `getTotalProjectedTardiness()`, `getWorkInProgress()` |
| `ToolGroupSnapshot` | `getName()`, `getWaitingForDispatch()`, `getTools()`, `getQueuedItems()`, `getInProcessItems()` |
| `ToolSnapshot` | `getId()`, `getCurrentToolState()` |
| `FlowItemQueuedSnapshot` | `getRemainingCycleTime()`, `getProcessingTime()`, `getExpectedSetupTime()`, `getTimeSinceArrival()`, `getPriority()`, `getLateness()`, `getRecipe()` |
| `FlowItemInProcessSnapshot` | `getRemainingCycleTime()`, `getProcessingTimeLeft()`, `getPriority()`, `getLateness()` |
| `FlowItemQueuedWithIDSnapshot` | `getId()` plus all `FlowItemQueuedSnapshot` methods |
| `DispatchDecisionResponse` | `of(selected_flow_item_id)`, `getSelectedFlowItemId()` |

`getCandidates()`, `getToolGroups()`, `getTools()`, `getQueuedItems()`, and `getInProcessItems()` return
Java lists. From Python, use `.size()` and `.get(index)` or JPype's Java collection iteration support.

## Run Result Methods

`MiniFab.run(...)` returns a single-run `RunResult`. External callers should orchestrate repeated runs
and aggregate statistics outside the Java benchmark.

| Method | Description |
| --- | --- |
| `getSimulationTimeHours()` | Simulation time in hours. |
| `getCompletedWafersPerDay()` | Completed wafers per day after warmup. |
| `getTardinessPerWaferMinutes()` | Tardiness per wafer in minutes after warmup. |
| `getCompletedWafers()` | Completed wafers after warmup. |
| `getTardyWafers()` | Tardy wafers after warmup. |
| `getFlowFactor()` | Flow factor after warmup. |

## Development

To develop a new fab environment, start with the benchmark implementations in
[`benchmarks/`](de.terministic.fabsimbenchmarks/src/main/java/de/terministic/fabsim/benchmarks/). 
A typical implementation defines products, routing, tool groups, dispatch behavior, statistics, and a run
method similar to `MiniFab`.

Build all modules and run tests:

```bash
mvn clean test
```

Build the shaded JPype/Docker jar after running tests:

```bash
mvn clean package
```

The shaded jar is written to:

- `de.terministic.fabsimbenchmarks/target/fabsim-benchmarks.jar`
