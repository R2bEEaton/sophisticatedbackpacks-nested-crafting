package cc.spea.sbac;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
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

	public static ItemStack extractFromCraftingSources(NestedCraftingUpgradeWrapper wrapper, Player player, Predicate<ItemStack> matcher) {
		Predicate<ItemStack> sourceMatcher = getSourceMatcher(wrapper, matcher);
		return switch (wrapper.getSourcePriority()) {
			case STORAGE_FIRST -> extractInOrder(wrapper, player, sourceMatcher, SourceGroup.STORAGE, SourceGroup.NESTED, SourceGroup.PLAYER);
			case NESTED_FIRST -> extractInOrder(wrapper, player, sourceMatcher, SourceGroup.NESTED, SourceGroup.STORAGE, SourceGroup.PLAYER);
			case PLAYER_FIRST -> extractInOrder(wrapper, player, sourceMatcher, SourceGroup.PLAYER, SourceGroup.STORAGE, SourceGroup.NESTED);
			case MEMORIZED_FIRST -> extractInOrder(wrapper, player, sourceMatcher, SourceGroup.MEMORIZED_NESTED, SourceGroup.STORAGE,
					SourceGroup.OTHER_NESTED, SourceGroup.PLAYER);
		};
	}

	public static List<ItemStack> getWhitelistedCraftingStacks(IStorageWrapper storageWrapper) {
		List<ItemStack> stacks = copyStacks(storageWrapper.getInventoryHandler());
		for (NestedBackpackSource nestedSource : getNestedBackpackSlotInventories(storageWrapper, true, false)) {
			stacks.addAll(copyStacks(nestedSource.inventory()));
		}
		return stacks;
	}

	private static ItemStack extractInOrder(NestedCraftingUpgradeWrapper wrapper, Player player, Predicate<ItemStack> matcher, SourceGroup... sourceGroups) {
		for (SourceGroup sourceGroup : sourceGroups) {
			ItemStack extracted = extractFromSourceGroup(wrapper, player, matcher, sourceGroup);
			if (!extracted.isEmpty()) {
				return extracted;
			}
		}
		return ItemStack.EMPTY;
	}

	private static ItemStack extractFromSourceGroup(NestedCraftingUpgradeWrapper wrapper, Player player, Predicate<ItemStack> matcher, SourceGroup sourceGroup) {
		return switch (sourceGroup) {
			case STORAGE -> InventoryHelper.extractFromInventory(matcher, 1, wrapper.getStorageWrapper().getInventoryHandler(), false);
			case NESTED -> extractFromNestedBackpackSources(wrapper, matcher, false, false);
			case MEMORIZED_NESTED -> extractFromNestedBackpackSources(wrapper, matcher, true, false);
			case OTHER_NESTED -> extractFromNestedBackpackSources(wrapper, matcher, false, true);
			case PLAYER -> extractFromPlayer(player, matcher);
		};
	}

	private static ItemStack extractFromNestedBackpackSources(NestedCraftingUpgradeWrapper wrapper, Predicate<ItemStack> matcher, boolean memorizedOnly,
			boolean excludeMemorized) {
		for (NestedBackpackSource nestedSource : getNestedBackpackSlotInventories(wrapper.getStorageWrapper(), memorizedOnly, excludeMemorized)) {
			if (wrapper.shouldUseMemorizedBackpackSlotsForNestedCrafting() && !nestedSource.memorized()) {
				continue;
			}
			ItemStack extracted = InventoryHelper.extractFromInventory(matcher, 1, nestedSource.inventory(), false);
			if (!extracted.isEmpty()) {
				return extracted;
			}
		}
		return ItemStack.EMPTY;
	}

	private static ItemStack extractFromPlayer(Player player, Predicate<ItemStack> matcher) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (!stack.isEmpty() && matcher.test(stack)) {
				ItemStack extracted = stack.split(1);
				player.getInventory().setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
				return extracted;
			}
		}
		return ItemStack.EMPTY;
	}

	private static Predicate<ItemStack> getSourceMatcher(NestedCraftingUpgradeWrapper wrapper, Predicate<ItemStack> matcher) {
		if (!wrapper.shouldPreserveLastItem()) {
			return matcher;
		}
		return stack -> stack.getCount() > 1 && matcher.test(stack);
	}

	private static List<NestedBackpackSource> getNestedBackpackSlotInventories(IStorageWrapper storageWrapper, boolean memorizedOnly, boolean excludeMemorized) {
		List<NestedBackpackSource> inventories = new ArrayList<>();
		InventoryHandler inventory = storageWrapper.getInventoryHandler();
		MemorySettingsCategory memorySettings = storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			boolean memorized = memorySettings.isSlotSelected(slot);
			if ((memorizedOnly && !memorized) || (excludeMemorized && memorized)) {
				continue;
			}
			ItemStack slotStack = inventory.getStackInSlot(slot);
			if (slotStack.isEmpty() || !(slotStack.getItem() instanceof BackpackItem)) {
				continue;
			}
			if (memorized && !memorySettings.matchesFilter(slot, slotStack)) {
				continue;
			}
			inventories.add(new NestedBackpackSource(BackpackWrapper.fromStack(slotStack).getInventoryForUpgradeProcessing(), memorized));
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

	private enum SourceGroup {
		STORAGE,
		NESTED,
		MEMORIZED_NESTED,
		OTHER_NESTED,
		PLAYER
	}

	private record NestedBackpackSource(IItemHandler inventory, boolean memorized) {}
}
