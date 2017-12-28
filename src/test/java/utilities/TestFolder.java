package utilities;

import java.io.File;
import java.util.Objects;

/**
 * Class responsible for clearing and creating a temporary test folder.
 *
 * @author Rsl1122
 */
public class TestFolder {

    private static final String path = getPath();

    private static String getPath() {
        File configFile = new File(TestFolder.class.getResource("/config.yml").getPath());
        File targetFolder = configFile.getParentFile().getParentFile();
        File testFolder = new File(targetFolder, "test-folder");
        return testFolder.getPath();
    }

    public static boolean clearFolder() {
        System.out.println("Clearing Folder: " + path);
        File folder = getFolder();
        if (folder.exists()) {
            return clear(folder);
        }
        return true;
    }

    public static File getFolder() {
        return new File(path);
    }

    public static boolean createFolder() {
        System.out.println("Creating Folder: " + path);
        File folder = getFolder();
        return folder.mkdirs();
    }

    /**
     * Delete files recursively.
     *
     * @param file File or Folder to delete
     * @return File was removed successfully.
     */
    private static boolean clear(File file) {
        if (file.isDirectory()) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                clear(subFile);
            }
        }
        return file.delete();
    }

}
