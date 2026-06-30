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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NestedCraftingJeiTransferHandler extends JeiCraftingContainerRecipeTransferHandlerBase<BackpackContainer, RecipeHolder<CraftingRecipe>> {

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
        IRecipeTransferError baseResult = super.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, false);

        // Propagate internal errors (no crafting upgrade found, container mismatch, etc.)
        if (baseResult != null && baseResult.getType() == IRecipeTransferError.Type.INTERNAL) {
            return baseResult;
        }

        if (doTransfer) {
            if (baseResult == null) {
                // All items in visible inventory — use the standard JEI transfer path
                return super.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, true);
            } else {
                // Items missing from visible inventory — server fills from nested backpacks
                // via getInventoryForUpgradeProcessing() which traverses inception upgrades
                PacketDistributor.sendToServer(new NestedCraftingTransferPayload(recipe.id()));
                return null;
            }
        }

        // doTransfer=false: return the base result so JEI shows an accurate button state.
        // null = green (all items visible), user error = orange/red (some items missing from
        // visible inventory — may still be in nested backpacks, clicking will attempt that).
        return baseResult;
    }
}
