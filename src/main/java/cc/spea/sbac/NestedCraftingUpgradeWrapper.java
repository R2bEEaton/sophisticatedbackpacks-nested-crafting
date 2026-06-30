package cc.spea.sbac;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

import java.util.function.Consumer;

public class NestedCraftingUpgradeWrapper extends CraftingUpgradeWrapper {
	public NestedCraftingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
	}

	@Override
	public boolean extractFromStorageOrPlayer(Player player, ItemStack stack) {
		return extractFromUpgradeProcessingInventory(stack) || extractFromPlayer(player, stack);
	}

	private boolean extractFromUpgradeProcessingInventory(ItemStack stack) {
		return !InventoryHelper
				.extractFromInventory(candidate -> ItemStack.isSameItemSameComponents(candidate, stack), 1, storageWrapper.getInventoryForUpgradeProcessing(),
						false)
				.isEmpty();
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
