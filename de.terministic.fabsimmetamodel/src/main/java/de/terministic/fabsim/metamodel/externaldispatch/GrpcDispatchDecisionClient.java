package de.terministic.fabsim.metamodel.externaldispatch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import de.terministic.fabsim.externaldispatch.grpc.DispatchDecisionRequest;
import de.terministic.fabsim.externaldispatch.grpc.DispatchDecisionResponse;
import de.terministic.fabsim.externaldispatch.grpc.DispatchDecisionServiceGrpc;

public class GrpcDispatchDecisionClient implements DispatchDecisionClient {

	private static final ConcurrentMap<String, ManagedChannel> CHANNELS = new ConcurrentHashMap<>();

	private final ExternalDispatchConfiguration configuration;

	public GrpcDispatchDecisionClient(final ExternalDispatchConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public long selectFlowItem(final DispatchDecisionSnapshot snapshot) {
		final String endpoint = this.configuration.getHost() + ":" + this.configuration.getPort();
		final ManagedChannel channel = CHANNELS.computeIfAbsent(endpoint,
				key -> ManagedChannelBuilder.forAddress(this.configuration.getHost(), this.configuration.getPort())
						.usePlaintext()
						.build());
		final DispatchDecisionServiceGrpc.DispatchDecisionServiceBlockingStub stub = DispatchDecisionServiceGrpc
				.newBlockingStub(channel)
				.withDeadlineAfter(this.configuration.getTimeoutMillis(), TimeUnit.MILLISECONDS);
		try {
			final DispatchDecisionRequest request = snapshot.toProto();
			final DispatchDecisionResponse response = stub.selectDispatchCandidate(request);
			return response.getSelectedFlowItemId();
		} catch (final StatusRuntimeException ex) {
			throw new ExternalDispatchException("External dispatch request failed for endpoint " + endpoint, ex);
		}
	}
}
