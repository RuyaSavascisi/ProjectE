package moze_intel.projecte.network.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.network.commands.parser.NSSItemParser;
import net.minecraft.commands.CommandBuildContext;

//TODO - 1.21: Remove this?
public class NSSItemArgument implements ArgumentType<NSSItem> {

	private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "minecraft:stick{foo=bar}", "#minecraft:wool");

	private final NSSItemParser parser;

	private NSSItemArgument(CommandBuildContext context) {
		this.parser = new NSSItemParser(context);
	}

	public static NSSItemArgument nss(CommandBuildContext context) {
		return new NSSItemArgument(context);
	}

	@Override
	public NSSItem parse(StringReader reader) throws CommandSyntaxException {
		return this.parser.parseResult(reader);
	}

	public static <S> NSSItem getNSS(CommandContext<S> context, String name) {
		return context.getArgument(name, NSSItem.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return this.parser.fillSuggestions(builder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}