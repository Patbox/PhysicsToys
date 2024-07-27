package dev.lazurite.toolbox.api.event;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ServerEvents {
    public static class Block {
        public static final Event<BlockUpdate> BLOCK_UPDATE = Event.create();

        @FunctionalInterface
        public interface BlockUpdate {
            void onBlockUpdate(World level, BlockState blockState, BlockPos blockPos);
        }
    }
}
