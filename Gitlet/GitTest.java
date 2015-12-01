import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.io.IOException;
import java.io.File;

public class GitTest {

    @Before
    public void setUp() throws IOException {
        File f = new File(".gitlet");
        if (f.exists()) {
            FileUtil.recursiveDelete(f);
        }
        Gitlet.main(new String[] {"init"});
    }

    @After
    public void deleteDirectories() {
        File tempDir = new File("tempDir");
        FileUtil.recursiveDelete(tempDir);
        File checkingOut = new File("checkingOut");
        FileUtil.recursiveDelete(checkingOut);
        File gitlet = new File(".gitlet");
        FileUtil.recursiveDelete(gitlet);
        System.out.println();
    }

    @Test
    public void testBasics() throws IOException {
        File original = new File("wug.txt");
        File copy = new File("./.gitlet/0/1/wug.txt");
        assertEquals(false, copy.exists());
        Gitlet.main(new String[] {"add", "wug.txt"});
        Gitlet.main(new String[] {"commit", "YOLO"});
        assertEquals(true, copy.exists());
        assertEquals(FileUtil.getText(original.toString()), FileUtil.getText(copy.toString()));

        //checkout
        File checkingOut = new File("checkingOut");
        checkingOut.mkdir();
        File source = new File("test1/test2/test3/hia.txt");
        File test1 = new File(checkingOut, "test1");
        File test2 = new File(test1, "test2");
        File test3 = new File(test2, "test3");
        assertEquals(false, test1.exists());
        assertEquals(false, test2.exists());
        assertEquals(false, test3.exists());
        FileUtil.makeRecursiveDirectory(checkingOut, source);
        File fullFilePathInWD = new File(checkingOut, source.toString());
        fullFilePathInWD.createNewFile();
        assertEquals(true, test1.exists());
        assertEquals(true, test2.exists());
        assertEquals(true, test3.exists());
        File commitedFile = new File(".gitlet/0/1/2/checkingOut/test1/test2/test3/hia.txt");
        assertEquals(false, commitedFile.exists());
        Gitlet.main(new String[] {"add", "checkingOut/test1/test2/test3/hia.txt"});
        Gitlet.main(new String[] {"commit", "testing that .gitlet has multiple directories"});
        assertEquals(true, commitedFile.exists());

        //now change fullFilePathInWD
        FileUtil.createFile(fullFilePathInWD.toString(), "modified hia.txt");
        assertEquals("modified hia.txt", FileUtil.getText(fullFilePathInWD.toString()));
        //checkout empty hia.txt 
        File gitlet = new File(".gitlet/0/1/2");
        Gitlet.main(new String[] {"checkout", fullFilePathInWD.toString()});
        assertEquals("", FileUtil.getText(fullFilePathInWD.toString()));
    }

    @Test
    public void testCheckout() {
        File hey = new File("hey/hey.txt");
        FileUtil.createFile(hey.toString(), "hey");
        File gitHey = new File(".gitlet/0/1/hey/hey.txt");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey.txt"});
        assertEquals(true, gitHey.exists());
        Gitlet.main(new String[] {"add", "wug.txt"});
        Gitlet.main(new String[] {"commit", "added wug"});
        assertEquals(FileUtil.getText(hey.toString()), FileUtil.getText(gitHey.toString()));

        FileUtil.createFile(hey.toString(), "hey 2");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey 2.txt"});
        File gitHey2 = new File(".gitlet/0/1/2/3/hey/hey.txt");
        assertEquals(FileUtil.getText(hey.toString()), FileUtil.getText(gitHey2.toString()));
        assertEquals("hey 2", FileUtil.getText(gitHey2.toString()));
        Gitlet.main(new String[] {"checkout", "1", hey.toString()});
        assertEquals("hey", FileUtil.getText(hey.toString()));

        //now change the contents of hey/hey.txt
    }

    @Test
    public void testCopyFile() throws IOException {
        FileUtil.createFile("tester.txt", "THIS is a test");
        FileUtil.createFile("../tester.txt", "NULL");
        File source = new File("tester.txt");
        File target = new File("../tester.txt");
        assertEquals("NULL", FileUtil.getText(target.toString()));
        assertEquals("THIS is a test", FileUtil.getText(source.toString()));

        FileUtil.copyFile(source, target);
        assertEquals("THIS is a test", FileUtil.getText(target.toString()));
        assertEquals(FileUtil.getText(target.toString()), FileUtil.getText(source.toString()));
        source.delete();
        target.delete();

        //layeredFile
        File tempDir = new File("tempDir");
        tempDir.mkdir();
        source = new File("test1/test2/test3/hia.txt");
        File test1 = new File(tempDir, "test1");
        File test2 = new File(test1, "test2");
        File test3 = new File(test2, "test3");
        assertEquals(false, test1.exists());
        assertEquals(false, test2.exists());
        assertEquals(false, test3.exists());
        FileUtil.makeRecursiveDirectory(tempDir, source);
        assertEquals(true, test1.exists());
        assertEquals(true, test2.exists());
        assertEquals(true, test3.exists());
    }        

    @Test
    public void testCheckoutBranch() {
        File hey = new File("hey/hey.txt");
        FileUtil.createFile(hey.toString(), "hey");
        File gitHey = new File(".gitlet/0/1/hey/hey.txt");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey.txt"});
        Gitlet.main(new String[] {"branch", "Anthony"}); 
        Gitlet.main(new String[] {"checkout", "Anthony"}); 
        assertEquals("hey", FileUtil.getText(hey.toString()));
        //change hey.txt in Anthony branch
        FileUtil.createFile(hey.toString(), "this is not a hey");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey.txt"});
        assertEquals("this is not a hey", FileUtil.getText(hey.toString()));
        //make sure the change was not saved to master branch
        Gitlet.main(new String[] {"checkout", "master"});
        assertEquals("hey", FileUtil.getText(hey.toString()));

        //Make sure that deleting files from another branch doesn't affect original branch
        File heyman = new File("heyman.txt");
        FileUtil.createFile(heyman.toString(), "heyman");
        Gitlet.main(new String[] {"add", heyman.toString()});
        Gitlet.main(new String[] {"commit", "contains heyman.txt"});
        Gitlet.main(new String[] {"checkout", "Anthony"}); 
        FileUtil.recursiveDelete(heyman);
        assertEquals(false, heyman.exists());
        Gitlet.main(new String[] {"checkout", "master"});
        assertEquals("heyman", FileUtil.getText(heyman.toString()));


        //TESTING REBASE
        File wug = new File("wug.txt");
        FileUtil.createFile(wug.toString(), "this is a wug.");
        Gitlet.main(new String[] {"add", wug.toString()});
        Gitlet.main(new String[] {"commit", "contains wug.txt"});
        assertEquals("hey", FileUtil.getText(hey.toString()));
        Gitlet.main(new String[] {"rebase", "Anthony"});
        assertEquals("this is not a hey", FileUtil.getText(hey.toString()));
        File wugRebased = new File(".gitlet/0/1/3/4/wug.txt");
        File heymanRebased = new File(".gitlet/0/1/3/heyman.txt");
        assertEquals(true, wugRebased.exists());
        assertEquals(true, heymanRebased.exists());
        assertEquals("this is a wug.", FileUtil.getText(wugRebased.toString()));
        assertEquals("heyman", FileUtil.getText(heymanRebased.toString()));
        assertEquals("this is not a hey", FileUtil.getText(hey.toString()));
    }

    @Test
    public void testRebaseSpecialCase() {
        File hey = new File("hey/hey.txt");
        File wug = new File("wug.txt");
        File heyman = new File("heyman.txt");
        FileUtil.createFile(hey.toString(), "hey");
        FileUtil.createFile(heyman.toString(), "heymanYOMAMA");
        FileUtil.createFile(wug.toString(), "this is a wug.YOMAMA");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"add", heyman.toString()});
        Gitlet.main(new String[] {"add", wug.toString()});
        Gitlet.main(new String[] {"commit", "contains hey.txt, heyman.txt, wug.txt"});
        Gitlet.main(new String[] {"branch", "Anthony"}); 

        FileUtil.createFile(heyman.toString(), "heyman");
        Gitlet.main(new String[] {"add", heyman.toString()});
        Gitlet.main(new String[] {"commit", "contains heyman.txt"}); //ERRORING RIGHT NOW
        FileUtil.createFile(wug.toString(), "this is a wug.");       
        Gitlet.main(new String[] {"add", wug.toString()});
        Gitlet.main(new String[] {"commit", "contains wug.txt"});
        Gitlet.main(new String[] {"checkout", "Anthony"}); 
        assertEquals("heymanYOMAMA", FileUtil.getText(heyman.toString()));
        assertEquals("this is a wug.YOMAMA", FileUtil.getText(wug.toString()));
        Gitlet.main(new String[] {"rebase", "master"});
        assertEquals("heyman", FileUtil.getText(heyman.toString()));
        assertEquals("this is a wug.", FileUtil.getText(wug.toString()));
    }

    @Test
    public void testReset() { 
        File hey = new File("hey/hey.txt");
        FileUtil.createFile(hey.toString(), "hey");
        File gitHey = new File(".gitlet/0/1/hey/hey.txt");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey"});
        FileUtil.createFile(hey.toString(), "hey 2");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey 2"});
        FileUtil.createFile(hey.toString(), "hey 3");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey 3"});
        assertEquals("hey 3", FileUtil.getText(hey.toString()));
        Gitlet.main(new String[] {"reset", "1"});
        assertEquals("hey", FileUtil.getText(hey.toString()));

    }

    @Test
    public void testMerge() {
        File hey = new File("hey/hey.txt");
        FileUtil.createFile(hey.toString(), "hey");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey"});

        //test modified
        Gitlet.main(new String[] {"branch", "Anthony"}); 
        Gitlet.main(new String[] {"checkout", "Anthony"}); 
        FileUtil.createFile(hey.toString(), "modified hey");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains modified hey"});
        Gitlet.main(new String[] {"checkout", "master"});
        assertEquals("hey", FileUtil.getText(hey.toString()));
        Gitlet.main(new String[] {"merge", "Anthony"});
        assertEquals("modified hey", FileUtil.getText(hey.toString()));

        //test adding a file
        FileUtil.createFile("tester.txt", "test");
        File tester = new File("tester.txt");
        Gitlet.main(new String[] {"add", "tester.txt"});
        Gitlet.main(new String[] {"commit", "contains test"});
        Gitlet.main(new String[] {"checkout", "Anthony"}); 
        System.out.print("The following should print an error: ");
        Gitlet.main(new String[] {"checkout", "tester.txt"}); //SHOULD PRINT AN ERROR 
        Gitlet.main(new String[] {"merge", "master"});
        System.out.print("The following should be blank: ");
        Gitlet.main(new String[] {"checkout", "tester.txt"});
        System.out.println("blank");

        //test conflicted file
        FileUtil.createFile(hey.toString(), "hey");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey"});
        Gitlet.main(new String[] {"checkout", "master"});
        FileUtil.createFile(hey.toString(), "this is not a hey");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "this is not a hey"});
        assertEquals("this is not a hey", FileUtil.getText(hey.toString()));
        File conflicted = new File("hey/hey.txt.conflicted");
        assertEquals(false, conflicted.exists());
        Gitlet.main(new String[] {"merge", "Anthony"});
        assertEquals("this is not a hey", FileUtil.getText(hey.toString()));
        assertEquals(true, conflicted.exists());

        conflicted.delete();
        tester.delete();
    }

    @Test
    public void testModification() {
        File hey = new File("hey/hey.txt");
        FileUtil.createFile(hey.toString(), "hey");
        Gitlet.main(new String[] {"add", hey.toString()});
        Gitlet.main(new String[] {"commit", "contains hey"});

        //this shouldn't allow us to add and should print 2 messages
        System.out.println("Following should print 2 messages:");
        Gitlet.main(new String[] {"add", hey.toString()}); 
        FileUtil.createFile(hey.toString(), "hey");
        Gitlet.main(new String[] {"add", hey.toString()}); 
    }

    public static void main(String[] args) {
        jh61b.junit.textui.runClasses(GitTest.class);
    }

}
