package cc.spea.sbac;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;

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
