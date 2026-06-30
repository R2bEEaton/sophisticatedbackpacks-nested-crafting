package cc.spea.sbac;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;

public class NestedCraftingUpgradeContainer extends CraftingUpgradeContainer {
	private static final String DATA_USE_MEMORIZED_BACKPACK_SLOTS = "useMemorizedBackpackSlots";
	private static final String DATA_SOURCE_PRIORITY = "sourcePriority";
	private static final String DATA_PRESERVE_LAST_ITEM = "preserveLastItem";

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
		if (data.contains(DATA_SOURCE_PRIORITY)) {
			getNestedWrapper().setSourcePriority(NestedCraftingSourcePriority.byId(data.getInt(DATA_SOURCE_PRIORITY)));
		}
		if (data.contains(DATA_PRESERVE_LAST_ITEM)) {
			getNestedWrapper().setPreserveLastItem(data.getBoolean(DATA_PRESERVE_LAST_ITEM));
		}
	}

	public boolean shouldUseMemorizedBackpackSlotsForNestedCrafting() {
		return getNestedWrapper().shouldUseMemorizedBackpackSlotsForNestedCrafting();
	}

	public void setUseMemorizedBackpackSlotsForNestedCrafting(boolean useMemorizedBackpackSlots) {
		getNestedWrapper().setUseMemorizedBackpackSlotsForNestedCrafting(useMemorizedBackpackSlots);
		sendBooleanToServer(DATA_USE_MEMORIZED_BACKPACK_SLOTS, useMemorizedBackpackSlots);
	}

	public NestedCraftingSourcePriority getSourcePriority() {
		return getNestedWrapper().getSourcePriority();
	}

	public void setSourcePriority(NestedCraftingSourcePriority sourcePriority) {
		getNestedWrapper().setSourcePriority(sourcePriority);
		sendDataToServer(() -> {
			CompoundTag tag = new CompoundTag();
			tag.putInt(DATA_SOURCE_PRIORITY, sourcePriority.getId());
			return tag;
		});
	}

	public boolean shouldPreserveLastItem() {
		return getNestedWrapper().shouldPreserveLastItem();
	}

	public void setPreserveLastItem(boolean preserveLastItem) {
		getNestedWrapper().setPreserveLastItem(preserveLastItem);
		sendBooleanToServer(DATA_PRESERVE_LAST_ITEM, preserveLastItem);
	}

	private NestedCraftingUpgradeWrapper getNestedWrapper() {
		return (NestedCraftingUpgradeWrapper) upgradeWrapper;
	}
}
