import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;


public class Serialize {
    public static void serializeGit(Git g) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(".gitlet/gitlet.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(g);
        out.close();
    }

    public static Git load() {
        try {
            FileInputStream fin = new FileInputStream(".gitlet/gitlet.ser");
            ObjectInputStream ois = new ObjectInputStream(fin);
            Git g = (Git) ois.readObject();
            ois.close();
            return g;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException z) {
            return null;
        }
    }
}
