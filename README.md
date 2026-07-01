# FabSim

Java-based factory simulator.

## MiniFab

### Build the container and run via Docker

Build the image from the repository root:

```bash
docker build -t minifab .
```

Run MiniFab in Docker when you want to use the predefined Java dispatch rules (`fifo`, `edd`, `srpt`, `random`) and optionally write dispatch logs to a mounted host path.

```bash
docker run --rm \
  minifab \
  --simulation-time 168 \
  --dispatch-rule fifo
```

To run multiple simulations, pass `--runs <n>`:

```bash
docker run --rm \
  minifab \
  --simulation-time 168 \
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
  --runs 1 \
  --dispatch-rule fifo \
  --log-file /logs/minifab.jsonl
```

#### CLI reference

Required arguments:

- `--simulation-time <hours>`
- `--dispatch-rule fifo|edd|srpt|random`

Optional:

- `--runs <n>`
- `--log-file <path>`

After the simulation finishes, the launcher prints either single-run metrics or aggregated mean/std summaries depending on `--runs`.

### Build the jar and run via JPype

This setup lets MiniFab delegate dispatch decisions to an external Python-based service through JPype. The Java simulation sends each dispatch request to Python, and Python returns the selected candidate.

Use Maven from the repository root to build the jars:

```bash
mvn -f de.terministic.fabsimcore/pom.xml install
mvn -f de.terministic.fabsimmetamodel/pom.xml package
```

Copy the following jars into your Python project, for example into a `jars/` folder inside that project.

*Jars from inside the respective `target` directories*:

- `de.terministic.fabsimcore/target/core-0.0.1-SNAPSHOT.jar`
- `de.terministic.fabsimmetamodel/target/metamodel-0.0.1-SNAPSHOT.jar`

*Jars from inside your local Maven `.m2` directory*:

- `~/.m2/repository/org/slf4j/slf4j-api/1.7.32/slf4j-api-1.7.32.jar`
- `~/.m2/repository/org/apache/logging/log4j/log4j-api/2.22.1/log4j-api-2.22.1.jar`
- `~/.m2/repository/org/apache/logging/log4j/log4j-core/2.22.1/log4j-core-2.22.1.jar`
- `~/.m2/repository/org/apache/logging/log4j/log4j-slf4j-impl/2.22.1/log4j-slf4j-impl-2.22.1.jar`

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
        str(jar_dir / "core-0.0.1-SNAPSHOT.jar"),
        str(jar_dir / "metamodel-0.0.1-SNAPSHOT.jar"),
        str(jar_dir / "slf4j-api-1.7.32.jar"),
        str(jar_dir / "log4j-api-2.22.1.jar"),
        str(jar_dir / "log4j-core-2.22.1.jar"),
        str(jar_dir / "log4j-slf4j-impl-2.22.1.jar"),
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
run_count = 100

result = mini_fab.runMiniFabWithExternalDispatch(provider, simulation_time_hours, run_count)
throughput_mean = result.getThroughputMean()
throughput_std = result.getThroughputStdDev()
twt_mean = result.getTotalWeightedTardinessMean()
twt_std = result.getTotalWeightedTardinessStdDev()
```
