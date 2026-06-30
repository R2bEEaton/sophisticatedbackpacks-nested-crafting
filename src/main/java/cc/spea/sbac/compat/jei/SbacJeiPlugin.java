package cc.spea.sbac.compat.jei;

import cc.spea.sbac.Sbac;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class SbacJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Sbac.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
            new NestedCraftingJeiTransferHandler(
                registration.getTransferHelper(),
                registration.getJeiHelpers().getStackHelper()
            ),
            RecipeTypes.CRAFTING
        );
    }
}
