package moze_intel.projecte.api.config;

import moze_intel.projecte.api.mapper.IEMCMapper;

public interface IConfigurableElement<TYPE> {

	/**
	 * A unique Name for the {@link IConfigurableElement}. This is used to identify the {@link IConfigurableElement} in the Configuration.
	 *
	 * @return A unique Name
	 */
	String getName();

	//TODO - 1.21: Docs?
	default String getConfigPath() {
		return getName().replace(' ', '-');
	}

	/**
	 * The translation key representing the translated version of the name, that will be included in the Configuration GUI.
	 *
	 * @implNote You should also include translations for this key plus ".tooltip" for the description, and this key plus ".button" for the button to open the section.
	 */
	String getTranslationKey();

	/**
	 * A Description, that will be included as a Comment in the Configuration File
	 *
	 * @implNote The translation for this description should be located at {@link #getTranslationKey()} + ".tooltip"
	 */
	String getDescription();

	/**
	 * This method is used to determine the default for enabling/disabling this element.
	 *
	 * @return {@code true} if you want this element to be part of the EMC calculations, {@code false} otherwise.
	 */
	default boolean isAvailable() {
		return true;
	}

	/**
	 * Use the config object to generate a useful Configuration for your {@link IEMCMapper}. <br/> The Configuration Object will be a
	 * {@link com.electronwill.nightconfig.core.file.CommentedFileConfig} representing the top-level mapping.cfg file. Please use properly prefixed config keys and do not
	 * clobber those not belonging to your mapper
	 */
	default void addConfigOptions(IConfigBuilder<TYPE> configBuilder) {
		//TODO - 1.21: Docs, and update the docs for addMappings
	}
}