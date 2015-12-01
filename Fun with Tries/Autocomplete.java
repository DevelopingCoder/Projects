/**
 * Implements autocomplete on prefixes for a given dictionary of terms and weights.
 */
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;
/**
 * Finds options of the most popular words of a given prefix
 * @author Anthony Tan
 */
public class Autocomplete {
    private HashMap<String, Double> termWeights;
    private MyTST trie;
    /**
     * Initializes required data structures from parallel arrays.
     * @param terms Array of terms.
     * @param weights Array of weights.
     * Runs in O(MN) time and space where M is the number of words and
     *  N is the length of the longest string 
     */
    public Autocomplete(String[] terms, double[] weights) {
        if (terms.length != weights.length) {
            throw new IllegalArgumentException("terms and weights arrays are not equal");
        }
        termWeights = new HashMap<String, Double>();
        trie = new MyTST();
        for (int i = 0; i < terms.length; i++) {
            if (weights[i] < 0) {
                throw new IllegalArgumentException("Negative weights are not permitted");
            }
            if (termWeights.containsKey(terms[i])) {
                throw new IllegalArgumentException("Duplicate terms are not permitted");
            }
            termWeights.put(terms[i], weights[i]);
            trie.put(terms[i], weights[i]);
        }
    }

    /**
     * Find the weight of a given term. If it is not in the dictionary, return 0.0
     * @param term String 
     * @return double
     * runs in O(1) time
     */
    public double weightOf(String term) {
        return termWeights.get(term);
    }

    /**
     * Return the top match for given prefix, or null if there is no matching term.
     * @param prefix Input prefix to match against.
     * @return Best (highest weight) matching string in the dictionary.
     * runs in O(MN) where M is the number of words with the prefix and 
     * N is the max length of the words beginning with the prefix
     */
    public String topMatch(String prefix) {
        return topMatches(prefix, 1).iterator().next();
    }

    /**
     * Returns the top k matching terms (in descending order of weight) as an iterable.
     * If there are less than k matches, return all the matching terms.
     * @param prefix String
     * @param k int
     * @return Iterable<String> 
     * runs in O(MN) where M is the number of words with the prefix and 
     * N is the max length of the words beginning with the prefix
     */
    public Iterable<String> topMatches(String prefix, int k) {
        if (k < 0) {
            throw new IllegalArgumentException("Number of top matches must be positive");
        }
        return trie.keysWithPrefix(prefix, k);
    }

    /**
     * Finds the the common prefixes between 2 strings
     * @param a String
     * @param b Maximum edit distance to search
     * @return String
     */
    public String getCommonPrefix(String a, String b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException();
        }
        if (a.length() == 0 || b.length() == 0) {
            return "";
        }
        StringBuilder prefix = new StringBuilder();
        int length = Math.min(a.length(), b.length());
        for (int i = 0; i < length; i++) {
            if (a.charAt(i) == b.charAt(i)) {
                prefix.append(a.charAt(i));
            } else {
                break;
            }
        }
        return prefix.toString();
    }

    /**
     * Returns the highest weighted matches within k edit distance of the word.
     * If the word is in the dictionary, then return an empty list.
     * @param word The word to spell-check
     * @param dist Maximum edit distance to search
     * @param k    Number of results to return 
     * @return Iterable in descending weight order of the matches
     */
    public Iterable<String> spellCheck(String word, int dist, int k) {
        if (dist <= 0 || k < 0) {
            throw new IllegalArgumentException("dist or k is Negative");
        }

        //Set Up
        String[] allWords = termWeights.keySet().toArray(new String[termWeights.size()]);
        List<String> sortedWords = Arrays.asList(allWords);
        Collections.sort(sortedWords);

        HashMap<String, Integer> levDist = new HashMap<String, Integer>();
        final int cap = 11;
        PriorityQueue<String> maxWords = new PriorityQueue<String>(cap, new WeightComparator());
        
        //Main Workload
        String prevWord = "";
        LinkedList<int[]> rows = new LinkedList<int[]>();
        for (String fullWord: sortedWords) {
            if (levDist.containsKey(fullWord)) {
                //only add the fullWord if it fits our distance
                if (levDist.get(fullWord) <= dist) {
                    maxWords.add(fullWord);
                }
            } else if (fullWord.length() - word.length() <= dist) {
                String commonPrefix = getCommonPrefix(fullWord, prevWord);
                int discreptancy = Math.abs(prevWord.length() - commonPrefix.length());
                while (discreptancy > 0) {
                    rows.removeLast(); //watch edge case of going over the linkedlist
                    //I'm assuming it shouldn't based on our algorithm
                    discreptancy -= 1;
                }
                //LinkedList is changed in levenshteinDistance
                int distance = levenshteinDistance(word, fullWord, commonPrefix.length(), rows);
                levDist.put(fullWord, distance);
                if (distance <= dist) {
                    maxWords.add(fullWord);
                }
                prevWord = fullWord;
            }
        }

        LinkedList<String> result = new LinkedList<String>();
        //transferring PQ into the form we want
        while (maxWords.size() > 0 && k > 0)  {
            result.add(maxWords.poll());
            k -= 1;
        }
        return result;
    }
    /**
     * Finds the edit distance of s in comparison to t
     *taken from http://en.wikipedia.org/wiki/Levenshtein_distance#Iterative_with_two_matrix_rows
     * @param incorrect String 
     * @param expected String
     * @param prefixLength int 
     * @param rows LinkedList<int[]>
     * @return int number of edit distances
     */
    public int levenshteinDistance(String incorrect, String expected, 
                                    int prefixLength, LinkedList<int[]> rows) {
        // degenerate cases
        if (incorrect.equals(expected)) {
            return 0;
        }
        if (incorrect.length() == 0) {
            return expected.length();
        }
        if (expected.length() == 0) {
            return incorrect.length();
        }

        // create two work vectors of integer distances
        int[] v1 = new int[incorrect.length() + 1];
        int[] v0 = new int[incorrect.length() + 1];
        if (rows.size() > 0) {
            int[] prevRow = rows.peekLast();
            for (int i = 0; i < prevRow.length; i++) {
                v0[i] = prevRow[i];
            }
        } else { 
            int[] dupV0 = new int[incorrect.length() + 1];
            // initialize v0 (the previous row of distances)
            // this row is A[0][i]: edit distance for an empty s
            // the distance is just the number of characters to delete from t
            for (int i = 0; i < v0.length; i++) {
                v0[i] = i;
                dupV0[i] = i;
            }
            rows.add(dupV0);
        }
        
        for (int i = prefixLength; i < expected.length(); i++) {
            // calculate v1 (current row distances) from the previous row v0
     
            // first element of v1 is A[i+1][0]
            // edit distance is delete (i+1) chars from s to match empty t
            v1 = new int[incorrect.length() + 1];
            v1[0] = i + 1;
     
            // use formula to fill in the rest of the row
            for (int j = 0; j < incorrect.length(); j++) {
                int cost = 1;
                if (expected.charAt(i) == incorrect.charAt(j)) {
                    cost = 0;
                }
                v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost));
            }
            //add to our linkedList
            rows.add(v1);

            // copy v1 (current row) to v0 (previous row) for next iteration
            for (int j = 0; j < v0.length; j++) {
                v0[j] = v1[j];
            }

        }
        return v1[incorrect.length()];
    }

    /**
     * Test client. Reads the data from the file, 
     * then repeatedly reads autocomplete queries from standard input and prints 
     * out the top k matching terms.
     * @param args takes the name of an input file and an integer k as command-line arguments
     */
    public static void main(String[] args) {
        // initialize autocomplete data structure
        In in = new In(args[0]);
        int N = in.readInt();
        String[] terms = new String[N];
        double[] weights = new double[N];
        for (int i = 0; i < N; i++) {
            weights[i] = in.readDouble();   // read the next weight
            in.readChar();                  // scan past the tab
            terms[i] = in.readLine();       // read the next term
        }

        Autocomplete autocomplete = new Autocomplete(terms, weights);

        // process queries from standard input
        int k = Integer.parseInt(args[1]);
        while (StdIn.hasNextLine()) {
            String prefix = StdIn.readLine();
            for (String term : autocomplete.topMatches(prefix, k)) {
                StdOut.printf("%14.1f  %s\n", autocomplete.weightOf(term), term);
            }
        }
    }

    /**
     * Creates a comparator based on the given alphabet
     */
    public class WeightComparator implements Comparator<String> {

        /**
         * Initializes a data structure with the given alphabet
         * @param a String 
         * @param b String
         * @return int
         */
        public int compare(String a, String b) {
            //we want descending order
            double result = termWeights.get(a) - termWeights.get(b); 
            if (result > 0) {
                return -1;
            } else if (result < 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
