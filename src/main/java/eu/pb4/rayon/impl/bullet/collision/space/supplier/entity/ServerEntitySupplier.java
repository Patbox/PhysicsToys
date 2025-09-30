package eu.pb4.rayon.impl.bullet.collision.space.supplier.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class ServerEntitySupplier implements EntitySupplier {
    @Override
    public GameMode getGameType(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            return serverPlayer.getEntityWorld().getServer().getPlayerInteractionManager(serverPlayer).getGameMode();
        }
        return GameMode.SURVIVAL;
    }
}
