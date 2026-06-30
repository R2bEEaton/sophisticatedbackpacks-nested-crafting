package cc.spea.sbac;

import cc.spea.sbac.compat.jei.NestedCraftingTransferPayload;
import cc.spea.sbac.compat.jei.SyncNestedInventoryPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

@Mod(Sbac.MOD_ID)
public class Sbac {
	public static final String MOD_ID = "sbac";
	public static final String SOPHISTICATED_BACKPACKS_MOD_ID = "sophisticatedbackpacks";

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SOPHISTICATED_BACKPACKS_MOD_ID);

	public static final DeferredHolder<Item, AdvancedCraftingUpgradeItem> ADVANCED_CRAFTING_UPGRADE = ITEMS.register("advanced_crafting_upgrade",
			AdvancedCraftingUpgradeItem::new);

	public Sbac(IEventBus modBus) {
		ITEMS.register(modBus);
		modBus.addListener(this::registerContainers);
		modBus.addListener(this::addCreativeTabItems);
		modBus.addListener(this::registerPayloads);
		NeoForge.EVENT_BUS.addListener(Sbac::onContainerOpen);
	}

	private void registerPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar("1")
			.playToServer(
				NestedCraftingTransferPayload.TYPE,
				NestedCraftingTransferPayload.STREAM_CODEC,
				NestedCraftingTransferPayload::handle
			)
			.playToClient(
				SyncNestedInventoryPayload.TYPE,
				SyncNestedInventoryPayload.STREAM_CODEC,
				SyncNestedInventoryPayload::handle
			);
	}

	private static void onContainerOpen(PlayerContainerEvent.Open event) {
		if (!(event.getContainer() instanceof BackpackContainer backpackContainer)) return;
		if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

		var processingInv = backpackContainer.getStorageWrapper().getInventoryForUpgradeProcessing();
		var items = new java.util.ArrayList<ItemStack>();
		for (ItemStack stack : InventoryHelper.getStacks(processingInv)) {
			items.add(stack.copy());
		}

		PacketDistributor.sendToPlayer(serverPlayer, new SyncNestedInventoryPayload(items));
	}

	private void registerContainers(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.MENU)) {
			UpgradeContainerRegistry.register(ADVANCED_CRAFTING_UPGRADE.getId(), net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.CRAFTING_TYPE);
		}
	}

	private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES
				|| event.getTab() == net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.CREATIVE_TAB.get()) {
			event.accept(ADVANCED_CRAFTING_UPGRADE.get());
		}
	}
}
