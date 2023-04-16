package eu.pb4.physicstoys.other;

import eu.pb4.physicstoys.PhysicsToysMod;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtil {

    public static MutableText of(String type, String path, Object... args) {
        return Text.translatable(type + "." + PhysicsToysMod.MOD_ID + "." + path, args);
    }

    public static MutableText text(String path, Object... args) {
        return of("text", path, args);
    }

    public static MutableText gui(String path, Object... args) {
        return of("gui", path, args);
    }

    public static MutableText number(int value) {
        return Text.literal(value == Integer.MAX_VALUE ? "∞" : Integer.toString(value));
    }

    public static Text prefix(Text text) {
        return Text.empty()
                .append(Text.literal("[").formatted(Formatting.DARK_GRAY))
                .append(text("prefix").setStyle(Style.EMPTY.withColor(0xfcc488)))
                .append(Text.literal("]").formatted(Formatting.DARK_GRAY))


                .append(" ").append(text);
    }
}
