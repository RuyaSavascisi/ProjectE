package moze_intel.projecte.gameObjs.registries;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registration.PEDeferredHolder;
import moze_intel.projecte.gameObjs.registration.PEDeferredRegister;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.impl.capability.AlchBagImpl.AlchemicalBagAttachment;
import moze_intel.projecte.impl.capability.KnowledgeImpl.KnowledgeAttachment;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class PEAttachmentTypes {

	private PEAttachmentTypes() {
	}

	public static final PEDeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = new PEDeferredRegister<>(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PECore.MODID);

	public static final PEDeferredHolder<AttachmentType<?>, AttachmentType<AlchemicalBagAttachment>> ALCHEMICAL_BAGS = ATTACHMENT_TYPES.register("alchemical_bags",
			() -> AttachmentType.builder(AlchemicalBagAttachment::new)
					.serialize(AlchemicalBagAttachment.CODEC)
					.copyHandler(AlchemicalBagAttachment::copy)
					.copyOnDeath()
					.build()
	);

	public static final PEDeferredHolder<AttachmentType<?>, AttachmentType<KnowledgeAttachment>> KNOWLEDGE = ATTACHMENT_TYPES.register("knowledge",
			() -> AttachmentType.builder(KnowledgeAttachment::new)
					.serialize(KnowledgeAttachment.CODEC)
					.copyHandler(KnowledgeAttachment::copy)
					.copyOnDeath()
					.build()
	);

	//Note: For current abilities we don't bother serializing them or copying on death
	//TODO: Eventually we may want to evaluate this as it technically allows people to bypass timers, but for now we don't really care
	public static final PEDeferredHolder<AttachmentType<?>, AttachmentType<CommonInternalAbilities>> COMMON_INTERNAL_ABILITIES = ATTACHMENT_TYPES.register("common_internal_abilities",
			() -> AttachmentType.builder(CommonInternalAbilities::new).build()
	);
	public static final PEDeferredHolder<AttachmentType<?>, AttachmentType<InternalTimers>> INTERNAL_TIMERS = ATTACHMENT_TYPES.register("internal_timers",
			() -> AttachmentType.builder(InternalTimers::new).build()
	);
	public static final PEDeferredHolder<AttachmentType<?>, AttachmentType<InternalAbilities>> INTERNAL_ABILITIES = ATTACHMENT_TYPES.register("internal_abilities",
			() -> AttachmentType.builder(InternalAbilities::new).build()
	);

	public static <HANDLER extends IItemHandlerModifiable> HANDLER copyHandler(IItemHandler handler, Int2ObjectFunction<HANDLER> handlerCreator) {
		int slots = handler.getSlots();
		HANDLER handlerCopy = handlerCreator.get(slots);
		for (int i = 0; i < slots; i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				handlerCopy.setStackInSlot(i, stack.copy());
			}
		}
		return handlerCopy;
	}
}