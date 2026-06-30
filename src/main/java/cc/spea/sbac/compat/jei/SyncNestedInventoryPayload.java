package cc.spea.sbac.compat.jei;

import cc.spea.sbac.Sbac;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Sent server→client when a BackpackContainer opens so the JEI transfer handler
 * can accurately check ingredient availability against nested backpack contents.
 * Carries both all processing items and the memory-whitelisted source items.
 */
public record SyncNestedInventoryPayload(List<ItemStack> processingItems, List<ItemStack> whitelistedProcessingItems) implements CustomPacketPayload {

    public static final Type<SyncNestedInventoryPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Sbac.MOD_ID, "sync_nested_inventory")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncNestedInventoryPayload> STREAM_CODEC =
        StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), SyncNestedInventoryPayload::processingItems,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), SyncNestedInventoryPayload::whitelistedProcessingItems,
            SyncNestedInventoryPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Client-side cache — replaced each time the player opens a backpack
    public static volatile List<ItemStack> clientProcessingItems = List.of();
    public static volatile List<ItemStack> clientWhitelistedProcessingItems = List.of();

    public static void handle(SyncNestedInventoryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            clientProcessingItems = payload.processingItems();
            clientWhitelistedProcessingItems = payload.whitelistedProcessingItems();
        });
    }
}
