package moze_intel.projecte.api.nss;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link NSSTag} for representing {@link Fluid}s.
 */
public final class NSSFluid extends AbstractDataComponentHolderNSSTag<Fluid> {

	public static final MapCodec<NSSFluid> CODEC = createCodec(BuiltInRegistries.FLUID, false, NSSFluid::new);


	private NSSFluid(@NotNull ResourceLocation resourceLocation, boolean isTag, @NotNull DataComponentPatch componentsPatch) {
		super(resourceLocation, isTag, componentsPatch);
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a fluid from a {@link FluidStack}
	 */
	@NotNull
	public static NSSFluid createFluid(@NotNull FluidStack stack) {
		//Don't bother checking if it is empty as getFluid returns EMPTY which will then fail anyway for being empty
		return createFluid(stack.getFluid(), stack.getComponentsPatch());
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a fluid from a {@link Fluid}
	 */
	@NotNull
	public static NSSFluid createFluid(@NotNull Fluid fluid) {
		return createFluid(fluid, DataComponentPatch.EMPTY);
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a fluid from a {@link Fluid} and an optional {@link DataComponentPatch}
	 */
	@NotNull
	public static NSSFluid createFluid(@NotNull Fluid fluid, @NotNull DataComponentPatch componentsPatch) {
		if (fluid == Fluids.EMPTY) {
			throw new IllegalArgumentException("Can't make NSSFluid with an empty fluid");
		}
		Optional<ResourceKey<Fluid>> registryKey = BuiltInRegistries.FLUID.getResourceKey(fluid);
		if (registryKey.isEmpty()) {
			throw new IllegalArgumentException("Can't make an NSSFluid with an unregistered fluid");
		}
		//This should never be null, or it would have crashed on being registered
		return createFluid(registryKey.get().location(), componentsPatch);
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a fluid from a {@link Holder} with no data components.
	 */
	@NotNull
	public static NSSFluid createFluid(@NotNull Holder<Fluid> item) {
		return createFluid(item, DataComponentPatch.EMPTY);
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a fluid from a {@link Holder} and an optional {@link DataComponentPatch}
	 */
	@NotNull
	public static NSSFluid createFluid(@NotNull Holder<Fluid> fluid, @NotNull DataComponentPatch componentsPatch) {
		return createFluid(fluid.value(), componentsPatch);
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a fluid from a {@link ResourceLocation}
	 */
	@NotNull
	public static NSSFluid createFluid(@NotNull ResourceLocation fluidID) {
		return createFluid(fluidID, DataComponentPatch.EMPTY);
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a fluid from a {@link ResourceLocation} and an optional {@link DataComponentPatch}
	 */
	@NotNull
	public static NSSFluid createFluid(@NotNull ResourceLocation fluidID, @NotNull DataComponentPatch componentsPatch) {
		return new NSSFluid(fluidID, false, componentsPatch);
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a tag from a {@link ResourceLocation}
	 */
	@NotNull
	public static NSSFluid createTag(@NotNull ResourceLocation tagId) {
		return new NSSFluid(tagId, true, DataComponentPatch.EMPTY);
	}

	/**
	 * Helper method to create an {@link NSSFluid} representing a tag from a {@link TagKey<Fluid>}
	 */
	@NotNull
	public static NSSFluid createTag(@NotNull TagKey<Fluid> tag) {
		return createTag(tag.location());
	}

	@NotNull
	@Override
	protected Registry<Fluid> getRegistry() {
		return BuiltInRegistries.FLUID;
	}

	@Override
	protected NSSFluid createNew(Holder<Fluid> fluid) {
		return createFluid(fluid);
	}

	@Override
	public MapCodec<NSSFluid> codec() {
		return CODEC;
	}
}