package cc.spea.sbac.client;

import cc.spea.sbac.NestedCraftingUpgradeContainer;
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

	public NestedCraftingUpgradeTab(CraftingUpgradeContainer container, Position position, StorageScreenBase<?> screen) {
		super(container, position, screen, SBPButtonDefinitions.SHIFT_CLICK_TARGET, SBPButtonDefinitions.REFILL_CRAFTING_GRID);
		if (container instanceof NestedCraftingUpgradeContainer nestedContainer) {
			addHideableChild(new ToggleButton<>(
					new Position(x + 39, y + 24),
					USE_MEMORIZED_BACKPACK_SLOTS,
					button -> nestedContainer.setUseMemorizedBackpackSlotsForNestedCrafting(
							!nestedContainer.shouldUseMemorizedBackpackSlotsForNestedCrafting()
					),
					nestedContainer::shouldUseMemorizedBackpackSlotsForNestedCrafting
			));
		}
	}
}
