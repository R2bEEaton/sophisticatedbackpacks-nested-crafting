package cc.spea.sbac;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class NestedCraftingSourceHelper {
	private NestedCraftingSourceHelper() {}

	public static ItemStack extractFromCraftingSources(NestedCraftingUpgradeWrapper wrapper, Predicate<ItemStack> matcher) {
		if (!wrapper.shouldUseMemorizedBackpackSlotsForNestedCrafting()) {
			return InventoryHelper.extractFromInventory(matcher, 1, wrapper.getStorageWrapper().getInventoryForUpgradeProcessing(), false);
		}

		ItemStack extracted = InventoryHelper.extractFromInventory(matcher, 1, wrapper.getStorageWrapper().getInventoryHandler(), false);
		if (!extracted.isEmpty()) {
			return extracted;
		}

		for (IItemHandler nestedInventory : getMemorizedBackpackSlotInventories(wrapper.getStorageWrapper())) {
			extracted = InventoryHelper.extractFromInventory(matcher, 1, nestedInventory, false);
			if (!extracted.isEmpty()) {
				return extracted;
			}
		}
		return ItemStack.EMPTY;
	}

	public static List<ItemStack> getWhitelistedCraftingStacks(IStorageWrapper storageWrapper) {
		List<ItemStack> stacks = copyStacks(storageWrapper.getInventoryHandler());
		for (IItemHandler nestedInventory : getMemorizedBackpackSlotInventories(storageWrapper)) {
			stacks.addAll(copyStacks(nestedInventory));
		}
		return stacks;
	}

	private static List<IItemHandler> getMemorizedBackpackSlotInventories(IStorageWrapper storageWrapper) {
		List<IItemHandler> inventories = new ArrayList<>();
		InventoryHandler inventory = storageWrapper.getInventoryHandler();
		MemorySettingsCategory memorySettings = storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			if (!memorySettings.isSlotSelected(slot)) {
				continue;
			}
			ItemStack slotStack = inventory.getStackInSlot(slot);
			if (slotStack.isEmpty() || !(slotStack.getItem() instanceof BackpackItem) || !memorySettings.matchesFilter(slot, slotStack)) {
				continue;
			}
			inventories.add(BackpackWrapper.fromStack(slotStack).getInventoryForUpgradeProcessing());
		}
		return inventories;
	}

	private static List<ItemStack> copyStacks(IItemHandler itemHandler) {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack stack : InventoryHelper.getStacks(itemHandler)) {
			stacks.add(stack.copy());
		}
		return stacks;
	}
}
