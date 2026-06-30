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
		return !NestedCraftingSourceHelper.extractFromCraftingSources(this, candidate -> ItemStack.isSameItemSameComponents(candidate, stack)).isEmpty()
				|| extractFromPlayer(player, stack);
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

	private boolean extractFromPlayer(Player player, ItemStack stack) {
		int playerInvMatchingIndex = player.getInventory().findSlotMatchingItem(stack);
		if (playerInvMatchingIndex >= 0) {
			player.getInventory().removeItem(playerInvMatchingIndex, 1);
			return true;
		}
		return false;
	}
}
