
import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Arrays;
import java.nio.file.Files;
import java.util.Scanner;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.io.BufferedReader;
import java.io.FileReader;


//Got printwriter from http://stackoverflow.com/questions/1053467/
//how-do-i-save-a-string-to-a-text-file-using-java

//Timestamp idea from 
//http://www.mkyong.com/java/how-to-get-current-timestamps-in-java/

//Copy files http://stackoverflow.com/questions/21256219/
//copy-file-from-one-folder-to-another-in-java

//cs61b-asi Ethan Rucker taught me how to check if two files are equal


public class Git implements Serializable {

    //make sure to empty data structures whenever switching branches
    private HashSet<File> filesAdded;
    private HashMap<Integer, HashMap<File, Integer>> modified; //.txt --> lastModified
    private HashMap<String, LinkedList<File>> findCommitID; 
    private HashSet<File> fileForRemoval;
    private HashMap<Integer, HashMap<File, File>> commitHist;
    private HashMap<String, Integer> branchToCurrentID;
    private HashMap<String, File> branchToDir;
    private Integer commit = 0;
    private String currentBranch;

    public Git() throws IOException {
        boolean folder = new File(".gitlet").mkdir();
        currentBranch = "master";
        filesAdded = new HashSet<File>(); //stores the added files
        modified = new HashMap<Integer, HashMap<File, Integer>>(); //stores a file's last modified
        modified.put(commit, new HashMap<File, Integer>());
        findCommitID = new HashMap<String, LinkedList<File>>(); //File is the current directory
        fileForRemoval = new HashSet<File>();
        commitHist = new HashMap<Integer, HashMap<File, File>>();
        commitHist.put(commit, new HashMap<File, File>());
        branchToCurrentID = new HashMap<String, Integer>();
        branchToCurrentID.put(currentBranch, commit);
        branchToDir = new HashMap<String, File>();
        branchToDir.put(currentBranch, new File(".gitlet"));
        makeDirectory("initial commit"); 
    }

    public void add(String[] tokens) throws IOException {
        if (tokens.length == 0) {
            System.out.println("Did not enter enough arguments");
        } else {
            String file = tokens[0];
            File specified = new File(file);
            if (specified.exists()) { 
                if (!isModified(specified)) { 
                    System.out.println("File has not been modified since the last commit.");
                } else {
                    filesAdded.add(specified);
                    fileForRemoval.remove(specified);
                } 
            } else {
                System.out.println("File does not exist.");
                return;
            }
        }
    }

    //check if a file is modified with respect to the current commit
    private boolean isModified(File file) throws IOException {
        int prevCommit = branchToCurrentID.get(currentBranch);
        if (!commitHist.get(prevCommit).containsKey(file)) {
            return true;
        }
        byte[] commitedFile = Files.readAllBytes(commitHist.get(prevCommit).get(file).toPath());
        byte[] newFile = Files.readAllBytes(file.toPath());
        return !Arrays.equals(commitedFile, newFile);
    } 

    public void commit(String[] tokens) throws IOException {
        if (tokens.length == 0 || tokens[0].length() == 0) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (filesAdded.size() == 0 && fileForRemoval.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        } 
        int prevCommit = branchToCurrentID.get(currentBranch);
        commit += 1;
        branchToCurrentID.put(currentBranch, commit);
        commitHist.put(commit, new HashMap<File, File>());

        //inherit old files from prev commit
        for (File prev: commitHist.get(prevCommit).keySet()) { 
            commitHist.get(commit).put(prev, commitHist.get(prevCommit).get(prev));
        }

        //inherit old files from prev modified
        modified.put(commit, new HashMap<File, Integer>());

        //we iterate through so that we create a duplicate not a pointer
        for (File prev: modified.get(prevCommit).keySet()) { 
            modified.get(commit).put(prev, modified.get(prevCommit).get(prev));
        }

        for (File file: fileForRemoval) {
            commitHist.get(commit).remove(file);
            modified.get(commit).remove(file);
        }

        fileForRemoval.clear();
        makeDirectory(tokens[0]);
        filesAdded.clear();

    }
    private void makeDirectory(String inputMessage) throws IOException {
        branchToDir.put(currentBranch, new File(branchToDir.get(currentBranch), commit.toString()));
        File currentDir = branchToDir.get(currentBranch);
        currentDir.mkdir();
        
        //now add new files to commitHist while replacing some old ones
        for (File file: filesAdded) {
            FileUtil.makeRecursiveDirectory(currentDir, file); 

            //the File in the .gitlet directory
            File newPathName = new File(currentDir, file.toString()); 

            //copy the file's contents from the working directory into the new one
            FileUtil.copyFile(file, newPathName);

            //add file to our data structures
            commitHist.get(commit).put(file, newPathName);
            modified.get(commit).put(file, commit);
        }

        //creates the commit info log
        String pathName = currentDir.toString() + "/info.txt";
        PrintWriter out = new PrintWriter(pathName); //Each Commit folder has an info.txt file
        out.println("Commit " + commit + ".");
        out.println(new Timestamp(System.currentTimeMillis()));
        out.println(inputMessage);
        out.close();

        //for find purposes
        if (findCommitID.get(inputMessage) == null) {
            findCommitID.put(inputMessage, new LinkedList<File>());
        }

        //add the path to the LinkedList asssociated with the message
        findCommitID.get(inputMessage).addFirst(currentDir); 
    }

    public void removeFile(String[] tokens) { //or at least set it up to be removed
        if (tokens.length == 0) {
            System.out.println("Did not enter enough arguments");
        } else {
            String txt = tokens[0];
            File shouldRemove = new File(txt);
            if (!filesAdded.contains(shouldRemove)  
                    && !commitHist.get(commit).containsKey(shouldRemove)) {
                System.out.println("No reason to remove the file.");
            } else {
                if (filesAdded.contains(shouldRemove)) {
                    filesAdded.remove(shouldRemove);
                } else {
                    //remove from staged 
                    fileForRemoval.add(shouldRemove); 
                }    
            }
        }
    }

    public void log() {
        File tempCurr = branchToDir.get(currentBranch);
        while (!tempCurr.getName().equals(".gitlet")) {
            printLog(tempCurr.toString() + "/info.txt");
            tempCurr = tempCurr.getParentFile();
        }
    }

    public void printLog(String path) throws IOException {
        BufferedReader readInfo = new BufferedReader(new FileReader(new File(path)));
        System.out.println("====");
        System.out.println(readInfo.readLine()); //commit id
        System.out.println(readInfo.readLine()); //Time Stamp
        System.out.println(readInfo.readLine()); //Message
        System.out.println();  
    }

    public void globalLog() {
        for (LinkedList<File> list: findCommitID.values()) {
            for (File dir: list) {
                printLog(dir.toString() + "/info.txt");
            }
        }
    }

    public void findIds(String[] tokens) {
        if (tokens.length == 0) {
            System.out.println("Did not enter enough arguments.");
        } else {
            String message = tokens[0];
            if (findCommitID.get(message) == null) {
                System.out.println("Found no commit with that message.");
            } else {
                for (File path: findCommitID.get(message)) {
                    System.out.println(path.getName());
                }
            }
        }
    } 

    public void getStatus() {
        System.out.println("=== Branches ===");
        for (String branch: branchToCurrentID.keySet()) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (File file: filesAdded) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Files Marked for Removal ===");
        for (File file: fileForRemoval) {
            System.out.println(file);
        }
    }

    public void checkout(String[] tokens) throws IOException {
        if (tokens.length == 0) {
            System.out.println("Checkout takes 1 or 2 arguments");
        }
        if (!getConfirmation()) {
            return;
        }
        if (tokens.length == 1) {
            String path = tokens[0]; 
            File file = new File(path);
            if (branchToCurrentID.keySet().contains(path)) { //checks if it's a branch
                checkOutBranch(path);
            } else if (commitHist.get(branchToCurrentID.get(currentBranch)).containsKey(file)) { 
                //retrieves the directory of the file path
                File fileInGitlet = commitHist.get(commit).get(file); 
                File target = new File(path);
                FileUtil.copyFile(fileInGitlet, target);
            } else {
                System.out.println("File does not exist in the most recent commit,
                                     or no such branch exists.");
            }
        } else {
            try {
                Integer commitID = Integer.parseInt(tokens[0]);
                if (!commitHist.containsKey(commitID)) {
                    System.out.println("No commit with that id exists.");
                    return;
                }
                String fileName = tokens[1];
                File path = new File(fileName);
                File idDirectory = commitHist.get(commitID).get(path);
                FileUtil.copyFile(idDirectory, path);
            } catch (Exception e) {
                System.out.println("File does not exist in that commit.");
            }           
        }
    }

    public void createBranch(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("branch takes in 1 argument");
            return;
        }
        String name = tokens[0];
        if (branchToCurrentID.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
        } else {
            branchToCurrentID.put(name, commit);
            branchToDir.put(name, branchToDir.get(currentBranch));
        }   
    }

    //gitlet main method will serialize so we just need to change current branch
    public void checkOutBranch(String branch) throws IOException {         
        if (currentBranch.equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        } else {
            currentBranch = branch; //checkout already makes sure our branch exists
            int branchesCommit = branchToCurrentID.get(branch);
            loadInheritedFiles(commitHist.get(branchesCommit));
        }
    }

    public void removeBranch(String[] tokens) {
        if (tokens.length == 0) {
            System.out.println("remove branch takes in 1 argument");
        } else {
            String branch = tokens[0];
            if (!branchToCurrentID.keySet().contains(branch)) {
                System.out.println("A branch with that name does not exist.");
            } else if (branch.equals(currentBranch)) {
                System.out.println("Cannot remove the current branch.");
            } else {
                //we don't have to delete the contents because those contents can never be reached again
                //since its starting directory changes. !!!after testing, we must find a way to delete its contents
                branchToCurrentID.remove(branch);
                branchToDir.remove(branch);
            }
        }
    }

    public void loadInheritedFiles(HashMap<File, File> inheritedFiles) throws IOException {
        for (File file: inheritedFiles.keySet()) {
            File committedFile = inheritedFiles.get(file);
            FileUtil.copyFile(committedFile, file);
        }
    }

    public void reset(String[] tokens) throws IOException {
        if (tokens.length == 0) {
            System.out.println("reset takes in 1 argument");
        } else {
            if (!getConfirmation()) {
                return;
            }
            try {
                Integer commitID = Integer.parseInt(tokens[0]);
                if (commitHist.containsKey(commitID)) {
                    HashMap<File, File> loadThis = commitHist.get(commitID);
                    loadInheritedFiles(loadThis);
                    branchToCurrentID.put(currentBranch, commitID);
                    File currentDir = branchToDir.get(currentBranch);
                    Integer currentID = Integer.parseInt(currentDir.getName());
                    while (!currentID.equals(commitID)) {
                        currentDir = currentDir.getParentFile();
                        currentID = Integer.parseInt(currentDir.getName());
                    }
                    branchToDir.put(currentBranch, currentDir);
                } else {
                    System.out.println("No commit with that id exists.");
                }   
            } catch (Exception e) {
                System.out.println("No commit with that id exists.");
            }
            
        }
    }

    public void merge(String[] tokens) throws IOException {
        if (tokens.length == 0) {
            System.out.println("merge takes in 1 argument");
        } else {
            if (!getConfirmation()) {
                return;
            }
            String branch = tokens[0];
            if (!branchToCurrentID.containsKey(branch)) {
                System.out.println("A branch with that name does not exist.");
            } else if (currentBranch.equals(branch)) {
                System.out.println("Cannot merge a branch with itself.");
            } else {
                File altBranch = branchToDir.get(branch);
                File currBranch = branchToDir.get(currentBranch);
                File commonAncestor = findCommonAncestor(altBranch, currBranch);
                Integer split = Integer.parseInt(commonAncestor.getName());
                Integer alt = Integer.parseInt(altBranch.getName());
                Integer curr = Integer.parseInt(currBranch.getName());
                //merge the files from the alt branch only if we haven't modified the file in the current

                for (File file: commitHist.get(alt).keySet()) {    
                    boolean currContainsFile = modified.get(curr).containsKey(file);
                    boolean altContainsFile = modified.get(alt).containsKey(file);
                    boolean splitContainsFile = modified.get(split).containsKey(file);

                    boolean altModifiedSinceSplit = !(
                    (altContainsFile && 
                    (modified.get(alt).get(file).equals(modified.get(split).get(file)))) || //scenario 1
                    (splitContainsFile && 
                    (modified.get(split).get(file).equals(modified.get(alt).get(file)))) || //scenario 2
                    (!altContainsFile && !splitContainsFile)); //scenario 3

                    boolean currModifiedSinceSplit = !(
                    (currContainsFile && 
                    (modified.get(curr).get(file).equals(modified.get(split).get(file)))) || //scenario 1
                    (splitContainsFile && 
                    (modified.get(split).get(file).equals(modified.get(curr).get(file)))) || //scenario 2
                    (!currContainsFile && !altContainsFile)); //scenario 3        

                    //tells us what the currBranch should inherit from altBranchID
                    // if a file has been removed, the modified one should be added
                    if (altModifiedSinceSplit && !currModifiedSinceSplit || 
                            (!currContainsFile && altContainsFile && altModifiedSinceSplit && currModifiedSinceSplit)) { 
                        modified.get(curr).put(file, alt);
                        commitHist.get(curr).put(file, commitHist.get(alt).get(file));
                    } 

                    //conflicts with modifications
                    else if (altModifiedSinceSplit && currModifiedSinceSplit) {
                        File conflictedFile = new File(file.toString() + ".conflicted");
                        File conflictedGitlet = new File(currBranch, file.toString() + ".conflicted");
                        commitHist.get(curr).put(conflictedFile, conflictedGitlet);
                        modified.get(curr).put(conflictedGitlet, curr);
                        File source = commitHist.get(alt).get(file);
                        FileUtil.makeRecursiveDirectory(currBranch, conflictedFile);
                        FileUtil.copyFile(source, conflictedGitlet);
                    }
                }
                loadInheritedFiles(commitHist.get(curr)); //load all the merged files
            }
        }
    }

    //look when the two files have the same commit ID
    public File findCommonAncestor(File branch1, File branch2) {
        if (branch1.equals(branch2)) {
            return branch1;
        } else if (Integer.parseInt(branch1.getName()) > Integer.parseInt(branch2.getName())) {
            return findCommonAncestor(branch1.getParentFile(), branch2);
        } else {
            return findCommonAncestor(branch1, branch2.getParentFile());
        }
    }

    
    public void rebase(String[] tokens, boolean interactive) throws IOException {
        if (tokens.length == 0) {
            System.out.println("rebase requires 1 argument");
        }
        String branch = tokens[0];
        if (!getConfirmation()) {
            return;
        }
        if (!branchToCurrentID.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
        } else if (branch.equals(currentBranch)) {
            System.out.println("Cannot rebase a branch onto itself.");
        } else {
            File currBranchDir = branchToDir.get(currentBranch);
            File altBranchDir = branchToDir.get(branch);
            File splitPoint = findCommonAncestor(currBranchDir, altBranchDir);
            Integer currBranchID = branchToCurrentID.get(currentBranch);
            Integer altBranchID = branchToCurrentID.get(branch);
            if (splitPoint.equals(altBranchDir)) {
                System.out.println("Already up-to-date.");
                return;
            }
            //check if the current branch is in the history of branch. SPECIAL CASE
            if (currBranchDir.toString().equals(splitPoint.toString())) {
                modified.put(currBranchID, modified.get(altBranchID));
                commitHist.put(currBranchID, commitHist.get(altBranchID));
                branchToCurrentID.put(currentBranch, altBranchID);
                branchToDir.put(currentBranch, branchToDir.get(branch));
            } else {
                //Normal Case 
                rebaseNormalCase(splitPoint, branch, interactive);
            }    
            Integer currentCommitID = branchToCurrentID.get(currentBranch);
            loadInheritedFiles(commitHist.get(currentCommitID));       
        }
    }

    public void rebaseNormalCase(File splitPoint, String branch, boolean interactive) throws IOException {
        //Set Up
        File currBranchDir = branchToDir.get(currentBranch);
        File altBranchDir = branchToDir.get(branch); //return to normal
        Integer altBranchID = branchToCurrentID.get(branch);
        Integer splitPointID = Integer.parseInt(splitPoint.getName());
        Integer currBranchID = branchToCurrentID.get(currentBranch);                              
            //change the current branch's directory to the one we want to add to
        File newDirectory = branchToDir.get(branch);
        branchToDir.put(currentBranch, newDirectory); 
        branchToCurrentID.put(currentBranch, branchToCurrentID.get(branch));

        //Add all modified files in the branch we're rebasing that are not modified in the current branch
        LinkedList<File> currBranchHist = getCurrBranchHist(currBranchDir, splitPoint);

        for (File fileHist: currBranchHist) {
            boolean replay = true;
            String inputMessage = "Effect of Rebasing " + branch;
            if (interactive) {
                System.out.println("Currently replaying:");
                printLog(fileHist.toString() + "/info.txt");
                HashSet<String> validInputs = new HashSet<String>();
                validInputs.add("c");
                validInputs.add("s");
                validInputs.add("m");
                String response = askForInput(validInputs);
                if (response.equals("s")) {
                    if (fileHist.toString().equals(currBranchHist.getFirst().toString()) || 
                        fileHist.toString().equals(currBranchHist.getLast().toString())) {
                        System.out.println("You cannot skip the initial or final commit of a branch.");
                        validInputs.remove("s");
                        response = askForInput(validInputs);
                    } else {
                        replay = false;
                    }                                   
                }
                if (response.equals("m")) {
                    System.out.println("Please enter a new message for this commit.");
                    Scanner in = new Scanner(System.in);
                    inputMessage = in.nextLine();
                }        
            }
            if (replay) {
                Integer commitID = Integer.parseInt(fileHist.getName());
                HashMap<File, File> frameFiles = commitHist.get(commitID);

                int prevCommit = branchToCurrentID.get(branch);
                commit += 1;
                branchToCurrentID.put(currentBranch, commit);
                //create a duplicate not a pointer
                commitHist.put(commit, new HashMap<File, File>());
                modified.put(commit, new HashMap<File, Integer>());

                //inherit all files from the replay 
                for (File file: frameFiles.keySet()) {
                    commitHist.get(commit).put(file, commitHist.get(commitID).get(file));
                    modified.get(commit).put(file, modified.get(commitID).get(file));
                }
                //determine which files to inherit for a given frame
                HashMap<File, File> shallInherit = shallInherit(commitID, altBranchID, splitPointID);
                for (File file: shallInherit.keySet()) {
                    File rebasedBranchFile = commitHist.get(altBranchID).get(file);
                    Integer fileModif = modified.get(altBranchID).get(file);
                    commitHist.get(commit).put(file, rebasedBranchFile);
                    modified.get(commit).put(file, fileModif);
                }
                rebaseCommit(inputMessage, branch);                
            }
        }
    }

    
    //rebaseCommit will add all the shallInherit files to the newCommit, make sure files are inherited from the
    //branch we are rebasing to, and updates the directories
    public void rebaseCommit(String inputMessage, String rebasingBranch) throws IOException {       
        branchToDir.put(currentBranch, new File(branchToDir.get(currentBranch), commit.toString())); //change the currentDir
        File currentDir = branchToDir.get(currentBranch);
        currentDir.mkdir();

        //creates the commit info log
        String pathName = currentDir.toString() + "/info.txt";
        PrintWriter out = new PrintWriter(pathName); //Each Commit folder has an info.txt file
        out.println("Commit " + commit + ".");
        out.println(new Timestamp(System.currentTimeMillis()));
        out.println(inputMessage);
        out.close();

        //for find purposes
        if (findCommitID.get(inputMessage) == null) {
            findCommitID.put(inputMessage, new LinkedList<File>());
        }
        findCommitID.get(inputMessage).addFirst(currentDir); //add the path to the LinkedList asssociated with the 
    }

    //Gives us back a map of files that should be inherited from the alternate commit ID
    public HashMap<File, File> shallInherit(Integer curr, Integer alt, Integer split) {
        HashMap<File, File> inherited = new HashMap<File, File>();

        for (File file: commitHist.get(alt).keySet()) {
            //if a file has been removed, then we consider that file to be modified, and should not inherit from rebase
            boolean currContainsFile = modified.get(curr).containsKey(file);
            boolean altContainsFile = modified.get(alt).containsKey(file);
            boolean splitContainsFile = modified.get(split).containsKey(file);

            boolean altModifiedSinceSplit = !(
            (altContainsFile && 
            (modified.get(alt).get(file).equals(modified.get(split).get(file)))) || //scenario 1
            (splitContainsFile && 
            (modified.get(split).get(file).equals(modified.get(alt).get(file)))) || //scenario 2
            (!altContainsFile && !splitContainsFile)); //scenario 3

            boolean currModifiedSinceSplit = !(
            (currContainsFile && 
            (modified.get(curr).get(file).equals(modified.get(split).get(file)))) || //scenario 1
            (splitContainsFile && 
            (modified.get(split).get(file).equals(modified.get(curr).get(file)))) || //scenario 2
            (!currContainsFile && !altContainsFile)); //scenario 3        

            if (altModifiedSinceSplit && !currModifiedSinceSplit || 
                    (!currContainsFile && altContainsFile && altModifiedSinceSplit && currModifiedSinceSplit)) { 
                inherited.put(file, commitHist.get(alt).get(file));
            } 
        }   
        return inherited;
    }

    public LinkedList<File> getCurrBranchHist(File currBranchDir, File splitPoint) {
        LinkedList<File> currBranchHist = new LinkedList<File>();
        while (!currBranchDir.getName().equals(splitPoint.getName())) {
            currBranchHist.addFirst(currBranchDir);
            currBranchDir = currBranchDir.getParentFile();
        }
        return currBranchHist;
    }

    public boolean getConfirmation() {
        System.out.println("Warning: The command you entered may alter the files in your working directory. Uncommitted changes may be lost. Are you sure you want to continue? (yes/no)");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        String yes = new String("yes");
        if (yes.equals(input)) {
            filesAdded.clear();
            fileForRemoval.clear();
            return true;
        } else {
            System.out.println("Did not type 'yes' so aborting");
            return false;
        }
    }
    private String askForInput(HashSet<String> validInputs) {
        String input = "default";
        while (!validInputs.contains(input)) {
            System.out.println("Would you like to (c)ontinue, (s)kip this commit, or change this commit's (m)essage?");
            Scanner in = new Scanner(System.in);
            input = in.nextLine(); 
        }
        return input;
    }
}
