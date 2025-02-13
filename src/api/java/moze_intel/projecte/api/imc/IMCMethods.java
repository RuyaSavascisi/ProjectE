package moze_intel.projecte.api.imc;

/**
 * This class declares the IMC methods accepted by ProjectE and their argument types
 */
public class IMCMethods {

	/**
	 * Registers a custom EMC value.
	 * <p>
	 * The Object sent must be an instance of {@link CustomEMCRegistration}, or else the message is ignored.
	 */
	public static final String REGISTER_CUSTOM_EMC = "register_custom_emc";
}