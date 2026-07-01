package cc.spea.sbac.compat.jei;

import cc.spea.sbac.NestedCraftingUpgradeContainer;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiCraftingContainerRecipeTransferHandlerBase;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

        // Propagate internal errors (no crafting upgrade, validation failure, etc.)
        if (baseResult != null && baseResult.getType() == IRecipeTransferError.Type.INTERNAL) {
            return baseResult;
        }

        // Nested-backpack refill via JEI requires the advanced crafting upgrade; with only the
        // regular crafting upgrade, fall back to vanilla JEI behavior (visible inventory only).
        boolean hasAdvancedCraftingUpgrade = container.getOpenOrFirstCraftingContainer(net.minecraft.world.item.crafting.RecipeType.CRAFTING)
            .map(craftingContainer -> craftingContainer instanceof NestedCraftingUpgradeContainer)
            .orElse(false);
        if (!hasAdvancedCraftingUpgrade) {
            if (doTransfer && baseResult == null) {
                return super.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, true);
            }
            return baseResult;
        }

        if (doTransfer) {
            if (baseResult == null) {
                return super.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, true);
            } else {
                // Items not all in visible inventory — server extracts from nested backpacks
                PacketDistributor.sendToServer(new NestedCraftingTransferPayload(recipe.id()));
                return null;
            }
        }

        // doTransfer=false: if visible inventory is sufficient, green; otherwise check the
        // server-synced processing inventory (sent when the backpack was opened) which
        // includes nested backpack items accessible via the inception upgrade.
        if (baseResult != null && canSatisfyFromSyncedInventory(container, recipe, player)) {
            return null;
        }
        return baseResult;
    }

    // Checks whether the recipe can be satisfied from the server-synced processing inventory
    // (includes nested backpack items) combined with the player's own inventory.
    private boolean canSatisfyFromSyncedInventory(BackpackContainer container, RecipeHolder<CraftingRecipe> recipe, Player player) {
        List<ItemStack> available = new ArrayList<>();
        boolean useWhitelist = container.getOpenOrFirstCraftingContainer(net.minecraft.world.item.crafting.RecipeType.CRAFTING)
            .map(craftingContainer -> craftingContainer instanceof NestedCraftingUpgradeContainer nestedContainer
                && nestedContainer.shouldUseMemorizedBackpackSlotsForNestedCrafting())
            .orElse(false);
        List<ItemStack> syncedItems = useWhitelist
            ? SyncNestedInventoryPayload.clientWhitelistedProcessingItems
            : SyncNestedInventoryPayload.clientProcessingItems;

        for (ItemStack stack : syncedItems) {
            if (!stack.isEmpty()) available.add(stack.copy());
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) available.add(stack.copy());
        }

        for (Ingredient ingredient : recipe.value().getIngredients()) {
            if (ingredient.isEmpty()) continue;
            boolean found = false;
            for (ItemStack stack : available) {
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    stack.shrink(1);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
}
