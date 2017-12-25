package test.utilities;

import java.io.File;
import java.util.Objects;

/**
 * Class responsible for clearing and creating a temporary test folder.
 *
 * @author Rsl1122
 */
public class TestFolderUtility {

    private static final String path = getPath();

    private static String getPath() {
        return TestFolderUtility.class.getClass().getResource("/test-folder").getPath();
    }

    public static boolean clearFolder() {
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
        File folder = getFolder();
        return folder.mkdirs();
    }

    /**
     * Delete files recursively.
     *
     * @param file File or Folder to delete
     * @return
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
