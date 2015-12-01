import java.util.Scanner;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Collections;
/**
 * Finds options of the most popular words of a given prefix
 * @author Anthony Tan
 */
public class Boggle {
    int numWords = 1;
    int width = 4;
    int height = 4;
    int lowestWordSize;
    File path;
    CharNode[][] board;
    Trie dictionary;
    HashSet<String> dictSet;

    /**
     * Initializes path to dictionary
     */
    public Boggle() {
        path = new File("words");
        dictSet = new HashSet<String>();
    }

    /**
     * Functions the Boggle
     * @param args Array
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs arguments");
        }
        Boggle boggle = new Boggle();
        boolean needInputFile = boggle.readFlags(args);
        if (needInputFile) {
            Scanner in = new Scanner(System.in);
            boggle.readBoggle(in);
        }
        boggle.readDictionary();
        boggle.start();  
    }

    /**
     * Reads and applies the flags
     * @param input Array
     * @return boolean tells us if we need to input a boggle file
     */
    private boolean readFlags(String[] input) {
        boolean inputFile = true;
        int count = 0;
        boolean randomize = false;
        while (count + 1 < input.length) {
            String flag = input[count];
            String info = input[count + 1];
            if (flag.equals("-r")) {
                randomize = true;
                count -= 1;
                inputFile = false;
            } else if (flag.equals("-k")) {
                numWords = Integer.parseInt(info);
            } else if (flag.equals("-n")) {
                width = Integer.parseInt(info);
            } else if (flag.equals("-m")) {
                height = Integer.parseInt(info);
            } else if (flag.equals("-d")) {
                path = new File(info);
            }
            if (numWords <= 0 || width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Invalid argument");
            }
            count += 2;
        }
        if (randomize) {
            board = new CharNode[height][width];
            int id = 0;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    final int startChar = 97;
                    board[i][j] = new CharNode((char) (startChar 
                                    + ((int) (Math.random() * 26))), i, j);
                    id += 1;
                }
            }    
        }
        return inputFile;  
    } 

    /**
     * Reads and initializes data
     * @param in Scanner
     */
    private void readBoggle(Scanner in) {
        if (in.hasNext()) {
            LinkedList<String> words = new LinkedList<String>();
            height = 1;
            String word = in.next();
            width = word.length();
            words.add(word);
            while (in.hasNext()) {
                height += 1;
                word = in.next();
                if (word.length() != width) {
                    throw new IllegalArgumentException("Not Rectangular");
                }
                words.add(word);
            }
            board = new CharNode[height][width];
            int row = 0;
            int id = 0;
            for (String x: words) {
                for (int col = 0; col < width; col++) {
                    board[row][col] = new CharNode(x.charAt(col), row, col);
                    id += 1;
                }
                row += 1;
            }
        }
    }

    /**
     * Sets up our trie dictionary
     */
    private void readDictionary() {
        try {
            if (!path.exists()) {
                throw new IllegalArgumentException("Invalid Dictionary Path");
            }
            Scanner in = new Scanner(path);
            dictionary = new Trie();
            while (in.hasNext()) {
                String word = in.next();
                dictionary.insert(word);
                dictSet.add(word);
            }   
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * begins the search for words starting at each character node
     */
    private void start() {
        ArrayList<String> list = new ArrayList<String>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                CharNode start = board[row][col]; //must reset each time
                getResults(list, start);
            }
        }
        Collections.sort(list);
        Collections.sort(list, new LengthComparator());
        HashSet<String> printedWords = new HashSet<String>();
        int count = 0;
        while (count < list.size() && numWords > 0) {
            String word = list.get(count);
            if (!printedWords.contains(word)) {
                System.out.println(word);
                numWords -= 1;         
                printedWords.add(word);
            }
            count += 1; 
        }
    }   

    /**
     * Iterative DFS that finds develops our list
     * @param list ArrayList<String>
     * @param start CharNode
     */
    private void getResults(ArrayList<String> list, CharNode start) {
        Stack<StackNode> validBuilds = new Stack<StackNode>();
        StackNode origin = new StackNode(start, 
                            ((Character) start.c).toString(), new HashSet<CharNode>());
        validBuilds.push(origin);

        while (!validBuilds.isEmpty()) {  
            StackNode node = validBuilds.pop();
            String current = node.prefix;    
            if (dictSet.contains(current) && (list.size() < numWords 
                                || current.length() > lowestWordSize)) {
                if (current.length() < lowestWordSize) {
                    lowestWordSize = current.length();
                }
                list.add(current);
            }
            boolean validPrefix = dictionary.find(current, false);
            if (validPrefix) {
                for (int x = -1; x < 2; x++) {
                    for (int y = -1; y < 2; y++) {
                        if (validIndex(node.charNode, x, y)) {
                            CharNode pot = board[node.charNode.row + x][node.charNode.col + y];
                            if (!node.hasVisited.contains(pot)) {
                                //pass in a duplicate of the hasVisited Set
                                StackNode next = new StackNode(pot, 
                                                    current + pot.c, node.hasVisited);
                                validBuilds.push(next);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Iterative DFS that finds develops our list
     * @param node CharNode
     * @param x int the row
     * @param y int the column
     * @return boolean 
     */
    private boolean validIndex(CharNode node, int x, int y) {
        int newRow = node.row + x;
        int newCol = node.col + y;
        if (newRow < height && newCol < width
                && newRow >= 0 && newCol >= 0) {
            return true;
        }
        return false;
    }

    /**
     * Stores information about the word we are building up
     */
    public class StackNode {
        CharNode charNode;
        String prefix;
        HashSet<CharNode> hasVisited;

        /**
         * Stores information about the word we are building up
         * @param x CharNode
         * @param s String
         * @param visited HashSet<CharNode>
         */
        public StackNode(CharNode x, String s, HashSet<CharNode> visited) {
            charNode = x;
            prefix = s;
            hasVisited = new HashSet<CharNode>();
            for (CharNode node: visited) {
                hasVisited.add(node);
            }
            hasVisited.add(charNode);
        }
    }

    /**
     * Defines a character in our Board
     */
    public class CharNode {
        char c;
        int row;
        int col;   

        /**
         * Stores information about the word we are building up
         * @param x char
         * @param row int
         * @param col int
         */
        public CharNode(char x, int row, int col) {
            c = x;
            this.row = row;
            this.col = col;            
        } 
    }

    /**
     * Compares our boggle words in descending order of length
     */
    public class LengthComparator implements Comparator<String> {
        /**
         * Compares our boggle words in descending order of length
         * @param a String
         * @param b String
         * @return int
         */
        public int compare(String a, String b) {
            return b.length() - a.length();
        }
    }
}
