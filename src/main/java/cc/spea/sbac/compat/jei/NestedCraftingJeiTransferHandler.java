package cc.spea.sbac.compat.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ICraftingContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiCraftingContainerRecipeTransferHandlerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiTransferRecipePayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            sendPartialTransfer(container, recipe, player, maxTransfer);
        }
        return null;
    }

    // Fills the crafting grid from visible inventory items, leaving slots empty for
    // ingredients that only exist in nested backpacks — the upgrade's extractFromStorageOrPlayer
    // handles those server-side via getInventoryForUpgradeProcessing().
    private void sendPartialTransfer(BackpackContainer container, RecipeHolder<CraftingRecipe> recipe,
                                     Player player, boolean maxTransfer) {
        container.getOpenOrFirstCraftingContainer(recipe.value().getType()).ifPresent(craftingContainerBase -> {
            if (!(craftingContainerBase instanceof ICraftingContainer craftingContainer)) return;
            UpgradeContainerBase<?, ?> upgradeContainer = (UpgradeContainerBase<?, ?>) craftingContainerBase;

            if (!upgradeContainer.isOpen()) {
                container.getOpenContainer().ifPresent(open -> open.setIsOpen(false));
                upgradeContainer.setIsOpen(true);
                container.setOpenTabId(upgradeContainer.getUpgradeContainerId());
            }

            List<Slot> craftingSlots = craftingContainer.getRecipeSlots();
            List<Slot> inventorySlots = container.realInventorySlots;

            List<ItemStack> availableVisible = new ArrayList<>();
            for (Slot slot : inventorySlots) {
                availableVisible.add(slot.getItem().isEmpty() ? ItemStack.EMPTY : slot.getItem().copy());
            }

            List<Ingredient> ingredients = recipe.value().getIngredients();
            Map<Integer, Integer> matchingItems = new HashMap<>();

            for (int i = 0; i < ingredients.size() && i < craftingSlots.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                if (ingredient.isEmpty()) continue;
                Slot craftingSlot = craftingSlots.get(i);

                for (int j = 0; j < inventorySlots.size(); j++) {
                    ItemStack available = availableVisible.get(j);
                    if (!available.isEmpty() && ingredient.test(available)) {
                        matchingItems.put(inventorySlots.get(j).index, craftingSlot.index);
                        available.shrink(1);
                        break;
                    }
                }
                // If not found in visible inventory, omit — upgrade refills from nested backpacks
            }

            List<Integer> craftingSlotIndexes = craftingSlots.stream().map(s -> s.index).sorted().toList();
            List<Integer> inventorySlotIndexes = inventorySlots.stream().map(s -> s.index).sorted().toList();

            ResourceLocation recipeTypeKey = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.value().getType());
            if (recipeTypeKey == null) return;

            PacketDistributor.sendToServer(new JeiTransferRecipePayload(
                recipe.id(), recipeTypeKey, matchingItems,
                craftingSlotIndexes, inventorySlotIndexes, maxTransfer
            ));
        });
    }
}
