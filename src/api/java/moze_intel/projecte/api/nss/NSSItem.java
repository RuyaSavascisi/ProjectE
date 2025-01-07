package moze_intel.projecte.api.nss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import moze_intel.projecte.api.codec.NSSCodecHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link NSSTag} for representing {@link Item}s.
 */
public final class NSSItem extends AbstractDataComponentHolderNSSTag<Item> {

	private static final boolean ALLOW_DEFAULT = false;

	private static final Codec<String> OPTIONAL_PREFIX_CODEC = Codec.of(ExtraCodecs.NON_EMPTY_STRING, ExtraCodecs.NON_EMPTY_STRING.flatMap(str -> {
		if (str.startsWith("ITEM|")) {
			return DataResult.success(str.substring(5));
		}
		return DataResult.success(str);
	}), ExtraCodecs.NON_EMPTY_STRING + "[projecte:optionalPrefix]");

	/**
	 * Codec for encoding NSSItems to and from strings.
	 */
	public static final Codec<NSSItem> LEGACY_CODEC = createLegacyCodec(BuiltInRegistries.ITEM, ALLOW_DEFAULT, OPTIONAL_PREFIX_CODEC, NSSItem::new);

	public static final MapCodec<NSSItem> EXPLICIT_CODEC = createExplicitCodec(BuiltInRegistries.ITEM, ALLOW_DEFAULT, NSSItem::new);

	public static final NSSCodecHolder<NSSItem> CODECS = new NSSCodecHolder<>("ITEM", LEGACY_CODEC, EXPLICIT_CODEC);

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
	public NSSCodecHolder<?> codecs() {
		return CODECS;
	}
}