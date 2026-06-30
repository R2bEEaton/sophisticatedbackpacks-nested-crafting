package cc.spea.sbac.client;

import cc.spea.sbac.NestedCraftingUpgradeContainer;
import cc.spea.sbac.NestedCraftingSourcePriority;
import cc.spea.sbac.Sbac;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.SBPButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeTab;

import java.util.Map;

public class NestedCraftingUpgradeTab extends CraftingUpgradeTab {
	private static final ResourceLocation MEMORIZED_BACKPACK_SLOTS_ICON = ResourceLocation.fromNamespaceAndPath(
			Sbac.MOD_ID, "textures/gui/memorized_backpack_slots.png"
	);
	private static final ResourceLocation MEMORIZED_BACKPACK_SLOTS_OFF_ICON = ResourceLocation.fromNamespaceAndPath(
			Sbac.MOD_ID, "textures/gui/memorized_backpack_slots_off.png"
	);
	private static final ResourceLocation SOURCE_PRIORITY_STORAGE_FIRST_ICON = ResourceLocation.fromNamespaceAndPath(
			Sbac.MOD_ID, "textures/gui/source_priority_storage_first.png"
	);
	private static final ResourceLocation SOURCE_PRIORITY_NESTED_FIRST_ICON = ResourceLocation.fromNamespaceAndPath(
			Sbac.MOD_ID, "textures/gui/source_priority_nested_first.png"
	);
	private static final ResourceLocation SOURCE_PRIORITY_PLAYER_FIRST_ICON = ResourceLocation.fromNamespaceAndPath(
			Sbac.MOD_ID, "textures/gui/source_priority_player_first.png"
	);
	private static final ResourceLocation SOURCE_PRIORITY_MEMORIZED_FIRST_ICON = ResourceLocation.fromNamespaceAndPath(
			Sbac.MOD_ID, "textures/gui/source_priority_memorized_first.png"
	);
	private static final ResourceLocation PRESERVE_LAST_ITEM_ICON = ResourceLocation.fromNamespaceAndPath(
			Sbac.MOD_ID, "textures/gui/preserve_last_item.png"
	);
	private static final ResourceLocation PRESERVE_LAST_ITEM_OFF_ICON = ResourceLocation.fromNamespaceAndPath(
			Sbac.MOD_ID, "textures/gui/preserve_last_item_off.png"
	);
	private static final Position BUTTON_ICON_OFFSET = new Position(1, 1);

	private static final ButtonDefinition.Toggle<Boolean> USE_MEMORIZED_BACKPACK_SLOTS = ButtonDefinitions.createToggleButtonDefinition(
			Map.of(
					Boolean.TRUE,
					new ToggleButton.StateData(
							new TextureBlitData(MEMORIZED_BACKPACK_SLOTS_ICON, BUTTON_ICON_OFFSET, Dimension.SQUARE_16, new UV(0, 0), Dimension.SQUARE_16),
							Component.translatable("gui.sophisticatedbackpacks.upgrades.buttons.use_memorized_backpack_slots")
					),
					Boolean.FALSE,
					new ToggleButton.StateData(
							new TextureBlitData(MEMORIZED_BACKPACK_SLOTS_OFF_ICON, BUTTON_ICON_OFFSET, Dimension.SQUARE_16, new UV(0, 0), Dimension.SQUARE_16),
							Component.translatable("gui.sophisticatedbackpacks.upgrades.buttons.use_all_backpack_slots")
					)
			)
	);
	private static final ButtonDefinition.Toggle<NestedCraftingSourcePriority> SOURCE_PRIORITY = ButtonDefinitions.createToggleButtonDefinition(
			Map.of(
					NestedCraftingSourcePriority.STORAGE_FIRST,
					new ToggleButton.StateData(
							icon(SOURCE_PRIORITY_STORAGE_FIRST_ICON),
							Component.translatable("gui.sophisticatedbackpacks.upgrades.buttons.source_priority_storage_first")
					),
					NestedCraftingSourcePriority.NESTED_FIRST,
					new ToggleButton.StateData(
							icon(SOURCE_PRIORITY_NESTED_FIRST_ICON),
							Component.translatable("gui.sophisticatedbackpacks.upgrades.buttons.source_priority_nested_first")
					),
					NestedCraftingSourcePriority.PLAYER_FIRST,
					new ToggleButton.StateData(
							icon(SOURCE_PRIORITY_PLAYER_FIRST_ICON),
							Component.translatable("gui.sophisticatedbackpacks.upgrades.buttons.source_priority_player_first")
					),
					NestedCraftingSourcePriority.MEMORIZED_FIRST,
					new ToggleButton.StateData(
							icon(SOURCE_PRIORITY_MEMORIZED_FIRST_ICON),
							Component.translatable("gui.sophisticatedbackpacks.upgrades.buttons.source_priority_memorized_first")
					)
			)
	);
	private static final ButtonDefinition.Toggle<Boolean> PRESERVE_LAST_ITEM = ButtonDefinitions.createToggleButtonDefinition(
			Map.of(
					Boolean.TRUE,
					new ToggleButton.StateData(
							icon(PRESERVE_LAST_ITEM_ICON),
							Component.translatable("gui.sophisticatedbackpacks.upgrades.buttons.preserve_last_item")
					),
					Boolean.FALSE,
					new ToggleButton.StateData(
							icon(PRESERVE_LAST_ITEM_OFF_ICON),
							Component.translatable("gui.sophisticatedbackpacks.upgrades.buttons.use_last_item")
					)
			)
	);

	public NestedCraftingUpgradeTab(CraftingUpgradeContainer container, Position position, StorageScreenBase<?> screen) {
		super(container, position, screen, SBPButtonDefinitions.SHIFT_CLICK_TARGET, SBPButtonDefinitions.REFILL_CRAFTING_GRID);
		openTabDimension = new Dimension(openTabDimension.width() + 36, openTabDimension.height());
		if (container instanceof NestedCraftingUpgradeContainer nestedContainer) {
			addHideableChild(new ToggleButton<>(
					new Position(x + 39, y + 24),
					USE_MEMORIZED_BACKPACK_SLOTS,
					button -> nestedContainer.setUseMemorizedBackpackSlotsForNestedCrafting(
							!nestedContainer.shouldUseMemorizedBackpackSlotsForNestedCrafting()
					),
					nestedContainer::shouldUseMemorizedBackpackSlotsForNestedCrafting
			));
			addHideableChild(new ToggleButton<>(
					new Position(x + 57, y + 24),
					SOURCE_PRIORITY,
					button -> nestedContainer.setSourcePriority(nestedContainer.getSourcePriority().next()),
					nestedContainer::getSourcePriority
			));
			addHideableChild(new ToggleButton<>(
					new Position(x + 75, y + 24),
					PRESERVE_LAST_ITEM,
					button -> nestedContainer.setPreserveLastItem(!nestedContainer.shouldPreserveLastItem()),
					nestedContainer::shouldPreserveLastItem
			));
		}
	}

	private static TextureBlitData icon(ResourceLocation texture) {
		return new TextureBlitData(texture, BUTTON_ICON_OFFSET, Dimension.SQUARE_16, new UV(0, 0), Dimension.SQUARE_16);
	}
}
