package jas.legacy.world;

import jas.modern.DefaultProps;

import java.io.File;

import com.google.gson.annotations.SerializedName;

public class SavedFolderConfiguration {
    @SerializedName("FILE_VERSION")
    public String fileVersion;
    public final String _IMPORTANT = "# DO NOT TOUCH. Internally used to remember how the Configuration file was actually saved.";
    /* Marks how the Entity CFG settings should be saved */
    @SerializedName("Use_Universal_Entity_CFG")
    public Boolean universalDirectory;

    public SavedFolderConfiguration() {
        this(false);
    }

    public SavedFolderConfiguration(boolean universalDirectory) {
        this.universalDirectory = universalDirectory;
        this.fileVersion = "1.0";
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/" + "temporarySaveSettings.cfg");
    }
}
