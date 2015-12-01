import java.util.TreeMap;
import java.util.Comparator;

/**
 * Prefix-Trie. Supports linear time find() and insert(). 
 * Should support determining whether a word is a full word in the 
 * Trie or a prefix.
 * @author Anthony Tan
 */
public class Trie {
    private Node top;
    /**
     * Initializes required data structures with default comparator
     * runs in O(1) time
     */
    public Trie() {
        this(new DefaultComparator());
    }

    /**
     * Initializes required data structures from parallel arrays.
     * @param x Comparator<Character> used for foreign dictionaries
     * runs in O(1) time
     */
    public Trie(Comparator<Character> x) {
        top = new Node(x);
    }

    /**
     * Finds out whether a string is in our trie.
     * @param s String the string we are looking for in our trie.
     * @param isFullWord tells us whether this string is a full word.
     * @return boolean return true or false whether we find the word or not
     * Runs in O(N) time and space where N is the length of the String
     */
    public boolean find(String s, boolean isFullWord) {
        if (s == null || s.length() == 0) {
            throw new IllegalArgumentException();
        }
        return top.find(s, isFullWord);
    }

    /**
     * Inserts a string into the trie
     * @param s String the string we are inserting.
     * Runs in O(N) time and space where N is the length of the String
     * @return
     */
    public void insert(String s) {
        if (s == null || s.length() == 0) {
            throw new IllegalArgumentException();
        }
        top.insert(s);
    }

    /**
     * Gets the Top Node
     * @return Node the top Node
     * runs in O(1) time
     */
    public Node getTopNode() {
        return top;
    }

    /**
     * Each Node holds links and 1 character. These are the building blocks of a Trie
     */
    public class Node {
        private boolean fullWord;
        private TreeMap<Character, Node> links;
        private Comparator<Character> comp;

        /**
         * Initializes required data structures with the given parameter.
         * @param x Comparator<Character> used for foreign dictionaries
         * runs in O(1) time
         */
        public Node(Comparator<Character> x) {
            comp = x;
            links = new TreeMap<Character, Node>(comp);
            fullWord = false;  
        }

        /**
         * Tells us if the word is a full word at this point
         * @return boolean 
         * runs in O(1) time
         */
        public boolean isFullWord() {
            return fullWord;
        }

        /**
         * Getter function
         * @return TreeMap<Character, Node> is the map of characters from this node
         * runs in O(1) time
         */
        public TreeMap<Character, Node> getLinks() {
            return links;
        }

        /**
         * finds the word starting from this node
         * @param s String word we are finding
         * @param isFullWord boolean tells us whether this string is a full word.
         * @return boolean return true or false whether we find the word or not
         * Runs in O(N) time and space where N is the length of the String
         */
        public boolean find(String s, boolean isFullWord) {
            Node copy = this;
            for (int i = 0; i < s.length(); i++) {
                if (!copy.links.containsKey(s.charAt(i))) {
                    return false;
                }
                copy = copy.links.get(s.charAt(i));
            }
            if (isFullWord) {
                return isFullWord == copy.fullWord;
            } else {
                return true;
            }
        }

        /**
         * Iteratively inserts all the characters of a word into the trie
         * Runs in O(N) time and space where N is the length of the String
         * @param s String the string we are inserting.
         * @return
         */
        public void insert(String s) {
            Node copy = this;
            for (int i = 0; i < s.length(); i++) {
                if (!copy.links.containsKey(s.charAt(i))) {
                    copy.links.put(s.charAt(i), new Node(comp));
                } 
                copy = copy.links.get(s.charAt(i));
            }
            copy.fullWord = true;
        }
    }

    /**
     * Default comparator for Characters
     */
    public static class DefaultComparator implements Comparator<Character> {
        /**
         * Tells the trie the order in which to sort words
         * @param x Character the first word compared
         * @param y Character the second word compared
         * @return int. This tells us which character comes first
         * runs in O(1) time
         */
        public int compare(Character x, Character y) {
            return x.compareTo(y);
        }
    }
}
