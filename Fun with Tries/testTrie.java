import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Iterator;
import java.util.Scanner;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedList;
/**
 * JUnit Tests
 * @author Anthony Tan
 */
public class testTrie {

    @Test
    public void testInsertandFind() {
        Trie t = new Trie();
        t.insert("hello");
        t.insert("hey");
        t.insert("goodbye");
        assertEquals(true, t.find("hell", false));
        assertEquals(true, t.find("hello", true));
        assertEquals(true, t.find("good", false));
        assertEquals(false, t.find("bye", false));
        assertEquals(false, t.find("heyy", false));
        assertEquals(false, t.find("hell", true));
    }
    
    @Test
    public void testAutoCompleteBasics() {
        // initialize autocomplete data structure
        String[] args =  new String[]{"wiktionary.txt"};
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
        Iterator<String> x = autocomplete.topMatches("", 5).iterator();
        assertEquals(" the", x.next());
        assertEquals(" of", x.next());
        assertEquals(" and", x.next());
        assertEquals(" to", x.next());
        assertEquals(" in", x.next());        
        assertEquals(" the", autocomplete.topMatch(" the"));

        
    }
    @Test
    public void testSpellCheck() {
        String[] args =  new String[]{"wiktionary.txt"};
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

        //test LevenshteinDistance
        LinkedList<int[]> rows = new LinkedList<int[]>();
        autocomplete.levenshteinDistance("whut", "what", 0, rows);
        for (int[] row: rows) {
            for (int num: row) {
                System.out.print(num + ", ");
            }
            System.out.println();
        }
        System.out.println();
        rows.removeLast();
        rows.removeLast();
        for (int[] row: rows) {
            for (int num: row) {
                System.out.print(num + ", ");
            }
            System.out.println();
        }
        System.out.println();
        autocomplete.levenshteinDistance("whut", "whit", 2, rows);
        for (int[] row: rows) {
            for (int num: row) {
                System.out.print(num + ", ");
            }
            System.out.println();
        }

        Iterable<String> spellCheck = autocomplete.spellCheck(" whut", 1, 5);
        String[] results = new String[] {" what", " shut", " hut", " whit"};
        int i = 0;
        for (String word: spellCheck) {
            System.out.println(word);
            // assertEquals(results[i], word);   
            i++;    
        }   
        assertEquals(4, i);
    }
    @Test
    public void testAutocompleteFunctionality() {
        // initialize autocomplete data structure
        String[] args =  new String[]{"cities.txt"};
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
        Iterator<String> x = autocomplete.topMatches("Rad", 2).iterator();
        assertEquals("Radom, Poland", x.next());
        assertEquals("Radomsko, Poland", x.next());    
    }

    @Test
    public void testEmptyPrefix() {
        String[] args =  new String[]{"cities.txt"};
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
        Iterator<String> x = autocomplete.topMatches("", 5).iterator();
        assertEquals("Shanghai, China", x.next());
        assertEquals("Buenos Aires, Argentina", x.next());
        assertEquals("Mumbai, India", x.next()); 
    }

    // @Test
    // public void testBoggle() throws IOException {
    //     Boggle.main(new String[] {"-k", "7"});
    // }
    

    public static void main(String[] args) {
        jh61b.junit.textui.runClasses(testTrie.class);
    }
}
