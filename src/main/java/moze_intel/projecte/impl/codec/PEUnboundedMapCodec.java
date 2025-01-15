package moze_intel.projecte.impl.codec;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.RecordBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.codec.MapProcessor;

public record PEUnboundedMapCodec<KEY, VALUE>(MapCodec<KEY> keyCodec, MapCodec<VALUE> valueCodec, MapProcessor<KEY, VALUE> processor, boolean lenientKey)
		implements Codec<Map<KEY, VALUE>> {

	//TODO - 1.21: Do we want a check like this?
	/*public PEUnboundedMapCodec {
		if (keyCodec.keys(JsonOps.INSTANCE).containsAny(valueCodec.keys(JsonOps.INSTANCE))) {
			throw
		}
	}*/

	@Override
	public <T> DataResult<T> encode(final Map<KEY, VALUE> input, final DynamicOps<T> ops, final T prefix) {
		final ListBuilder<T> builder = ops.listBuilder();
		for (final Map.Entry<KEY, VALUE> entry : input.entrySet()) {
			RecordBuilder<T> encoded = keyCodec().encode(entry.getKey(), ops, ops.mapBuilder());
			encoded = valueCodec().encode(entry.getValue(), ops, encoded);
			builder.add(encoded.build(ops.emptyMap()));
		}
		return builder.build(prefix);
	}

	@Override
	public <T> DataResult<Pair<Map<KEY, VALUE>, T>> decode(final DynamicOps<T> ops, final T input) {
		return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
			final DecoderState<T> decoder = new DecoderState<>(ops);
			stream.accept(decoder::accept);
			return decoder.build();
		}).map(result -> Pair.of(result, input));
	}

	@Override
	public String toString() {
		if (lenientKey) {
			return "projecte:LenientKeyUnboundedMapCodec[" + keyCodec + " -> " + valueCodec + ']';
		}
		return "projecte:UnboundedMapCodec[" + keyCodec + " -> " + valueCodec + ']';
	}

	private class DecoderState<T> {

		private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

		private final DynamicOps<T> ops;
		//TODO - 1.21: Should this be a Object2ObjectArrayMap like Vanilla's BaseMapCodec uses?
		private final Map<KEY, VALUE> elements = new LinkedHashMap<>();
		private final Stream.Builder<T> failed = Stream.builder();
		private DataResult<Unit> result = INITIAL_RESULT;

		private DecoderState(final DynamicOps<T> ops) {
			this.ops = ops;
		}

		private void accept(final T input) {
			//TODO - 1.21: Re-evaluate comments, clean up actual references,
			// and then refer to https://gist.github.com/thiakil/7cadeb2a8e50aabc5056bc6574af0d90 as one of the references
			//Based on BaseMapCodec#decode and LenientUnboundedMapCodec
			DataResult<KEY> keyResult = keyCodec().decoder().parse(ops, input);
			//TODO - 1.21: Test that we implemented behavior for lenientKey properly
			if (lenientKey() && keyResult.isError()) {
				//Skip this key as it is invalid (potentially representing something unloaded)
				// Note: We log the error to help diagnose any issues
				//TODO - 1.21: Do we want to try and allow partial deserialization for example if it just has invalid components? (probably not)
				PECore.LOGGER.error("Unable to deserialize key: {}", keyResult.error().orElseThrow().message());
				return;
			}
			DataResult<VALUE> valueResult = valueCodec().decoder().parse(ops, input);
			DataResult<Map.Entry<KEY, VALUE>> entryResult = keyResult.apply2stable(Map::entry, valueResult);

			Optional<Map.Entry<KEY, VALUE>> resultOrPartial = entryResult.resultOrPartial();
			if (resultOrPartial.isPresent()) {
				Map.Entry<KEY, VALUE> entry = resultOrPartial.get();
				//TODO - 1.21: Error if the key already has a corresponding value (See if what we do below is correct, and add tests for it)
				VALUE existing = processor().addElement(elements, entry.getKey(), entry.getValue());
				if (existing != null) {
					failed.add(input);
					result = result.apply2stable((result, element) -> result, DataResult.error(() -> "Duplicate entry for key: '" + entry.getKey() + "'"));
					return;
				}
			}
			//TODO - 1.21: Do we want to include what the input was in the entry that failed? Might improve readability of the error message
			entryResult.ifError(error -> failed.add(input));
			result = result.apply2stable((result, element) -> result, entryResult);
		}

		public DataResult<Map<KEY, VALUE>> build() {
			final T errors = ops.createList(failed.build());
			final Map<KEY, VALUE> immutableElements = ImmutableMap.copyOf(elements);
			return result.map(ignored -> immutableElements)
					.setPartial(immutableElements)
					.mapError(e -> e + " missed input: " + errors);
		}
	}

}