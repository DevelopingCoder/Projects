import java.util.Scanner;
import java.util.HashSet;
import java.util.Comparator;

/**
 * AlphabetSort sorts a list of words into alphabetical order, 
 * according to a given permutation of some alphabet
 * @author Anthony Tan
 */
public class AlphabetSort {

    /**
     * Scans the file coming in and then calls the sort function
     * @param args Array we don't care what it is
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        sort(in);
    }

    /**
     * Sorts the incoming file in order of the given alphabet
     * @param scanner Scanner the alphabet file that we read from
     * runs in O(MN) time where N is the number of words and M is the max length of the words
     * @return
     */
    public static void sort(Scanner scanner) {
        if (!scanner.hasNext()) {
            throw new IllegalArgumentException("requires an alphabet");
        }
        String alphabet = scanner.next();

        //check uniqueness in alphabet
        HashSet<Character> checkUniqueness = new HashSet<Character>();
        for (int i = 0; i < alphabet.length(); i++) {
            checkUniqueness.add(alphabet.charAt(i));
        }
        if (alphabet.length() != checkUniqueness.size()) {
            throw new IllegalArgumentException("A letter appears multiple times in the alphabet.");
        }

        //Add the words into the try based on our dictionaries
        if (!scanner.hasNext()) {
            throw new IllegalArgumentException("No words are given");
        }
        Trie alphabetTrie = new Trie(new AlphabetComparator(alphabet));
        while (scanner.hasNext()) {
            boolean inAlphabet = true;
            String word = scanner.next();
            //check to ensure each letter is in the alphabet
            for (int i = 0; i < word.length(); i++) {
                if (!checkUniqueness.contains(word.charAt(i))) {
                    inAlphabet = false;
                    break;
                }
            }
            if (inAlphabet) {
                alphabetTrie.insert(word);   
            }
        }

        //traverse and print the trie
        StringBuilder start = new StringBuilder();
        printWords(alphabetTrie.getTopNode(), start);
    }

    /**
     *recursively prints all words starting from the top node
     * runs in O(MN) time where N is the number of words and M is the max length of the words
     * @param node Node the node of the trie we are traversing from
     * @param currWord StringBuilder. we build up currWord each time we traverse the trie
     * @return
     */
    public static void printWords(Trie.Node node, StringBuilder currWord) {           
        if (node.isFullWord()) {
            System.out.println(currWord);
        }
        if (node.getLinks().size() == 0) { //reached a stopping point
            return;
        }
        StringBuilder original = new StringBuilder(currWord.toString());
        for (Character x: node.getLinks().keySet()) {
            //build a new string for each starting letter
            StringBuilder letter = new StringBuilder(original.toString());
            letter.append(x);
            printWords(node.getLinks().get(x), letter);
        }         
    }

    /**
     * Creates a comparator based on the given alphabet
     */
    public static class AlphabetComparator implements Comparator<Character> {
        String order;

        /**
         * Initializes a data structure with the given alphabet
         * @param alphabet String the order of words for a particular dictionary
         * runs in O(1) time
         */
        public AlphabetComparator(String alphabet) {
            order = alphabet;
        }
        
        /**
         * Tells the trie the order in which to sort words
         * @param x Character the first word compared
         * @param y Character the second word compared
         * @return int. Tells us whether x or y comes first in our trie
         * runs in O(1) time
         */
        public int compare(Character x, Character y) {
            return order.indexOf(x) - order.indexOf(y);
        }
    }
}
