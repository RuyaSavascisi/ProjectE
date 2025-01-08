package moze_intel.projecte.utils.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Helper interface for creating formatted translations in our lang enums
 *
 * @apiNote From Mekanism
 */
public interface ILangEntry extends IHasTranslationKey {

	/**
	 * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place
	 */
	default MutableComponent translate(Object... args) {
		return TextComponentUtil.smartTranslate(getTranslationKey(), args);
	}

	/**
	 * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place.
	 */
	default MutableComponent translate() {
		return TextComponentUtil.translate(getTranslationKey());
	}

	/**
	 * Translates this {@link ILangEntry} and applies the {@link net.minecraft.network.chat.TextColor} of the given {@link ChatFormatting} to the {@link
	 * net.minecraft.network.chat.Component}
	 */
	default MutableComponent translateColored(ChatFormatting color, Object... args) {
		return TextComponentUtil.build(color, translate(args));
	}

	/**
	 * Translates this {@link ILangEntry} and applies the {@link net.minecraft.network.chat.TextColor} to the {@link Component}.
	 *
	 * @since 10.4.0
	 */
	default MutableComponent translateColored(ChatFormatting color) {
		return TextComponentUtil.build(color, translate());
	}
}