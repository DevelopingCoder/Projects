import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;


//adopted many methods from GitletPublicTest

public class FileUtil {
    public static void copyFile(File source, File target) throws IOException {
        if (!source.exists()) {
            System.out.println("invalid source");
            return;
        }
        byte[] sourceContents = Files.readAllBytes(Paths.get(source.toString()));
        Path endPath = Paths.get(target.toString());
        Files.write(endPath, sourceContents);
    }

    public static void makeRecursiveDirectory(File target, File source) { 
        //source contains a file, target is the directory
        if (source.getParentFile() == null) {
            return; //No more directories needed bc this is a file
        } else {
            String child = "";
            while (source.getParentFile() != null) { //makes source the very top parent
                child = source.getName() + "/" + child;
                source = source.getParentFile();
            }
            File newTarget = new File(target, source.getPath()); 
            if (!newTarget.exists()) { //don't create a new directory if not necessary
                //creates the directory with the parent File
                boolean createSingleDirectory = newTarget.mkdir(); 
            }            
            makeRecursiveDirectory(newTarget, new File(child));            
        }
    }
    public static void recursiveDelete(File d) {
        if (d.isDirectory()) {
            for (File f : d.listFiles()) {
                recursiveDelete(f);
            }
        }
        d.delete();
    }
    public static String getText(String fileName) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
    public static void createFile(String fileName, String fileText) {
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeFile(fileName, fileText);
    }

    /**
     * Replaces all text in the existing file with the given text.
     */
    public static void writeFile(String fileName, String fileText) {
        FileWriter fw = null;
        try {
            File f = new File(fileName);
            fw = new FileWriter(f, false);
            fw.write(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
