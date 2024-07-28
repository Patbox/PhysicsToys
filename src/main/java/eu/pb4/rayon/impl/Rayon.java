package eu.pb4.rayon.impl;

import eu.pb4.rayon.impl.bullet.natives.NativeLoader;
import eu.pb4.rayon.impl.event.ServerEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Rayon {
	public static final String MODID = "rayon";
	public static final Logger LOGGER = LogManager.getLogger("PbRayon");

	public static void initialize() {
		// prevent annoying libbulletjme spam
		java.util.logging.LogManager.getLogManager().reset();

		NativeLoader.load();
		ServerEventHandler.register();
	}
}