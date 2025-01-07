package moze_intel.projecte.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.nss.NSSItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used for keeping track of a combined {@link Item} and {@link DataComponentPatch}. Unlike {@link ItemStack} this class does not keep track of count, and overrides
 * {@link #equals(Object)} and {@link #hashCode()} so that it can be used properly in a {@link java.util.Set}.
 *
 * @implNote If the {@link DataComponentPatch} this {@link ItemInfo} is given is empty, then it converts it to being null.
 * @apiNote {@link ItemInfo} and the data it stores is Immutable
 */
public final class ItemInfo {

	/**
	 * Codec for encoding ItemInfo to and from strings.
	 */
	public static final Codec<ItemInfo> LEGACY_CODEC = IPECodecHelper.INSTANCE.validatePresent(
			NSSItem.LEGACY_CODEC.xmap(ItemInfo::fromNSS, itemInfo -> NSSItem.createItem(itemInfo.getItem(), itemInfo.getComponentsPatch())),
			() -> "ItemInfo does not support tags or missing items"
	);

	public static final Codec<ItemInfo> EXPLICIT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemInfo::getItem),
			DataComponentPatch.CODEC.optionalFieldOf("data", DataComponentPatch.EMPTY).forGetter(ItemInfo::getComponentsPatch)
	).apply(instance, ItemInfo::new));

	/**
	 * Stream codec for encoding ItemInfo across the network.
	 */
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemInfo> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.ITEM), ItemInfo::getItem,
			DataComponentPatch.STREAM_CODEC, ItemInfo::getComponentsPatch,
			ItemInfo::new
	);

	@NotNull
	private final Holder<Item> item;
	@NotNull
	private final DataComponentPatch componentsPatch;

	//TODO - 1.21: Should this use the components patch or the components map of the stack???
	private ItemInfo(@NotNull Holder<Item> item, @NotNull DataComponentPatch componentsPatch) {
		//TODO - 1.21: Do we want to throw if holder instanceof Holder.Direct as is(ResourceKey) and stuff returns bad values for that
		this.item = item;
		this.componentsPatch = componentsPatch;
	}

	/**
	 * Creates an {@link ItemInfo} object from a given {@link Item} with an optional {@link DataComponentPatch} attached.
	 *
	 * @apiNote While it is not required that the item is not air, it is expected to check yourself to make sure it is not air.
	 */
	public static ItemInfo fromItem(@NotNull ItemLike itemLike, @NotNull DataComponentPatch componentsPatch) {
		return new ItemInfo(itemLike.asItem().builtInRegistryHolder(), componentsPatch);
	}

	/**
	 * Creates an {@link ItemInfo} object from a given {@link Item} with an optional {@link DataComponentPatch} attached.
	 *
	 * @apiNote While it is not required that the item is not air, it is expected to check yourself to make sure it is not air.
	 */
	public static ItemInfo fromItem(@NotNull Holder<Item> item, @NotNull DataComponentPatch componentsPatch) {
		//TODO - 1.21: Update Docs
		return new ItemInfo(item, componentsPatch);
	}

	/**
	 * Creates an {@link ItemInfo} object from a given {@link Item} with no {@link DataComponentPatch} attached.
	 *
	 * @apiNote While it is not required that the item is not air, it is expected to check yourself to make sure it is not air.
	 */
	public static ItemInfo fromItem(@NotNull ItemLike itemLike) {
		return fromItem(itemLike.asItem().builtInRegistryHolder(), DataComponentPatch.EMPTY);
	}

	/**
	 * Creates an {@link ItemInfo} object from a given {@link Item} with no {@link DataComponentPatch} attached.
	 *
	 * @apiNote While it is not required that the item is not air, it is expected to check yourself to make sure it is not air.
	 */
	public static ItemInfo fromItem(@NotNull Holder<Item> item) {
		//TODO - 1.21: Update Docs
		return fromItem(item, DataComponentPatch.EMPTY);
	}

	/**
	 * Creates an {@link ItemInfo} object from a given {@link ItemStack}.
	 *
	 * @apiNote While it is not required that the stack is not empty, it is expected to check yourself to make sure it is not empty.
	 */
	public static ItemInfo fromStack(@NotNull ItemStack stack) {
		return new ItemInfo(stack.getItemHolder(), stack.getComponentsPatch());
	}

	/**
	 * Creates an {@link ItemInfo} object from a given {@link NSSItem}.
	 *
	 * @return An {@link ItemInfo} object from a given {@link NSSItem}, or null if the given {@link NSSItem} represents a tag or the item it represents is not registered
	 */
	@Nullable
	public static ItemInfo fromNSS(@NotNull NSSItem stack) {
		if (stack.representsTag()) {
			return null;
		}
		Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.getHolder(stack.getResourceLocation());
		//noinspection OptionalIsPresent - Capturing lambda
		if (holder.isEmpty()) {
			return null;
		}
		return fromItem(holder.get(), stack.getComponentsPatch());
	}

	/**
	 * @return The {@link Item} stored in this {@link ItemInfo}.
	 */
	@NotNull
	public Holder<Item> getItem() {
		//TODO - 1.21: Docs for this and all other methods we added that are undocumented
		return item;
	}

	/**
	 * @return The {@link DataComponentPatch} stored in this {@link ItemInfo}, or {@link DataComponentPatch#EMPTY} if there is no nbt data stored.
	 */
	@NotNull
	public DataComponentPatch getComponentsPatch() {
		//TODO - 1.21: Re-evaluate callers, should the component processors allow persisting and using default components?
		// The damage processor semi gets around this by creating the fake stack to see if it is damageable
		return componentsPatch;
	}

	/**
	 * Checks if this {@link ItemInfo} has an associated {@link DataComponentPatch}.
	 *
	 * @return True if this {@link ItemInfo} has an associated {@link DataComponentPatch}, false otherwise.
	 */
	public boolean hasModifiedData() {//TODO - 1.21: Re-evaluate if this method is useful
		return !componentsPatch.isEmpty();
	}

	/**
	 * @return A new {@link ItemStack} created from the stored {@link Item} and {@link DataComponentPatch}
	 */
	public ItemStack createStack() {
		return new ItemStack(getItem(), 1, getComponentsPatch());
	}

	@Override
	public int hashCode() {
		ResourceKey<Item> resourceKey = item.getKey();
		int code = resourceKey == null ? 0 : resourceKey.hashCode();
		code = 31 * code + componentsPatch.hashCode();
		return code;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof ItemInfo other) {
			return item.is(other.item) && componentsPatch.equals(other.componentsPatch);
		}
		return false;
	}

	@Override
	public String toString() {
		//TODO: If getRegisteredName ends up being a hotspot, replace it with a variant that acts upon Holder#getKey
		if (componentsPatch.isEmpty()) {
			return item.getRegisteredName();
		}
		//TODO - 1.21: Do we want to change this? It isn't in the same format as the command does it
		return item.getRegisteredName() + " " + componentsPatch;
	}
}