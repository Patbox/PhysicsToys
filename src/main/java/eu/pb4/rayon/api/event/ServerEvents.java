package eu.pb4.rayon.api.event;

import eu.pb4.rayon.api.event.collision.PhysicsSpaceEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ServerEvents {
    public static class Block {
        public static final Event<BlockUpdate> BLOCK_UPDATE = EventFactory.createArrayBacked(BlockUpdate.class, (events) -> (world, blockState, pos) -> {
            for (var e : events) {
                e.onBlockUpdate(world, blockState, pos);
            }
        });

        @FunctionalInterface
        public interface BlockUpdate {
            void onBlockUpdate(World level, BlockState blockState, BlockPos blockPos);
        }
    }
}
