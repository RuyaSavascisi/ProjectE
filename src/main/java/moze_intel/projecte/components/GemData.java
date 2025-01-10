package moze_intel.projecte.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

//TODO - 1.21: Whitelist don't allow duplicates (and maybe make it a strict size of nine?
public record GemData(boolean isWhitelist, List<ItemStack> whitelist, List<ItemStack> consumed) {

	public static final GemData EMPTY = new GemData(false, Collections.emptyList(), Collections.emptyList());

	public static final Codec<GemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("isWhitelist").forGetter(GemData::isWhitelist),
			ItemStack.OPTIONAL_CODEC.listOf().fieldOf("whitelist").forGetter(GemData::whitelist),
			ItemStack.OPTIONAL_CODEC.listOf().fieldOf("consumed").forGetter(GemData::consumed)
	).apply(instance, GemData::new));
	//TODO - 1.21: Theoretically it will work as is because neo has builtin packet splitting for everything now
	// but we may want to evaluate moving this off to world save data (and also removing the ItemHelper method)
	public static final StreamCodec<RegistryFriendlyByteBuf, GemData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, GemData::isWhitelist,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, GemData::whitelist,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, GemData::consumed,
			GemData::new
	);

	public boolean whitelistMatches(Predicate<ItemStack> predicate) {
		return whitelist.stream().anyMatch(predicate);
	}

	public GemData toggleWhitelist() {
		return withWhitelist(!isWhitelist);
	}

	public GemData withWhitelist(boolean isWhitelist) {
		return new GemData(isWhitelist, whitelist, consumed);
	}

	public GemData withWhitelist(List<ItemStack> whitelist) {
		return new GemData(isWhitelist, List.copyOf(whitelist), consumed);
	}

	public GemData clearConsumed() {
		if (this == EMPTY) {
			return EMPTY;
		}
		return new GemData(isWhitelist, whitelist, Collections.emptyList());
	}

	public GemData addToList(ItemStack stack) {
		//TODO - 1.21: FIXME, make this a mutable list and make it so that the stacks are modifiable as well
		List<ItemStack> modifiableConsumed = consumed;
		boolean hasFound = false;
		for (ItemStack s : modifiableConsumed) {
			if (s.getCount() < s.getMaxStackSize() && ItemStack.isSameItemSameComponents(s, stack)) {
				int remain = s.getMaxStackSize() - s.getCount();
				if (stack.getCount() <= remain) {
					s.grow(stack.getCount());
					hasFound = true;
					break;
				} else {
					s.grow(remain);
					stack.shrink(remain);
				}
			}
		}
		if (!hasFound) {
			modifiableConsumed.add(stack);
		}
		//TODO - 1.21: Actually set the modified list back onto a gem data
		return new GemData(isWhitelist, whitelist, List.copyOf(modifiableConsumed));
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GemData other = (GemData) o;
		return isWhitelist == other.isWhitelist && ItemStack.listMatches(whitelist, other.whitelist) && ItemStack.listMatches(consumed, other.consumed);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int hashCode() {
		int hash = 31 * Boolean.hashCode(isWhitelist) + ItemStack.hashStackList(whitelist);
		return 31 * hash + ItemStack.hashStackList(consumed);
	}
}