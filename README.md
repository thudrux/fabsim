<h1 align="center">fabsim</h1>

<p align="center"><i>A Java-based simulator for semiconductor fabrication facilities.</i></p>

<p align="center">
  <a href="#repository-structure"><b>Repository Structure</b></a> ·
  <a href="#supported-benchmarks"><b>Supported Benchmarks</b></a> ·
  <a href="#installation-and-usage"><b>Installation & Usage</b></a> ·
  <a href="#data-format"><b>Data Format</b></a> ·
  <a href="#development"><b>Development</b></a>
</p>

---

`fabsim` is a Java-based discrete-event simulator for modelling semiconductor fabrication facilities.
It provides reusable simulation primitives for fab environments and includes out-of-the-box benchmark
implementations such as MiniFab. For data generation and baseline experiments, fabsim can run predefined
dispatching rules directly in Docker. For custom dispatching logic, reinforcement learning, or policy
evaluation, fabsim can delegate dispatch decisions to a Python service through JPype.

## Repository Structure

The repository is organized as a multi-module Maven project:

- [`de.terministic.fabsimcore`](de.terministic.fabsimcore/): core discrete-event simulation infrastructure,
  including events, event-list managers, simulation end conditions, duration distributions, and base
  simulation interfaces.
- [`de.terministic.fabsimmetamodel`](de.terministic.fabsimmetamodel/): fab-specific metamodel built on top
  of the core simulator, including components for lots, wafers, tools, routing, setup, batching,
  dispatching rules, statistics, external dispatch integration, and benchmark examples.

## Supported Benchmarks

Benchmark implementations are located in
[`examples/`](de.terministic.fabsimmetamodel/src/main/java/de/terministic/fabsim/metamodel/examples/).

Currently supported:

- MiniFab: an implementation of the Intel five-machine, six-step benchmark used in
  Ingy A. El-Khouly, Khaled S. El-Kilany, and Aziz E. El-Sayed,
  ["Modelling and simulation of re-entrant flow shop scheduling: An application in semiconductor manufacturing"](https://doi.org/10.1109/ICCIE.2009.5223754),
  2009 International Conference on Computers & Industrial Engineering, pp. 211-216.

## Installation and Usage

fabsim supports two common execution modes:

- Docker execution with predefined local dispatch rules (`random`, `fifo`, `edd`, `cr`, `srpt`), useful for
  baseline simulations and dispatch-log data generation.
- JPype execution from Python, useful when dispatch decisions should come from an external Python policy,
  such as a reinforcement-learning agent or a custom heuristic.

### Build the container and run via Docker

Build the image from the repository root:

```bash
docker build -t fabsim .
```

Run fabsim in Docker when you want to use predefined dispatch rules and optionally write dispatch logs to
a mounted host path.

The Docker launcher requires selecting a fab implementation with `--fab`; `minifab` is currently the only
available implementation.

```bash
docker run --rm \
  fabsim \
  --fab minifab \
  --simulation-time 168 \
  --warmup-time 24 \
  --dispatch-rule fifo
```

To run multiple simulations, pass `--runs <n>`:

```bash
docker run --rm \
  fabsim \
  --fab minifab \
  --simulation-time 168 \
  --warmup-time 24 \
  --runs 20 \
  --dispatch-rule fifo
```

To write the dispatch log to disk in the format described in [Data Format](#data-format), mount a host
directory and pass `--log-file`.
Logging is only supported for a single run:

```bash
mkdir -p logs

docker run --rm \
  -v "$PWD/logs:/logs" \
  fabsim \
  --fab minifab \
  --simulation-time 168 \
  --warmup-time 24 \
  --runs 1 \
  --dispatch-rule fifo \
  --log-file /logs/minifab.jsonl
```

#### CLI reference

Required arguments:

- `--fab minifab`
- `--simulation-time <hours>`
- `--dispatch-rule random|fifo|edd|cr|srpt`

Optional arguments:

- `--warmup-time <hours>`: warmup period excluded from reported metrics.
- `--runs <n>`: number of independent simulation runs. Defaults to `1`.
- `--log-file <path>`: JSONL dispatch-log output path. Only supported when `--runs 1`.
- `--help`, `-h`: print CLI usage.

After the simulation finishes, the launcher prints either single-run metrics or aggregated mean/std
summaries for:

- completed wafers per day
- tardiness per wafer in minutes
- completed wafers
- tardy wafers
- flow factor

These metrics exclude any time spent in the warmup window.

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

- `de.terministic.fabsimmetamodel/target/fabsim.jar`

This jar bundles the project classes and runtime dependencies needed by JPype.

The Java side exposes a `DispatchProvider` interface that Python implements through JPype. Java passes a
`DispatchDecisionRequest` containing `fab_state` and `candidates`, and Python returns a
`DispatchDecisionResponse` with the selected flow item id. The request and response structures
are described in [Data Format](#data-format).

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
        str(jar_dir / "fabsim.jar"),
    ],
)

DispatchProvider = jpype.JClass("de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider")
MiniFab = jpype.JClass("de.terministic.fabsim.metamodel.examples.minifab.MiniFab")
DispatchDecisionResponse = jpype.JClass("de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionResponse")

@JImplements(DispatchProvider)
class Provider:
    @JOverride
    def selectDispatchCandidate(self, request):
        candidates = request.getCandidates()
        selected = candidates.get(0).getId()
        return DispatchDecisionResponse.of(selected)

provider = Provider()
mini_fab = MiniFab()
simulation_time_hours = 168
warmup_time_hours = 24
run_count = 100

result = mini_fab.runMiniFabWithExternalDispatch(
    provider,
    simulation_time_hours,
    run_count,
    warmup_time_hours,
)

completed_wafers_per_day_mean = result.getCompletedWafersPerDayMean()
completed_wafers_per_day_std = result.getCompletedWafersPerDayStdDev()
tardiness_per_wafer_mean = result.getTardinessPerWaferMinutesMean()
tardiness_per_wafer_std = result.getTardinessPerWaferMinutesStdDev()
completed_wafers_mean = result.getCompletedWafersMean()
completed_wafers_std = result.getCompletedWafersStdDev()
tardy_wafers_mean = result.getTardyWafersMean()
tardy_wafers_std = result.getTardyWafersStdDev()
flow_factor_mean = result.getFlowFactorMean()
flow_factor_std = result.getFlowFactorStdDev()
```

## Data Format

The interface between fabsim and an external dispatch service is defined in
[`external_dispatch.proto`](de.terministic.fabsimmetamodel/src/main/proto/external_dispatch.proto). The same
schema is also used for local JSONL dispatch logs. Each logged line represents one dispatch decision.

*NOTE*: All time values are expressed in milliseconds unless stated otherwise.

| Field | Type | Description |
| --- | --- | --- |
| `DispatchDecisionRequest.fab_state` | `FabStateSnapshot` | Current fab state at the dispatch decision point. |
| `DispatchDecisionRequest.candidates` | `FlowItemQueuedWithIDSnapshot[]` | Candidate queued flow items from which the dispatch rule must choose exactly one. |
| `DispatchDecisionResponse.selected_flow_item_id` | `int64` | ID of the selected candidate flow item. |
| `LocalDispatchLog.dispatch_decisions` | `DispatchDecisionLogEntry[]` | Collection of recorded dispatch decisions. In JSONL output, each line stores one decision entry. |
| `DispatchDecisionLogEntry.fab_state` | `FabStateSnapshot` | Fab state observed before the logged dispatch decision. |
| `DispatchDecisionLogEntry.dispatch_decision` | `DispatchDecisionSnapshot` | Flow item selected by the dispatch rule. |
| `DispatchDecisionSnapshot.chosen_flow_item` | `FlowItemQueuedSnapshot` | Snapshot of the selected queued item. |
| `FabStateSnapshot.simulation_time` | `int64` | Current simulation timestamp. |
| `FabStateSnapshot.tool_groups` | `ToolGroupSnapshot[]` | State of all tool groups in the fab. |
| `FabStateSnapshot.cost_snapshot` | `CostSnapshot` | Aggregate cost-related state for the current decision point. |
| `CostSnapshot.total_projected_tardiness` | `int64` | Projected total tardiness over work currently visible to the snapshot. |
| `CostSnapshot.work_in_progress` | `int64` | Current work in progress. |
| `ToolGroupSnapshot.name` | `string` | Tool-group name. |
| `ToolGroupSnapshot.waiting_for_dispatch` | `bool` | Whether the tool group is currently requesting a dispatch decision. |
| `ToolGroupSnapshot.tools` | `ToolSnapshot[]` | Tools belonging to the tool group. |
| `ToolGroupSnapshot.queued_items` | `FlowItemQueuedSnapshot[]` | Items waiting in the tool-group queue. |
| `ToolGroupSnapshot.in_process_items` | `FlowItemInProcessSnapshot[]` | Items currently being processed by the tool group. |
| `ToolSnapshot.id` | `int64` | Tool identifier. |
| `ToolSnapshot.current_tool_state` | `string` | Current tool state, such as standby, setup, processing, maintenance, or breakdown. |
| `FlowItemQueuedSnapshot.remaining_cycle_time` | `int64` | Remaining planned cycle time for a queued item. |
| `FlowItemQueuedSnapshot.processing_time` | `int64` | Processing time required at the current operation. |
| `FlowItemQueuedSnapshot.expected_setup_time` | `int64` | Expected setup time before processing can start. |
| `FlowItemQueuedSnapshot.time_since_arrival` | `int64` | Time since the item arrived in the current queue. |
| `FlowItemQueuedSnapshot.priority` | `int32` | Item priority. |
| `FlowItemQueuedSnapshot.lateness` | `int64` | Current lateness relative to the item's due-date target. |
| `FlowItemQueuedSnapshot.recipe` | `string` | Recipe required for the current operation. |
| `FlowItemInProcessSnapshot.remaining_cycle_time` | `int64` | Remaining planned cycle time for an item in process. |
| `FlowItemInProcessSnapshot.processing_time_left` | `int64` | Remaining processing time on the current tool. |
| `FlowItemInProcessSnapshot.priority` | `int32` | Item priority. |
| `FlowItemInProcessSnapshot.lateness` | `int64` | Current lateness relative to the item's due-date target. |
| `FlowItemQueuedWithIDSnapshot.id` | `int64` | Candidate flow item identifier used by `selected_flow_item_id`. |
| `FlowItemQueuedWithIDSnapshot.*` | mixed | Same queued-item fields as `FlowItemQueuedSnapshot`, plus the candidate ID. |

## Development

To develop a new fab environment, start with the benchmark implementations in
[`examples/`](de.terministic.fabsimmetamodel/src/main/java/de/terministic/fabsim/metamodel/examples/). A
typical implementation defines products, routing, tool groups, dispatch behavior, statistics, and a run
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

- `de.terministic.fabsimmetamodel/target/fabsim.jar`
