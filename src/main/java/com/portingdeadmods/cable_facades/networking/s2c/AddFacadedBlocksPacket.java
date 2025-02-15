package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record AddFacadedBlocksPacket(ChunkPos chunkPos, Map<BlockPos, Block> facadedBlocks) {
    public AddFacadedBlocksPacket(FriendlyByteBuf buf) {
        this(buf.readChunkPos(), SyncFacadedBlocks.getFacades(buf));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeChunkPos(this.chunkPos);
        buf.writeInt(this.facadedBlocks.size());
        for (Map.Entry<BlockPos, Block> entry : this.facadedBlocks.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            //noinspection deprecation
            buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(entry.getValue()));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (!ClientFacadeManager.LOADED_BLOCKS.containsKey(this.chunkPos)) {
                ClientFacadeManager.FACADED_BLOCKS.putAll(this.facadedBlocks);
                ClientFacadeManager.LOADED_BLOCKS.put(this.chunkPos, this.facadedBlocks.keySet().stream().toList());
            }
        });
    }
}
