package jas.modern.profile;

import java.io.File;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.event.FMLServerStartingEvent;

/**
 * Configurable World Settings - Each Profile supports a unique configuration file layout
 */
public interface Profile {
	public void init();
	
	public void serverStart(FMLServerStartingEvent event);

	public void saveToConfig(File configDirectory, World world);

	public void loadFromConfig(File configDirectory, World world);
}
