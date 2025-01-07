package moze_intel.projecte.network.commands.parser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Customized version of {@link net.minecraft.commands.arguments.item.ItemParser} that does not support NBT on tags, and does not wrap it into a Predicate.
 */
public class NSSItemParser {//TODO - 1.21: Update to be more like ItemParser and ResourceOrTagKeyArgument in terms of supporting components

	//This error message is a copy of ItemParser ERROR_UNKNOWN_ITEM and ERROR_UNKNOWN_TAG
	private static final DynamicCommandExceptionType UNKNOWN_ITEM = new DynamicCommandExceptionType(PELang.UNKNOWN_ITEM::translate);
	private static final DynamicCommandExceptionType UNKNOWN_TAG = new DynamicCommandExceptionType(PELang.UNKNOWN_TAG::translate);
	private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
	private static final char SYNTAX_START_NBT = '{';
	private static final char SYNTAX_TAG = '#';

	private final HolderLookup<Item> items;
	private Either<Holder<Item>, ResourceLocation> result;
	@Nullable
	private CompoundTag nbt;
	/** Builder to be used when creating a list of suggestions */
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

	public NSSItemParser(CommandBuildContext context) {
		this.items = context.lookupOrThrow(Registries.ITEM);
	}

	public NSSItem parseResult(StringReader reader) throws CommandSyntaxException {
		int cursor = reader.getCursor();
		try {
			parse(reader);
			//TODO - 1.21: Figure out??
			return result.map(item -> NSSItem.createItem(item, DataComponentPatch.EMPTY), NSSItem::createTag);
		} catch (CommandSyntaxException e) {
			reader.setCursor(cursor);
			throw e;
		}
	}

	/**
	 * Create a list of suggestions for the specified builder.
	 *
	 * @param builder Builder to create list of suggestions
	 */
	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder builder) {
		StringReader reader = new StringReader(builder.getInput());
		reader.setCursor(builder.getStart());
		try {
			parse(reader);
		} catch (CommandSyntaxException ignored) {
		}
		return suggestions.apply(builder.createOffset(reader.getCursor()));
	}

	private void parse(StringReader reader) throws CommandSyntaxException {
		this.suggestions = this::suggestTagOrItem;
		int cursor = reader.getCursor();
		if (reader.canRead() && reader.peek() == SYNTAX_TAG) {
			//Read Tag
			reader.expect(SYNTAX_TAG);
			this.suggestions = this::suggestTag;
			ResourceLocation name = ResourceLocation.read(reader);
			Optional<? extends HolderSet<Item>> tag = this.items.get(TagKey.create(Registries.ITEM, name));
			tag.orElseThrow(() -> {
				//If it isn't present reset and error
				reader.setCursor(cursor);
				return UNKNOWN_TAG.createWithContext(reader, name);
			});
			this.result = Either.right(name);
		} else {
			//Read Item
			ResourceLocation name = ResourceLocation.read(reader);
			Optional<Holder.Reference<Item>> item = this.items.get(ResourceKey.create(Registries.ITEM, name));
			this.result = Either.left(item.orElseThrow(() -> {
				reader.setCursor(cursor);
				return UNKNOWN_ITEM.createWithContext(reader, name);
			}));
			this.suggestions = this::suggestOpenNbt;
			if (reader.canRead() && reader.peek() == SYNTAX_START_NBT) {
				this.suggestions = SUGGEST_NOTHING;
				this.nbt = new TagParser(reader).readStruct();
			}
		}
	}

	private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder builder) {
		if (builder.getRemaining().isEmpty()) {
			builder.suggest(String.valueOf(SYNTAX_START_NBT));
		}
		return builder.buildFuture();
	}

	/**
	 * Builds a list of suggestions based on item tags.
	 *
	 * @param builder Builder to create list of suggestions
	 */
	private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(this.items.listTags().map(reference -> reference.key().location()), builder, String.valueOf(SYNTAX_TAG));
	}

	private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(this.items.listElements().map(reference -> reference.key().location()), builder);
	}

	/**
	 * Builds a list of suggestions based on item tags (if the parser is set to allows tags) and item registry names.
	 *
	 * @param builder Builder to create list of suggestions
	 */
	private CompletableFuture<Suggestions> suggestTagOrItem(SuggestionsBuilder builder) {
		suggestTag(builder);
		return suggestItem(builder);
	}
}