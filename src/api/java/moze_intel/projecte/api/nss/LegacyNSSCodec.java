package moze_intel.projecte.api.nss;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import moze_intel.projecte.api.nss.LegacyNSSCodec.NameComponent;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record LegacyNSSCodec<TYPE>(Registry<TYPE> registry, boolean allowDefault, Codec<String> baseCodec
								   //TODO - 1.21: Can we make these be used instead of the registry + allow default thing we have going on?
								   //, Codec<ResourceLocation> nameCodec, Codec<DataComponentType<?>> typeCodec
) implements Codec<Either<ResourceLocation, NameComponent>> {

	//TODO - 1.21: Better name
	public record NameComponent(ResourceLocation name, DataComponentPatch patch) {
	}

	private DataResult<ResourceLocation> validate(ResourceLocation id) {
		//Like Vanilla's Registry#referenceHolderWithLifecycle
		if (!registry.containsKey(id)) {
			return DataResult.error(() -> "Registry " + registry.key().location() + " does not contain element " + id);
		} else if (!allowDefault && registry instanceof DefaultedRegistry<?> defaultedRegistry && id.equals(defaultedRegistry.getDefaultKey())) {
			return DataResult.error(() -> "Default element " + id + " in registry " + registry.key().location() + " is not valid");
		}
		return DataResult.success(id);
	}

	@Override
	public <T> DataResult<Pair<Either<ResourceLocation, NameComponent>, T>> decode(DynamicOps<T> ops, T input) {
		return baseCodec.decode(ops, input).flatMap(p -> {
			String name = p.getFirst();
			if (name.startsWith("#")) {
				return ResourceLocation.read(name.substring(1))
						.map(r -> Pair.of(Either.left(r), p.getSecond()));
			}
			DataResult<DataComponentPatch> componentResult;
			int componentStart = name.indexOf('[');
			if (componentStart == -1) {//If there is no component, just use the empty one
				componentResult = DataResult.success(DataComponentPatch.EMPTY);
			} else {
				String rawComponent = name.substring(componentStart);
				//Update name to not contain the component so that we can read it cleanly below
				name = name.substring(0, componentStart);
				DataResult<Pair<DataComponentPatch, T>> components = new ComponentDecoderState<>(ops, rawComponent).build();
				if (components.isError()) {//TODO - 1.21: Does this work
					return DataResult.error(((DataResult.Error<?>) components).messageSupplier());
				}
				componentResult = components.map(Pair::getFirst);
			}
			//TODO - 1.21: Allow for a partial result when the name is fine but the component isn't
			// and then allow potentially trying to consume it?
			return ResourceLocation.read(name).flatMap(this::validate)
					.apply2(NameComponent::new, componentResult)
					.map(r -> Pair.of(Either.right(r), p.getSecond()));
		});
	}

	@Override
	public <T> DataResult<T> encode(Either<ResourceLocation, NameComponent> input, DynamicOps<T> ops, T prefix) {
		String stringRepresentation = input.map(
				left -> "#" + left,//Tag
				right -> right.name().toString() + convertComponentPatchToString(right.patch())
		);
		return baseCodec.encode(stringRepresentation, ops, prefix);
	}

	public static String convertComponentPatchToString(@Nullable DataComponentPatch patch) {
		if (patch == null || patch.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder("[");
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
			DataResult<String> result = appendComponentData(entry.getKey(), entry.getValue());
			//TODO - 1.21: Handle error
			result.ifSuccess(builder::append);
		}
		return builder.append("]")
				.toString();
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static <COMPONENT> DataResult<String> appendComponentData(DataComponentType<COMPONENT> componentType, Optional<?> value) {
		Codec<COMPONENT> componentCodec = componentType.codec();
		if (componentCodec == null) {
			return DataResult.error(() -> componentType + " is not a persistent component");
		}
		ResourceLocation typeName = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
		if (typeName == null) {
			return DataResult.error(() -> componentType + " is not registered");
		}
		if (value.isEmpty()) {
			return DataResult.success("!" + typeName);
		} else {
			//TODO - 1.21: Does this have access to registries
			return componentCodec.encodeStart(NbtOps.INSTANCE, (COMPONENT) value.get())
					.map(tag -> typeName + "=" + tag);
		}
	}

	@Override
	public String toString() {
		return "projecte:legacy_nss_codec";
	}

	private static class ComponentDecoderState<T> {

		private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

		private final DynamicOps<T> ops;
		private final String rawComponent;
		private final DataComponentPatch.Builder builder = DataComponentPatch.builder();
		//private final Stream.Builder<T> failed = Stream.builder();
		private DataResult<Unit> result = INITIAL_RESULT;

		private ComponentDecoderState(DynamicOps<T> ops, String rawComponent) {
			this.ops = ops;
			this.rawComponent = rawComponent;
		}

		private boolean missingExpected(StringReader reader, char c) {
			if (!reader.canRead() || reader.peek() != c) {
				return true;
			}
			reader.skip();
			return false;
		}

		//[VanillaCopy] Based initially off of ItemParser$State
		public DataResult<Pair<DataComponentPatch, T>> build() {
			StringReader reader = new StringReader(rawComponent);
			if (missingExpected(reader, '[')) {
				return DataResult.error(() -> "Expected component to start with a '['");
			}
			Set<DataComponentType<?>> knownTypes = new ReferenceArraySet<>();

			while (reader.canRead() && reader.peek() != ']') {
				reader.skipWhitespace();
				boolean isRemoval = false;
				if (reader.canRead() && reader.peek() == '!') {
					reader.skip();
					isRemoval = true;
				}

				//READ COMPONENT TYPE
				if (!reader.canRead()) {
					return DataResult.error(() -> "Expected data component");
				}
				//TODO - 1.21: ? Codec<DataComponentType<?>> typeCodec = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec();
				ResourceLocation componentName;
				try {
					componentName = ResourceLocation.read(reader);
				} catch (CommandSyntaxException e) {
					return DataResult.error(e::getMessage);
				}
				DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(componentName);
				if (componentType == null || componentType.isTransient()) {
					return DataResult.error(() -> "Unknown data component '" + componentName + "'");
				} else if (componentType.isTransient()) {
					return DataResult.error(() -> componentType + " is not a persistent component");
				} else if (!knownTypes.add(componentType)) {
					return DataResult.error(() -> "Data component '" + componentType + "' was repeated, but only one value can be specified");
				}
				//END READ COMPONENT TYPE

				reader.skipWhitespace();
				if (isRemoval) {
					this.builder.remove(componentType);
				} else {
					if (missingExpected(reader, '=')) {
						//TODO - 1.21: Better error message
						return DataResult.error(() -> "Expected component to SOMETHING with a '='");
					}
					reader.skipWhitespace();
					DataResult<? extends Pair<?, T>> componentResult = readComponent(reader, componentType);
					if (componentResult.isError()) {//TODO - 1.21: Does this work
						return componentResult.map(r -> Pair.of(DataComponentPatch.EMPTY, r.getSecond()));
					}
					reader.skipWhitespace();
				}

				if (!reader.canRead() || reader.peek() != ',') {
					break;
				}

				reader.skip();
				reader.skipWhitespace();
				if (!reader.canRead()) {
					return DataResult.error(() -> "Expected data component");
				}
			}
			if (missingExpected(reader, ']')) {
				return DataResult.error(() -> "Expected component to end with a ']'");
			}

			//TODO - 1.21: Make this support partial results and allow skipping invalid components?
			//final T errors = ops.createList(failed.build());
			//final Pair<DataComponentPatch, T> pair = Pair.of(this.builder.build(), errors);
			final Pair<DataComponentPatch, T> pair = Pair.of(this.builder.build(), ops.empty());
			return result.map(ignored -> pair).setPartial(pair);
		}

		private <COMPONENT> DataResult<Pair<COMPONENT, T>> readComponent(StringReader reader, DataComponentType<COMPONENT> componentType) {
			//int i = reader.getCursor();
			Tag tag;
			try {
				tag = new TagParser(reader).readValue();
			} catch (CommandSyntaxException e) {
				return DataResult.error(e::getMessage);
			}
			//DataResult<Pair<COMPONENT, T>> dataResult = componentCodec.decode(ItemParser.this.registryOps, tag);
			//TODO - 1.21: Re-evaluate if this is even correct for how do decode it and if we need to somehow apply the registry ops.
			// I believe ops may already have registry access
			//Note: We use codecOrThrow as we validate that there is a codec before calling this method
			DataResult<Pair<COMPONENT, T>> dataResult = componentType.codecOrThrow().decode(ops, NbtOps.INSTANCE.convertTo(ops, tag));

			/*dataResult.error().ifPresent(error -> {
				failed.add(value);
				//TODO - 1.21: Re-evaluate if we even care about doing this
				reader.setCursor(i);
				//"Malformed '%s' component: '%s'"
				//return ItemParser.ERROR_MALFORMED_COMPONENT.createWithContext(reader, componentType.toString(), error);
			});*/
			dataResult.resultOrPartial().ifPresent(pair -> builder.set(componentType, pair.getFirst()));
			this.result = this.result.apply2stable((result, element) -> result, dataResult);

			return dataResult;
		}
	}
}