package moze_intel.projecte.gameObjs.items;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;
import moze_intel.projecte.gameObjs.items.DiviningRod.DiviningMode;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public class DiviningRod extends ItemPE implements IItemMode<DiviningMode> {

	private final int maxModes;

	public DiviningRod(Properties props, int maxModes) {
		super(props.component(PEDataComponentTypes.DIVINING_ROD_MODE, DiviningMode.LOW));
		this.maxModes = maxModes;
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null) {
			return InteractionResult.FAIL;
		}
		Level level = ctx.getLevel();
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		LongList emcValues = new LongArrayList();
		long totalEmc = 0;
		int numBlocks = 0;
		int depth = getDepthFromMode(ctx.getItemInHand());
		//Lazily retrieve the values for the furnace recipes
		Lazy<List<RecipeHolder<SmeltingRecipe>>> furnaceRecipes = Lazy.of(() -> level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING));
		for (BlockPos digPos : WorldHelper.getPositionsInBox(WorldHelper.getDeepBox(ctx.getClickedPos(), ctx.getClickedFace(), depth))) {
			BlockState state = level.getBlockState(digPos);
			if (state.isAir()) {
				continue;
			}
			List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, digPos, WorldHelper.getBlockEntity(level, digPos), player, ctx.getItemInHand());
			if (drops.isEmpty()) {
				continue;
			}
			ItemStack blockStack = drops.get(0);
			long blockEmc = EMCHelper.getEmcValue(blockStack);
			if (blockEmc == 0) {
				for (RecipeHolder<SmeltingRecipe> furnaceRecipeHolder : furnaceRecipes.get()) {
					SmeltingRecipe furnaceRecipe = furnaceRecipeHolder.value();
					if (furnaceRecipe.getIngredients().get(0).test(blockStack)) {
						long currentValue = EMCHelper.getEmcValue(furnaceRecipe.getResultItem(level.registryAccess()));
						if (currentValue != 0) {
							if (!emcValues.contains(currentValue)) {
								emcValues.add(currentValue);
							}
							totalEmc += currentValue;
							break;
						}
					}
				}
			} else {
				if (!emcValues.contains(blockEmc)) {
					emcValues.add(blockEmc);
				}
				totalEmc += blockEmc;
			}
			numBlocks++;
		}

		if (numBlocks == 0) {
			return InteractionResult.FAIL;
		}
		player.sendSystemMessage(PELang.DIVINING_AVG_EMC.translate(numBlocks, totalEmc / numBlocks));
		if (this == PEItems.MEDIUM_DIVINING_ROD.get() || this == PEItems.HIGH_DIVINING_ROD.get()) {
			emcValues.sort(LongComparators.OPPOSITE_COMPARATOR);
			player.sendSystemMessage(PELang.DIVINING_SECOND_MAX.translate(getOrDefault(emcValues, 0)));
			if (this == PEItems.HIGH_DIVINING_ROD.get()) {
				player.sendSystemMessage(PELang.DIVINING_SECOND_MAX.translate(getOrDefault(emcValues, 1)));
				player.sendSystemMessage(PELang.DIVINING_THIRD_MAX.translate(getOrDefault(emcValues, 2)));
			}
		}
		return InteractionResult.CONSUME;
	}

	private static long getOrDefault(LongList emcValues, int index) {
		return index < emcValues.size() ? emcValues.getLong(index) : 1;
	}

	private int getDepthFromMode(ItemStack stack) {
		DiviningMode mode = IItemMode.super.getMode(stack);
		if (mode.ordinal() > maxModes) {
			//No range something went wrong
			return 0;
		}
		return mode.range;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(getToolTip(stack));
	}

	@Override
	public DataComponentType<DiviningMode> getDataComponentType() {
		return PEDataComponentTypes.DIVINING_ROD_MODE.get();
	}

	@Override
	public DiviningMode getDefaultMode() {
		return DiviningMode.LOW;
	}

	public enum DiviningMode implements IModeEnum<DiviningMode> {
		LOW(PELang.DIVINING_RANGE_3, 3),
		MEDIUM(PELang.DIVINING_RANGE_16, 16),
		HIGH(PELang.DIVINING_RANGE_64, 64);

		public static final Codec<DiviningMode> CODEC = StringRepresentable.fromEnum(DiviningMode::values);
		public static final IntFunction<DiviningMode> BY_ID = ByIdMap.continuous(DiviningMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
		public static final StreamCodec<ByteBuf, DiviningMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DiviningMode::ordinal);

		private final IHasTranslationKey langEntry;
		private final String serializedName;
		private final int range;

		DiviningMode(IHasTranslationKey langEntry, int range) {
			this.serializedName = name().toLowerCase(Locale.ROOT);
			this.langEntry = langEntry;
			this.range = range;
		}

		@NotNull
		@Override
		public String getSerializedName() {
			return serializedName;
		}

		@Override
		public String getTranslationKey() {
			return langEntry.getTranslationKey();
		}

		@Override
		public DiviningMode next(ItemStack stack) {
			if (ordinal() > ((DiviningRod) stack.getItem()).maxModes) {
				//Don't increment
				return this;
			}
			return switch (this) {
				case LOW -> MEDIUM;
				case MEDIUM -> HIGH;
				case HIGH -> LOW;
			};
		}
	}
}