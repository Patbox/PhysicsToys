package dev.lazurite.rayon.impl;

import dev.lazurite.rayon.impl.bullet.natives.NativeLoader;
import dev.lazurite.rayon.impl.event.ServerEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Rayon {
	public static final String MODID = "rayon";
	public static final Logger LOGGER = LogManager.getLogger("Rayon");

	public static void initialize() {
		// prevent annoying libbulletjme spam
		java.util.logging.LogManager.getLogManager().reset();

		NativeLoader.load();
		ServerEventHandler.register();
	}
}