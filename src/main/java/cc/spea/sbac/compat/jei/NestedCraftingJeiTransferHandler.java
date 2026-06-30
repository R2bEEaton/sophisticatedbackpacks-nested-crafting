package cc.spea.sbac.compat.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiCraftingContainerRecipeTransferHandlerBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NestedCraftingJeiTransferHandler extends JeiCraftingContainerRecipeTransferHandlerBase<BackpackContainer, RecipeHolder<CraftingRecipe>> {

    private static final Logger LOGGER = LogManager.getLogger();

    public NestedCraftingJeiTransferHandler(IRecipeTransferHandlerHelper transferHelper, IStackHelper stackHelper) {
        super(transferHelper, stackHelper);
    }

    @Override
    public Class<? extends BackpackContainer> getContainerClass() {
        return BackpackContainer.class;
    }

    @Override
    public Optional<MenuType<BackpackContainer>> getMenuType() {
        return Optional.of(ModItems.BACKPACK_CONTAINER_TYPE.get());
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(BackpackContainer container, RecipeHolder<CraftingRecipe> recipe,
                                               IRecipeSlotsView recipeSlotsView, Player player,
                                               boolean maxTransfer, boolean doTransfer) {
        LOGGER.debug("[sbac] transferRecipe called, doTransfer={}", doTransfer);

        IRecipeTransferError baseResult = super.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, false);
        if (baseResult == null) {
            // Visible inventory satisfies the recipe — use the standard transfer path
            if (doTransfer) {
                return super.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, true);
            }
            return null;
        }

        LOGGER.debug("[sbac] Base check error type={}", baseResult.getType());

        // Internal errors mean something structural is wrong (no crafting upgrade found, validation failure).
        // For user-facing / missing-items errors, allow the transfer: the server-side
        // extractFromStorageOrPlayer will pull ingredients from nested backpacks via inception upgrade.
        // We can't check nested inventory client-side because it isn't synced to the client.
        if (baseResult.getType() == IRecipeTransferError.Type.INTERNAL) {
            return baseResult;
        }

        if (doTransfer) {
            // Server-side handler extracts from getInventoryForUpgradeProcessing(),
            // which includes nested backpack items via the inception upgrade.
            PacketDistributor.sendToServer(new NestedCraftingTransferPayload(recipe.id()));
        }
        return null;
    }
}
