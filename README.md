# FABSIM

Java factory simulator.

## MiniFab

### Build the container

Build the image from the repository root:

```bash
docker build -t minifab .
```

The build uses a multi-stage Dockerfile. It installs the `core` module first, then packages the `metamodel` module and copies the application jar plus its runtime dependencies into a slim runtime image.

### Run MiniFab in external dispatch mode

External mode requires the [gRPC](https://grpc.io)-based dispatch service host and port.

```bash
docker run --rm \
  minifab \
  --mode external \
  --simulation-time 168 \
  --dispatch-host dispatch-service.example.internal \
  --dispatch-port 50051 \
  --dispatch-timeout-ms 5000
```

If the dispatch service is reachable from the host machine rather than the Docker network, use the appropriate hostname for your environment, such as `host.docker.internal` on Docker Desktop.

> **NOTE**: The protocol buffer that needs to be implemented by an external dispatch service can be found in [external_dispatch.proto](de.terministic.fabsimmetamodel/src/main/proto/external_dispatch.proto).

### Run MiniFab in local dispatch mode

Local mode currently supports `fifo` only.

```bash
docker run --rm \
  minifab \
  --mode local \
  --simulation-time 168 \
  --dispatch-rule fifo
```

### Write logs to a mounted host path

An optional `--log-file` path can be passed in either mode. Mount a host directory into the container and point `--log-file` at a file inside that mount so the log survives container shutdown.

```bash
mkdir -p logs

docker run --rm \
  -v "$PWD/logs:/logs" \
  minifab \
  --mode local \
  --simulation-time 168 \
  --dispatch-rule fifo \
  --log-file /logs/minifab.jsonl
```

The terminal summary is printed after every run and does not depend on `--log-file`.

### CLI reference

Required arguments:

- `--mode external|local`
- `--simulation-time <hours>`

External mode arguments:

- `--dispatch-host <host>`
- `--dispatch-port <port>`
- `--dispatch-timeout-ms <ms>` optional, defaults to `5000`

Local mode arguments:

- `--dispatch-rule fifo`

Optional in either mode:

- `--log-file <path>`

After the simulation finishes, the launcher prints throughput and priority-weighted tardiness to the terminal.
