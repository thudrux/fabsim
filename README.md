# FabSim

Java-based factory simulator.

## MiniFab

### Build the container and run via Docker

Build the image from the repository root:

```bash
docker build -t minifab .
```

Run MiniFab in Docker when you want to use the predefined Java dispatch rules (`random`, `fifo`, `edd`, `cr`, `srpt`) and optionally write dispatch logs to a mounted host path.

```bash
docker run --rm \
  minifab \
  --simulation-time 168 \
  --warmup-time 24 \
  --dispatch-rule fifo
```

To run multiple simulations, pass `--runs <n>`:

```bash
docker run --rm \
  minifab \
  --simulation-time 168 \
  --warmup-time 24 \
  --runs 20 \
  --dispatch-rule fifo
```

To write the dispatch log to disk, mount a host directory and pass `--log-file`. Logging is only supported for a single run:

```bash
mkdir -p logs

docker run --rm \
  -v "$PWD/logs:/logs" \
  minifab \
  --simulation-time 168 \
  --warmup-time 24 \
  --runs 1 \
  --dispatch-rule fifo \
  --log-file /logs/minifab.jsonl
```

#### CLI reference

Required arguments:

- `--simulation-time <hours>`
- `--dispatch-rule random|fifo|edd|cr|srpt`

Optional:

- `--warmup-time <hours>`
- `--runs <n>`
- `--log-file <path>`

After the simulation finishes, the launcher prints either single-run metrics or aggregated mean/std summaries for:

- completed wafers per day
- tardiness per wafer in minutes
- completed wafers
- tardy wafers
- flow factor

These metrics exclude any time spent in the warmup window.

### Build the jar and run via JPype

This setup lets MiniFab delegate dispatch decisions to an external Python-based service through JPype. The Java simulation sends each dispatch request to Python, and Python returns the selected candidate.

Use Maven from the repository root to build the shaded jar:

```bash
mvn clean package -Dmaven.test.skip=true
```

Copy the resulting single jar into your Python project, for example into a `libs/` folder inside that project.

- `de.terministic.fabsimmetamodel/target/fabsim.jar`

This jar already bundles the project classes and the runtime dependencies needed by JPype.

The Java side exposes a `DispatchProvider` interface that Python implements through JPype. Java passes a `DispatchDecisionRequest` containing `fab_state` and `candidates`, and Python returns a `DispatchDecisionResponse` with the selected flow item id.

The request and response content matches the structures in [external_dispatch.proto](de.terministic.fabsimmetamodel/src/main/proto/external_dispatch.proto).

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
MiniFab = jpype.JClass("de.terministic.fabsim.metamodel.examples.MiniFab")
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

result = mini_fab.runMiniFabWithExternalDispatch(provider, simulation_time_hours, run_count, warmup_time_hours)
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
