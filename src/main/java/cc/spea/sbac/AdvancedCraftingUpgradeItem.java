package cc.spea.sbac;

import net.p3pp3rf1y.sophisticatedbackpacks.Config;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;

public class AdvancedCraftingUpgradeItem extends CraftingUpgradeItem {
	private static final UpgradeType<CraftingUpgradeWrapper> TYPE = new UpgradeType<>(NestedCraftingUpgradeWrapper::new);

	public AdvancedCraftingUpgradeItem() {
		super(Config.SERVER.maxUpgradesPerStorage);
	}

	@Override
	public UpgradeType<CraftingUpgradeWrapper> getType() {
		return TYPE;
	}
}
