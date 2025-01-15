package moze_intel.projecte.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

//TODO - 1.21: Whitelist don't allow duplicates (and maybe make it a strict size of nine?). Maybe just make it a set???
//TODO - 1.21: Do we want to enforce whitelist and consumed being unmodifiable views?
public record GemData(boolean isWhitelist, List<ItemStack> whitelist, List<ItemStack> consumed) {

	public static final GemData EMPTY = new GemData(false, Collections.emptyList(), Collections.emptyList());

	public static final Codec<GemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("isWhitelist").forGetter(GemData::isWhitelist),
			ItemStack.OPTIONAL_CODEC.listOf().fieldOf("whitelist").forGetter(GemData::whitelist),
			ItemStack.OPTIONAL_CODEC.listOf().fieldOf("consumed").forGetter(GemData::consumed)
	).apply(instance, GemData::new));
	//TODO: Theoretically it will work as is because neo has builtin packet splitting for everything now
	// but we may want to evaluate moving this off to world save data (and also removing the ItemHelper method)
	public static final StreamCodec<RegistryFriendlyByteBuf, GemData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, GemData::isWhitelist,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, GemData::whitelist,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, GemData::consumed,
			GemData::new
	);

	public boolean whitelistMatches(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		for (ItemStack itemStack : whitelist) {
			if (ItemStack.isSameItemSameComponents(stack, itemStack)) {
				return true;
			}
		}
		return false;
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

	/**
	 * @param stack May be modified by the method
	 */
	public GemData addToList(ItemStack stack) {
		if (stack.isEmpty()) {
			//Nothing to do, just return this element
			return this;
		}
		//Note: We make a shallow copy so that we don't need to use as much memory keeping track of individual stacks
		// when we modify a stack we copy it then
		List<ItemStack> modifiableConsumed = new ArrayList<>(consumed);
		for (int i = 0, size = modifiableConsumed.size(); i < size; i++) {
			ItemStack existing = modifiableConsumed.get(i);
			int maxStackSize = existing.getMaxStackSize();
			if (existing.getCount() < maxStackSize && ItemStack.isSameItemSameComponents(existing, stack)) {
				int spaceAvailable = maxStackSize - existing.getCount();
				if (stack.getCount() <= spaceAvailable) {
					//Replace the element that we are merging into with a fresh copy so that we don't affect the old data
					existing = existing.copyWithCount(existing.getCount() + stack.getCount());
					modifiableConsumed.set(i, existing);
					return new GemData(isWhitelist, whitelist, List.copyOf(modifiableConsumed));
				} else {
					//Replace the element that we are merging into with a fresh copy so that we don't affect the old data
					existing = existing.copyWithCount(existing.getCount() + spaceAvailable);
					modifiableConsumed.set(i, existing);
					//Note: We shrink the existing stack that we were passed as we can mutate it
					stack.shrink(spaceAvailable);
				}
			}
		}
		//Add whatever remains of the stack to the end of the list
		modifiableConsumed.add(stack);
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