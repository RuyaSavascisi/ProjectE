package moze_intel.projecte.api.config;

public interface IConfigurableElement {

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
}