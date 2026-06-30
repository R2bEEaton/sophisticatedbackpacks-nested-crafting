package cc.spea.sbac;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;

public class NestedCraftingUpgradeContainer extends CraftingUpgradeContainer {
	private static final String DATA_USE_MEMORIZED_BACKPACK_SLOTS = "useMemorizedBackpackSlots";

	public static final UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> TYPE = new UpgradeContainerType<>(
			NestedCraftingUpgradeContainer::new
	);

	public NestedCraftingUpgradeContainer(Player player, int upgradeContainerId, CraftingUpgradeWrapper upgradeWrapper,
			UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> type) {
		super(player, upgradeContainerId, upgradeWrapper, type);
	}

	@Override
	public void handlePacket(CompoundTag data) {
		super.handlePacket(data);
		if (data.contains(DATA_USE_MEMORIZED_BACKPACK_SLOTS)) {
			getNestedWrapper().setUseMemorizedBackpackSlotsForNestedCrafting(data.getBoolean(DATA_USE_MEMORIZED_BACKPACK_SLOTS));
		}
	}

	public boolean shouldUseMemorizedBackpackSlotsForNestedCrafting() {
		return getNestedWrapper().shouldUseMemorizedBackpackSlotsForNestedCrafting();
	}

	public void setUseMemorizedBackpackSlotsForNestedCrafting(boolean useMemorizedBackpackSlots) {
		getNestedWrapper().setUseMemorizedBackpackSlotsForNestedCrafting(useMemorizedBackpackSlots);
		sendBooleanToServer(DATA_USE_MEMORIZED_BACKPACK_SLOTS, useMemorizedBackpackSlots);
	}

	private NestedCraftingUpgradeWrapper getNestedWrapper() {
		return (NestedCraftingUpgradeWrapper) upgradeWrapper;
	}
}
