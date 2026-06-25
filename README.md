# FABSIM

Java factory simulator.

## MiniFab

### Build the container and run via Docker

Build the image from the repository root:

```bash
docker build -t minifab .
```

Run MiniFab in Docker when you want to use the predefined Java dispatch rules (`fifo`, `edd`, `srpt`) and optionally write dispatch logs to a mounted host path.

```bash
docker run --rm \
  minifab \
  --simulation-time 168 \
  --dispatch-rule fifo
```

To write the dispatch log to disk, mount a host directory and pass `--log-file`:

```bash
mkdir -p logs

docker run --rm \
  -v "$PWD/logs:/logs" \
  minifab \
  --simulation-time 168 \
  --dispatch-rule fifo \
  --log-file /logs/minifab.jsonl
```

#### CLI reference

Required arguments:

- `--simulation-time <hours>`
- `--dispatch-rule fifo|edd|srpt`

Optional:

- `--log-file <path>`

After the simulation finishes, the launcher prints throughput and priority-weighted tardiness to the terminal.

### Build the JAR and Run via JPype

Use Maven from the repository root:

```bash
mvn -f de.terministic.fabsimcore/pom.xml install
mvn -f de.terministic.fabsimmetamodel/pom.xml package
```

The resulting application jar is created in `de.terministic.fabsimmetamodel/target/`.

The Java side exposes a `DispatchProvider` interface that Python implements through JPype. Java passes a `DispatchDecisionRequest` containing `fab_state` and `candidates`, and Python returns a `DispatchDecisionResponse` with the selected flow item id.

The request and response content matches the structures in [external_dispatch.proto](de.terministic.fabsimmetamodel/src/main/proto/external_dispatch.proto).

Example flow:

```python
import jpype
from jpype import JImplements, JOverride

jpype.startJVM(classpath=["de.terministic.fabsimmetamodel/target/metamodel-0.0.1-SNAPSHOT.jar"])

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
result = mini_fab.runMiniFabWithExternalDispatch(provider, 168)
throughput = result.getThroughput()
twt = result.getTotalWeightedTardiness()
```
