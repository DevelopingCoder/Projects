import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.Comparator;

/**
 *  This implementation uses a ternary search trie.
 *  Manipulated for Autocomplete purposes
 *  @author Robert Sedgewick and Kevin Wayne.
 **/
public class MyTST {
    private int N; // size
    private Node root;   // root of TST

    /**
     * Initializes an empty string symbol table.
     * runs in O(1) time
     */
    public MyTST() {
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     * @return the number of key-value pairs in this symbol table
     * runs in O(1) time
     */
    public int size() {
        return N;
    }

    /**
     * Does this symbol table contain the given key?
     * @param key the key
     * @return true if this symbol table contains key and false otherwise
     * runs in O(1) time
     */
    public boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * Returns the value associated with the given key.
     * @param key the key
     * @return the value associated with the given key if the key is in the symbol table
     *     and null if the key is not in the symbol table
     * runs in O(L) where L length of the key
     */
    public Double get(String key) {
        if (key == null) {
            throw new NullPointerException();
        } 
        Node x = get(root, key, 0);
        if (x == null) {
            return null;
        }
        return x.val;
    }

    /**
     * return subtrie corresponding to given key
     * @param key String the key
     * @param x Node the Node
     * @param d int the character at position d of the key
     * @return Node
     * runs in O(L) where L length of the key
     */
    private Node get(Node x, String key, int d) {
        if (key == null) {
            throw new NullPointerException();
        }
        if (x == null) {
            return null;
        }
        if (key.length() == 0) {
            return x;
        }
        char c = key.charAt(d);
        if (c < x.c) {
            return get(x.left, key, d);
        } else if (c > x.c) {
            return get(x.right, key, d);
        } else if (d < key.length() - 1) {
            return get(x.mid, key, d + 1);
        } else {
            return x;
        }                          
    }

    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is null this effectively deletes the key from the symbol table.
     * @param key the key
     * @param val the value
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     * runs in O(L) where L length of the key
     */
    public void put(String key, Double val) {
        if (!contains(key)) {
            N++;
        } 
        root = put(root, key, val, 0);
    }

    /**
     * Inserts a key into our TST
     * @param x Node the Node we are inserting from
     * @param key String
     * @param val Double value assigned to the key
     * @param d int
     * @return Node that we finished our insert at
     * runs in O(L) where L length of the key
     */
    private Node put(Node x, String key, Double val, int d) {
        char c = key.charAt(d);
        if (x == null) {
            x = new Node();
            x.c = c;
            x.val = val;
        }
        if (val > x.val) {
            x.val = val;
        }
        if (c < x.c) {
            x.left = put(x.left, key, val, d);
        } else if (c > x.c) {
            x.right = put(x.right, key, val, d);
        } else if (d < key.length() - 1) {
            x.mid = put(x.mid, key, val, d + 1);
        } else {
            x.wordWeight = val;
            x.isFullWord = true;
            x.fullWord = key;
        }                     
        return x;
    }

    /**
     * Returns all of the keys in the set that start with <tt>prefix</tt>.
     * @param prefix the prefix
     * @param k int the number of keys we want
     * @return all of the keys in the set that start with <tt>prefix</tt>,
     *     as an iterable
     * runs in O(MN) where M is the number of words with the prefix and 
     * N is the max length of the words beginning with the prefix
     */
    public Iterable<String> keysWithPrefix(String prefix, int k) {
        PriorityQueue<Node> minPQ = new PriorityQueue<Node>(k, new MinNodeComparator());
        PriorityQueue<Node> maxPQ = new PriorityQueue<Node>(k, new MaxNodeComparator());
        Node x = get(root, prefix, 0); //gets us to the correct Node
        if (x == null) {
            return new LinkedList<String>(); //miss
        }
        if (prefix.length() == 0) {
            traverse(x, minPQ, k, maxPQ);
        } else {
            checkFullWord(x, minPQ, k);
            traverse(x.mid, minPQ, k, maxPQ);
        }
        
        LinkedList<String> topMatches = new LinkedList<String>();
        while (minPQ.size() > 0) {
            topMatches.addFirst(minPQ.poll().fullWord);
        }
        return topMatches;
    }

    /**
     * Adds the node to the priority queue if it meets our conditions
     * @param x Node node we want to add
     * @param minPQ PriorityQueue
     * @param k int the max size of our minPQ
     * runs in O(log k) time where k is the max size of our minPQ
     * @return
     */
    private void checkFullWord(Node x, PriorityQueue<Node> minPQ, int k) {
        if (x.isFullWord) {
            if (minPQ.size() < k) {
                minPQ.add(x);
            } else if (x.wordWeight > minPQ.peek().wordWeight) {
                minPQ.poll();
                minPQ.add(x);
            }
        }
    }

    /**
     * traverses our trie and builds a PQ of the top words of a given prefix
     * runs in O(MN) where M is the number of words with the prefix and 
     * N is the max length of the words beginning with the prefix
     * @param x Node node we are traversing through
     * @param minPQ PriorityQueue
     * @param k int the max size of our minPQ
     * @param maxPQ PriorityQueue
     * @return
     */
    private void traverse(Node x, PriorityQueue<Node> minPQ, int k, PriorityQueue<Node> maxPQ) {
        if (x == null) {
            return; //base case
        } 
        checkFullWord(x, minPQ, k);
        addToMaxPQ(x.left, maxPQ);
        addToMaxPQ(x.right, maxPQ);
        addToMaxPQ(x.mid, maxPQ);
        while (maxPQ.size() != 0 && (minPQ.size() < k 
            || maxPQ.peek().val > minPQ.peek().wordWeight)) {
            traverse(maxPQ.poll(), minPQ, k, maxPQ);
        }
    }

    /**
     * adds x to our PQ if x is not null
     * @param x Node node we are tadding
     * @param maxPQ PriorityQueue
     * runs in O(log X) time where X is the number of items in our maxPQ
     * @return
     */
    private void addToMaxPQ(Node x, PriorityQueue<Node> maxPQ) {
        if (x == null) {
            return;
        } else {
            maxPQ.add(x);
        }
    }

    /**
     *  Nodes make up a trie. They hold one character
     **/
    public class Node {
        private char c;                        // character
        private Node left, mid, right;  // left, middle, and right subtries
        private Double val;                     // value associated with string
        private boolean isFullWord;
        private String fullWord;
        private Double wordWeight;
    }

    /**
     *  Compares Nodes, making the smaller one come first
     **/
    public static class MinNodeComparator implements Comparator<Node> {
        /**
         *  Compares Nodes using the weight of its word
         * @param x Node 
         * @param y Node
         * @return int
         * runs in O(1) time
         **/
        public int compare(Node x, Node y) {
            double result = x.wordWeight - y.wordWeight;
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    /**
     *  Compares Nodes, making the larger one come first
     **/
    public static class MaxNodeComparator implements Comparator<Node> {
        /**
         *  Compares Nodes by their values
         * @param x Node 
         * @param y Node
         * @return int
         * runs in O(1) time
         **/
        public int compare(Node x, Node y) {
            double result = x.val - y.val;
            if (result < 0) {
                return 1;
            } else if (result > 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
