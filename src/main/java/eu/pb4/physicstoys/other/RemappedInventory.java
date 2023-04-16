package eu.pb4.physicstoys.other;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface RemappedInventory extends Inventory {
    Inventory getInventory();

    @Override
    default int size() {
        return this.getInventory().size();
    }

    @Override
    default boolean isEmpty() {
        return this.getInventory().isEmpty();
    }

    @Override
    default ItemStack getStack(int slot) {
        return this.getInventory().getStack(slot);
    }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        return this.getInventory().removeStack(slot, amount);
    }

    @Override
    default ItemStack removeStack(int slot) {
        return this.getInventory().removeStack(slot);
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        this.getInventory().setStack(slot, stack);
    }

    @Override
    default void markDirty() {
        this.getInventory().markDirty();
    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return this.getInventory().canPlayerUse(player);
    }

    @Override
    default void clear() {
        this.getInventory().clear();
    }

    @Override
    default int getMaxCountPerStack() {
        return this.getInventory().getMaxCountPerStack();
    }
}
