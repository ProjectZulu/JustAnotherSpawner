package jas.compatability;

import jas.api.CompatibilityRegistrationEvent;
import jas.common.JASLog;
import jas.compatability.tf.TFLoadInfo;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = CompatibilityProps.MODID, name = CompatibilityProps.MODNAME, version = CompatibilityProps.VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class JASCompatability {

	private final List<LoadInfo> modulesInfo = new ArrayList<LoadInfo>();

	@Instance(CompatibilityProps.MODID)
	public static JASCompatability modInstance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		modulesInfo.add(new TFLoadInfo());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void CompatibilityRegistration(CompatibilityRegistrationEvent event) {
		for (LoadInfo moduleInfo : modulesInfo) {
			try {
				if (shouldLoadModule(moduleInfo)) {
					for (Object object : moduleInfo.getObjectsToRegister()) {
						event.loader.registerObject(object);
					}
				}
			} catch (Exception e) {
				JASLog.severe(
						"Failed to load JAS compatability module %s due to %s",
						moduleInfo.loaderID(), e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private boolean shouldLoadModule(LoadInfo moduleInfo) {
		for (String modID : moduleInfo.getRequiredModIDs()) {
			if (!Loader.isModLoaded(modID)) {
				return false;
			}
		}
		return true;
	}
}
