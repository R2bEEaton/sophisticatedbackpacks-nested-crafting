package cc.spea.sbac.client;

import cc.spea.sbac.NestedCraftingUpgradeContainer;
import cc.spea.sbac.Sbac;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;

@EventBusSubscriber(modid = Sbac.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SbacClient {
	private SbacClient() {}

	@SubscribeEvent
	public static void onMenuScreenRegister(RegisterMenuScreensEvent event) {
		UpgradeGuiManager.registerTab(NestedCraftingUpgradeContainer.TYPE, NestedCraftingUpgradeTab::new);
	}
}
