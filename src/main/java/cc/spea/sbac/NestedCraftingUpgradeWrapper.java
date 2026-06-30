package cc.spea.sbac;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;

import java.util.function.Consumer;

public class NestedCraftingUpgradeWrapper extends CraftingUpgradeWrapper {
	public NestedCraftingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
	}

	@Override
	public boolean extractFromStorageOrPlayer(Player player, ItemStack stack) {
		return !NestedCraftingSourceHelper.extractFromCraftingSources(this, player, candidate -> ItemStack.isSameItemSameComponents(candidate, stack)).isEmpty();
	}

	public IStorageWrapper getStorageWrapper() {
		return storageWrapper;
	}

	public boolean shouldUseMemorizedBackpackSlotsForNestedCrafting() {
		return upgrade.getOrDefault(Sbac.USE_MEMORIZED_BACKPACK_SLOTS.get(), false);
	}

	public void setUseMemorizedBackpackSlotsForNestedCrafting(boolean useMemorizedBackpackSlots) {
		upgrade.set(Sbac.USE_MEMORIZED_BACKPACK_SLOTS.get(), useMemorizedBackpackSlots);
		save();
	}

	public NestedCraftingSourcePriority getSourcePriority() {
		return NestedCraftingSourcePriority.byId(upgrade.getOrDefault(Sbac.SOURCE_PRIORITY.get(), NestedCraftingSourcePriority.STORAGE_FIRST.getId()));
	}

	public void setSourcePriority(NestedCraftingSourcePriority sourcePriority) {
		upgrade.set(Sbac.SOURCE_PRIORITY.get(), sourcePriority.getId());
		save();
	}

	public boolean shouldPreserveLastItem() {
		return upgrade.getOrDefault(Sbac.PRESERVE_LAST_ITEM.get(), false);
	}

	public void setPreserveLastItem(boolean preserveLastItem) {
		upgrade.set(Sbac.PRESERVE_LAST_ITEM.get(), preserveLastItem);
		save();
	}
}
