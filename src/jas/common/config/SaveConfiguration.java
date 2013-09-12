package jas.common.config;

import jas.common.DefaultProps;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class SaveConfiguration extends Configuration {

    public static final String CONFIG_CATEGORY = "Save_Configuration";
    public static final String OS_CHAR_WARNING = "Case Sensitive if OS allows. Beware invalid OS characters.";

    public SaveConfiguration(File configDirectory) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + "SaveConfig.cfg"));
    }

    public Property getImportName() {
        return this.get(CONFIG_CATEGORY, "Import_Name", "", "Folder name to Copy Missing Files From. "
                + OS_CHAR_WARNING);
    }

    public Property getDefaultSaveName() {
        return this.get(CONFIG_CATEGORY, "Default Save_Name", "{$world}", "Default name used for Save_Name. "
                + OS_CHAR_WARNING);
    }

    public Property getLocalSaveName(String curWorldName, String defaultSaveName) {
        return this.get(CONFIG_CATEGORY + "." + curWorldName, "Save_Name", defaultSaveName,
                "Folder name to look for and generate CFG files. " + OS_CHAR_WARNING);
    }

    public Property getLocalSortByCreature(String curWorldName, boolean defaultSortByCreature) {
        return this.get(CONFIG_CATEGORY + "." + curWorldName, "Sort Creature By Biome - Setting",
                defaultSortByCreature, "Determines if Entity CFGs are sorted internally by Entity or Biome.");
    }

    public Property getLocalSortUseUniversal(String curWorldName, boolean defaultUseUniversal) {
        return this.get(CONFIG_CATEGORY + "." + curWorldName, "Universal Entity CFG - Settings", defaultUseUniversal,
                "Specifies if the User wants the Entity CFG to Combined into a Universal CFG.");
    }
}