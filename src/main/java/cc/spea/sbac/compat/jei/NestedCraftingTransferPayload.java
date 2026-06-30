package cc.spea.sbac.compat.jei;

import cc.spea.sbac.NestedCraftingSourceHelper;
import cc.spea.sbac.NestedCraftingUpgradeWrapper;
import cc.spea.sbac.Sbac;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ICraftingContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

import java.util.List;

/**
 * Sent client→server to fill the crafting upgrade grid using items from all available
 * inventory including nested backpacks (via getInventoryForUpgradeProcessing()).
 * Used by NestedCraftingJeiTransferHandler when the visible inventory alone can't
 * satisfy the recipe but nested backpack items may cover the gap.
 */
public record NestedCraftingTransferPayload(ResourceLocation recipeId) implements CustomPacketPayload {

    public static final Type<NestedCraftingTransferPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Sbac.MOD_ID, "nested_crafting_transfer")
    );

    public static final StreamCodec<ByteBuf, NestedCraftingTransferPayload> STREAM_CODEC =
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, NestedCraftingTransferPayload::recipeId,
            NestedCraftingTransferPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NestedCraftingTransferPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player.containerMenu instanceof BackpackContainer backpackContainer)) return;

            backpackContainer.getOpenOrFirstCraftingContainer(RecipeType.CRAFTING)
                .ifPresent(craftingContainerBase -> {
                    if (!(craftingContainerBase instanceof ICraftingContainer craftingContainer)) return;
                    UpgradeContainerBase<?, ?> upgradeContainer = (UpgradeContainerBase<?, ?>) craftingContainerBase;

                    player.level().getRecipeManager().byKey(payload.recipeId()).ifPresent(recipeHolder -> {
                        if (!(recipeHolder.value() instanceof CraftingRecipe craftingRecipe)) return;

                        // Switch to the crafting upgrade tab
                        backpackContainer.getOpenContainer().ifPresent(open -> open.setIsOpen(false));
                        upgradeContainer.setIsOpen(true);
                        backpackContainer.setOpenTabId(upgradeContainer.getUpgradeContainerId());

                        List<Slot> craftingSlots = craftingContainer.getRecipeSlots();

                        // Return any existing grid items to the player before refilling
                        for (Slot slot : craftingSlots) {
                            ItemStack current = slot.getItem();
                            if (!current.isEmpty()) {
                                slot.set(ItemStack.EMPTY);
                                if (!player.getInventory().add(current)) {
                                    player.drop(current, false);
                                }
                            }
                        }

                        // Fill each recipe slot: try nested+outer inventory first, then player inventory
                        List<Ingredient> ingredients = craftingRecipe.getIngredients();
                        for (int i = 0; i < ingredients.size() && i < craftingSlots.size(); i++) {
                            Ingredient ingredient = ingredients.get(i);
                            if (ingredient.isEmpty()) continue;

                            ItemStack extracted;
                            if (upgradeContainer.getUpgradeWrapper() instanceof NestedCraftingUpgradeWrapper nestedWrapper) {
                                extracted = NestedCraftingSourceHelper.extractFromCraftingSources(nestedWrapper, player, ingredient::test);
                            } else {
                                extracted = InventoryHelper.extractFromInventory(
                                    ingredient::test, 1, backpackContainer.getStorageWrapper().getInventoryForUpgradeProcessing(), false
                                );
                                if (extracted.isEmpty()) {
                                    for (int j = 0; j < player.getInventory().getContainerSize(); j++) {
                                        ItemStack playerItem = player.getInventory().getItem(j);
                                        if (!playerItem.isEmpty() && ingredient.test(playerItem)) {
                                            extracted = playerItem.split(1);
                                            player.getInventory().setItem(j, playerItem.isEmpty() ? ItemStack.EMPTY : playerItem);
                                            break;
                                        }
                                    }
                                }
                            }

                            if (!extracted.isEmpty()) {
                                craftingSlots.get(i).set(extracted);
                            }
                        }

                        backpackContainer.broadcastChanges();
                    });
                });
        });
    }
}
