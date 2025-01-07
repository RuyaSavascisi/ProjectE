package moze_intel.projecte.api.nss;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.nss.LegacyNSSCodec.NameComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract implementation to make implementing {@link NSSTag} and {@link NSSDataComponentHolder} simpler, and automatically be able to register conversions for:
 * <p>
 * - Tag -> Type
 * <p>
 * - Type -> Tag
 *
 * @param <TYPE> The type of the tag this {@link NormalizedSimpleStack} is for.
 *
 * @implNote This does not handle data components on Tags.
 */
public abstract class AbstractDataComponentHolderNSSTag<TYPE> extends AbstractNSSTag<TYPE> implements NSSDataComponentHolder {

	@NotNull
	private final DataComponentPatch componentsPatch;

	protected AbstractDataComponentHolderNSSTag(@NotNull ResourceLocation resourceLocation, boolean isTag, @NotNull DataComponentPatch componentsPatch) {
		super(resourceLocation, isTag);
		this.componentsPatch = Objects.requireNonNull(componentsPatch, "Components patch must not be null");
	}

	@NotNull
	@Override
	public DataComponentPatch getComponentsPatch() {
		return componentsPatch;
	}

	@Override
	public String toString() {
		return super.toString() + LegacyNSSCodec.convertComponentPatchToString(componentsPatch);
	}

	@Override
	public boolean equals(Object o) {
		//Note: This needs to use Objects.equals instead of equals so that it doesn't throw an exception in super when adding to the set of created tags
		return o == this || super.equals(o) && Objects.equals(componentsPatch, ((AbstractDataComponentHolderNSSTag<?>) o).componentsPatch);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), componentsPatch);
	}

	/**
	 * Creates an explicit codec capable of reading and writing this {@link NormalizedSimpleStack}.
	 *
	 * @param registry       Registry that backs this codec.
	 * @param allowDefault   {@code true} to allow ids matching the default element of the registry.
	 * @param nssConstructor Normalized Simple Stack constructor.
	 */
	protected static <TYPE, NSS extends AbstractDataComponentHolderNSSTag<TYPE>> MapCodec<NSS> createExplicitCodec(Registry<TYPE> registry, boolean allowDefault,
			DataComponentHolderNSSConstructor<TYPE, NSS> nssConstructor) {
		//Note: We return a MapCodec so that dispatch codecs can inline this
		return NeoForgeExtraCodecs.withAlternative(
				createExplicitTagCodec(nssConstructor),
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						idComponent(registry, allowDefault),
						DataComponentPatch.CODEC.optionalFieldOf("data", DataComponentPatch.EMPTY).forGetter(AbstractDataComponentHolderNSSTag::getComponentsPatch)
				).apply(instance, nssConstructor::create))
		);
	}

	/**
	 * Creates a legacy codec capable of reading and writing this {@link NormalizedSimpleStack} to/from strings.
	 *
	 * @param registry       Registry that backs this codec.
	 * @param allowDefault   {@code true} to allow ids matching the default element of the registry.
	 * @param prefix         A string representing the prefix to use for serialization. Must end with '|' to properly work. Anything without a '|' is assumed to be an
	 *                       item.
	 * @param nssConstructor Normalized Simple Stack constructor.
	 */
	protected static <TYPE, NSS extends AbstractDataComponentHolderNSSTag<TYPE>> Codec<NSS> createLegacyCodec(Registry<TYPE> registry, boolean allowDefault, String prefix,
			DataComponentHolderNSSConstructor<TYPE, NSS> nssConstructor) {
		return createLegacyCodec(registry, allowDefault, IPECodecHelper.INSTANCE.withPrefix(prefix), nssConstructor);
	}

	/**
	 * Creates a legacy codec capable of reading and writing this {@link NormalizedSimpleStack} to/from strings.
	 *
	 * @param registry       Registry that backs this codec.
	 * @param allowDefault   {@code true} to allow ids matching the default element of the registry.
	 * @param baseCodec      String codec that processes any related prefix requirements.
	 * @param nssConstructor Normalized Simple Stack constructor.
	 */
	static <TYPE, NSS extends AbstractDataComponentHolderNSSTag<TYPE>> Codec<NSS> createLegacyCodec(Registry<TYPE> registry, boolean allowDefault,
			Codec<String> baseCodec, DataComponentHolderNSSConstructor<TYPE, NSS> nssConstructor) {
		return new LegacyNSSCodec<>(registry, allowDefault, baseCodec).xmap(
				data -> data.map(nssConstructor::createTag, right -> nssConstructor.create(right.name(), right.patch())),
				nss -> nss.representsTag() ? Either.left(nss.getResourceLocation()) : Either.right(new NameComponent(nss.getResourceLocation(), nss.getComponentsPatch()))
		);
	}

	/**
	 * Represents a constructor of an {@link AbstractDataComponentHolderNSSTag}.
	 */
	@FunctionalInterface
	protected interface DataComponentHolderNSSConstructor<TYPE, NSS extends AbstractDataComponentHolderNSSTag<TYPE>> extends NSSTagConstructor<TYPE, NSS> {

		NSS create(ResourceLocation rl, boolean isTag, @NotNull DataComponentPatch componentsPatch);

		@Override
		default NSS create(ResourceLocation rl, boolean isTag) {
			return create(rl, isTag, DataComponentPatch.EMPTY);
		}

		default NSS create(ResourceLocation rl, @NotNull DataComponentPatch componentsPatch) {
			return create(rl, false, componentsPatch);
		}
	}
}