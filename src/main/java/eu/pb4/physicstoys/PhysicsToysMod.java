package eu.pb4.physicstoys;

import com.mojang.logging.LogUtils;
import eu.pb4.physicstoys.registry.USRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class PhysicsToysMod implements ModInitializer {
    public static final String MOD_ID = "physics_toys";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        USRegistry.register();
    }
}
