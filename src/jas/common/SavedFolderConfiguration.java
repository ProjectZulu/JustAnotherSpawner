package jas.common;

import java.io.File;

import com.google.gson.annotations.SerializedName;

public class SavedFolderConfiguration {
    public final String _IMPORTANT = "# DO NOT TOUCH. Internally used to remember how the Configuration file was actually saved.";
    /* Marks how the Entity CFG settings should be saved */
    @SerializedName("Use_Universal_Entity_CFG")
    public Boolean universalDirectory;

    public SavedFolderConfiguration() {
        this(false);
    }

    public SavedFolderConfiguration(boolean universalDirectory) {
        this.universalDirectory = universalDirectory;
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/" + DefaultProps.ENTITYSUBDIR
                + "temporarySaveSettings.cfg");
    }
}
