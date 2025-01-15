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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link NSSTag} for representing {@link Item}s.
 */
public final class NSSItem extends AbstractDataComponentHolderNSSTag<Item> {

	public static final MapCodec<NSSItem> CODEC = createCodec(BuiltInRegistries.ITEM, false, NSSItem::new);

	private NSSItem(@NotNull ResourceLocation resourceLocation, boolean isTag, @NotNull DataComponentPatch componentsPatch) {
		super(resourceLocation, isTag, componentsPatch);
	}

	/**
	 * Helper method to create an {@link NSSItem} representing an item from an {@link ItemStack}
	 */
	@NotNull
	public static NSSItem createItem(@NotNull ItemStack stack) {
		if (stack.isEmpty()) {
			throw new IllegalArgumentException("Can't make NSSItem with empty stack");
		}
		return createItem(stack.getItem(), stack.getComponentsPatch());
	}

	/**
	 * Helper method to create an {@link NSSItem} representing an item from an {@link ItemLike}
	 */
	@NotNull
	public static NSSItem createItem(@NotNull ItemLike itemProvider) {
		return createItem(itemProvider, DataComponentPatch.EMPTY);
	}

	/**
	 * Helper method to create an {@link NSSItem} representing an item from a {@link Holder} and an optional {@link DataComponentPatch}
	 */
	@NotNull
	public static NSSItem createItem(@NotNull Holder<Item> item, @NotNull DataComponentPatch componentsPatch) {
		return createItem(item.value(), componentsPatch);
	}

	/**
	 * Helper method to create an {@link NSSItem} representing an item from an {@link ItemLike} and an optional {@link DataComponentPatch}
	 */
	@NotNull
	public static NSSItem createItem(@NotNull ItemLike itemProvider, @NotNull DataComponentPatch componentsPatch) {
		Item item = itemProvider.asItem();
		if (item == Items.AIR) {
			throw new IllegalArgumentException("Can't make NSSItem with empty stack");
		}
		Optional<ResourceKey<Item>> registryKey = BuiltInRegistries.ITEM.getResourceKey(item);
		if (registryKey.isEmpty()) {
			throw new IllegalArgumentException("Can't make an NSSItem with an unregistered item");
		}
		//This should never be null, or it would have crashed on being registered
		return createItem(registryKey.get().location(), componentsPatch);
	}

	/**
	 * Helper method to create an {@link NSSItem} representing an item from a {@link ResourceLocation}
	 */
	@NotNull
	public static NSSItem createItem(@NotNull ResourceLocation itemID) {
		return createItem(itemID, DataComponentPatch.EMPTY);
	}

	/**
	 * Helper method to create an {@link NSSItem} representing an item from a {@link ResourceKey}
	 */
	@NotNull
	public static NSSItem createItem(@NotNull ResourceKey<Item> itemID) {
		return createItem(itemID.location());
	}

	/**
	 * Helper method to create an {@link NSSItem} representing an item from a {@link ResourceLocation} and an optional {@link DataComponentPatch}
	 */
	@NotNull
	public static NSSItem createItem(@NotNull ResourceLocation itemID, @NotNull DataComponentPatch componentsPatch) {
		return new NSSItem(itemID, false, componentsPatch);
	}

	/**
	 * Helper method to create an {@link NSSItem} representing a tag from a {@link ResourceLocation}
	 */
	@NotNull
	public static NSSItem createTag(@NotNull ResourceLocation tagId) {
		return new NSSItem(tagId, true, DataComponentPatch.EMPTY);
	}

	/**
	 * Helper method to create an {@link NSSItem} representing a tag from a {@link TagKey<Item>}
	 */
	@NotNull
	public static NSSItem createTag(@NotNull TagKey<Item> tag) {
		return createTag(tag.location());
	}

	@NotNull
	@Override
	protected Registry<Item> getRegistry() {
		return BuiltInRegistries.ITEM;
	}

	@Override
	protected NSSItem createNew(Item item) {
		return createItem(item);
	}

	@Override
	public MapCodec<NSSItem> codec() {
		return CODEC;
	}
}