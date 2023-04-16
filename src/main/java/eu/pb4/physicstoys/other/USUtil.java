package eu.pb4.physicstoys.other;

import eu.pb4.physicstoys.PhysicsToysMod;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class USUtil {

    public static final Predicate<ItemStack> ALWAYS_ALLOW = (x) -> true;

    public static boolean areStacksMatching(ItemStack a, ItemStack b) {
        if (a.getItem() != b.getItem() || a.hasNbt() != b.hasNbt() || a.hasEnchantments() != b.hasEnchantments()) {
            return false;
        }

        if (!a.hasNbt()) {
            return true;
        }


        if (a.getDamage() == b.getDamage() && a.hasEnchantments() && !EnchantmentHelper.get(a).equals(EnchantmentHelper.get(b))) {
            return false;
        }

        {
            var storedEnchA = EnchantedBookItem.getEnchantmentNbt(a);
            var storedEnchB = EnchantedBookItem.getEnchantmentNbt(b);

            if (storedEnchA.size() != storedEnchB.size() || !EnchantmentHelper.fromNbt(storedEnchA).equals(EnchantmentHelper.fromNbt(storedEnchB))) {
                return false;
            }
        }


        var aNbt = a.getNbt().copy();
        var bNbt = b.getNbt().copy();

        clearCheckedNbt(aNbt);
        clearCheckedNbt(bNbt);

        return aNbt.equals(bNbt);
    }

    private static void clearCheckedNbt(NbtCompound nbt) {
        nbt.remove(ItemStack.ENCHANTMENTS_KEY);
        nbt.remove(ItemStack.DAMAGE_KEY);
        nbt.remove(EnchantedBookItem.STORED_ENCHANTMENTS_KEY);
    }

    public static int transfer(Inventory inventory, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun, Predicate<ItemStack> consumer) {
        if (maxCount < 1) {
            return 0;
        }

        int i = 0;

        for (int j = 0; j < inventory.size(); ++j) {
            ItemStack itemStack = inventory.getStack(j);
            int k = transfer(itemStack, shouldRemove, maxCount - i, dryRun, consumer);
            if (k > 0 && !dryRun && itemStack.isEmpty()) {
                inventory.setStack(j, ItemStack.EMPTY);
            }

            i += k;
        }
        if (i != 0) {
            inventory.markDirty();
        }

        return i;
    }

    private static int transfer(ItemStack stack, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun, Predicate<ItemStack> consumer) {
        if (!stack.isEmpty() && shouldRemove.test(stack)) {
            int i = Math.min(maxCount, stack.getCount());
            if (i != 0) {
                var copy = stack.copy();
                copy.setCount(i);
                if (!consumer.test(copy)) {
                    return 0;
                }

                if (!dryRun) {
                    stack.decrement(i);
                }
            }
            return i;
        } else {
            return 0;
        }
    }

    @Nullable
    public static Inventory getInventoryAt(World world, BlockPos pos) {
        return HopperBlockEntity.getInventoryAt(world, pos);
    }

    public static Text asText(ItemStack value) {
        return value.getCount() == 1 ? value.getName() : Text.empty().append(value.getName()).append(" × " + value.getCount());
    }

    public static SimpleInventory copyInventory(Inventory input) {
        var out = new SimpleInventory(input.size());
        for (int i = 0; i < input.size(); i++) {
            out.setStack(i, input.getStack(i).copy());
        }
        return out;
    }

    public static boolean checkAdmin(PlayerEntity player, String permission) {
        return Permissions.check(player, PhysicsToysMod.MOD_ID + "." + permission, 3);
    }

    public static Predicate<ServerCommandSource> requireAdmin(String permission) {
        return Permissions.require(PhysicsToysMod.MOD_ID + "." + permission, 3);
    }

    public static boolean checkDefault(PlayerEntity player, String permission) {
        return Permissions.check(player, PhysicsToysMod.MOD_ID + "." + permission, true);
    }

    public static Predicate<ServerCommandSource> requireDefault(String permission) {
        return Permissions.require(PhysicsToysMod.MOD_ID + "." + permission, true);
    }

    public static SimpleInventory copyInventory(DefaultedList<ItemStack> input) {
        var out = new SimpleInventory(input.size());
        for (int i = 0; i < input.size(); i++) {
            out.setStack(i, input.get(i).copy());
        }
        return out;
    }

    public static Predicate<ItemStack> addToInventory(Inventory inventory) {
        return (i) -> addStack(inventory, i).isEmpty();
    }

    public static Predicate<ItemStack> addToInventory(DefaultedList<ItemStack> inventory) {
        return (i) -> addStack(inventory, i).isEmpty();
    }

    public static ItemStack addStack(Inventory inv, ItemStack stack) {
        return addStack(inv::getStack, inv::setStack, inv.size(), inv.getMaxCountPerStack(), stack);
    }

    public static ItemStack addStack(DefaultedList<ItemStack> inv, ItemStack stack) {
        return addStack(inv::get, inv::set, inv.size(), 64, stack);
    }

    private static ItemStack addStack(IntFunction<ItemStack> getter, SlotSetter setter, int size, int maxStackSize, ItemStack stack) {
        ItemStack stackCopy = stack.copy();
        // Insert to existing
        for(int i = 0; i < size; ++i) {
            var itemStack = getter.apply(i);
            if (ItemStack.canCombine(itemStack, stackCopy)) {
                transfer(maxStackSize, stackCopy, itemStack);
                if (stackCopy.isEmpty()) {
                    break;
                }
            }
        }

        if (stackCopy.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            for(int i = 0; i < size; ++i) {
                ItemStack itemStack = getter.apply(i);
                if (itemStack.isEmpty()) {
                    setter.set(i, stackCopy.copy());
                    stackCopy.setCount(0);
                    break;
                }
            }


            return stackCopy.isEmpty() ? ItemStack.EMPTY : stackCopy;
        }
    }

    private static void transfer(int maxStackSize, ItemStack source, ItemStack target) {
        int i = Math.min(maxStackSize, target.getMaxCount());
        int j = Math.min(source.getCount(), i - target.getCount());
        if (j > 0) {
            target.increment(j);
            source.decrement(j);
        }
    }

    public static Predicate<ItemStack> mergeIntoCursor(ScreenHandler handler) {
        return (stack) -> {
            if (handler.getCursorStack().isEmpty()) {
                handler.setCursorStack(stack.copy());
                stack.setCount(0);
                return true;
            } else if (ItemStack.canCombine(stack, handler.getCursorStack())) {
                var count = stack.getCount() + handler.getCursorStack().getCount();

                if (count > handler.getCursorStack().getMaxCount()) {
                    return false;
                } else {
                    handler.getCursorStack().setCount(count);
                    stack.setCount(0);
                    return true;
                }
            }

            return false;
        };
    }

    public static void playUiSound(ServerPlayerEntity player, SoundEvent event) {
        player.playSound(event, SoundCategory.MASTER, 0.8f, 1);
    }

    public static boolean canInsert(Inventory chest, ItemStack value, int count) {
        return canInsert(chest::getStack, chest.size(), value, count);
    }

    public static boolean canInsert(DefaultedList<ItemStack> chest, ItemStack value, int count) {
        return canInsert(chest::get, chest.size(), value, count);
    }


    private static boolean canInsert(IntFunction<ItemStack> getter, int size, ItemStack value, int count) {
        for (int i = 0; i < size; i++) {
            var stack = getter.apply(i);

            if (stack.isEmpty()) {
                return true;
            }

            if (ItemStack.canCombine(stack, value)) {
                count -= (stack.getMaxCount() - stack.getCount());

                if (count <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Iterable<ItemStack> iterable(Inventory container) {
        return () -> new Iterator<>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < container.size();
            }

            @Override
            public ItemStack next() {
                return container.getStack(i++);
            }
        };
    }

    public interface SlotSetter {
        void set(int i, ItemStack stack);
    }
}
