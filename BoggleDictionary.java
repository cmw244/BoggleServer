package cs671;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/** A dictionary, specialized for the Boggle game.  In addition to
 * usual word lookup, a dictionary offers a prefix lookup to decide
 * if a string is a prefix of an existing word.  In this class, a
 * dictionary is implemented as a sorted array of words (a better
 * implementation would use tries).  Method <code>find</code> is
 * implemented as a single binary search, and <code>hasWord</code> and
 * <code>hasPrefix</code> are implemented in terms of
 * <code>find</code> (a call to <code>find</code> should be used
 * preferably to a call to <code>hasWord</code> <em>and</em> a call to
 * <code>hasPrefix</code>.)  Dictionary words are stored in capital
 * letters; a word/prefix that contains lowercase letters will never
 * be found.
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 * @see Arrays#binarySearch(Object[],Object)
 */
public class BoggleDictionary implements Iterable<String> {

  private final String[] dict;
  private final java.util.ArrayList<String> col;
          
  /** Constructs a new dictionary by iterating over a collection of words.
   *
   * @param c a collections of words
   */
  public BoggleDictionary (java.util.Collection<String> c) {
    dict = new String[c.size()];
    col = new java.util.ArrayList<>(c);
    Collections.sort(col);
    int i = 0;
    for(String s : col) {
        dict[i++] = s;
    }
  }

  /** An iterator over the dictionary.  Words are returned in
   * alphabetical order.  The iterator does not support word removal.
   *
   * @return an iterator over the dictionary, in order
   */
    @Override
  public Iterator<String> iterator () {
    return col.iterator();
  }

  /** Word/prefix lookup.  This is implemented as a single binary
   * search, so this method should be preferred to
   * <code>hasWord</code> and <code>hasPrefix</code> when both
   * answers are needed.
   *
   * @param string the word/prefix to look for
   * @return a positive value if the string is a dictionary word, 0
   * if the string is not a word but is a prefix of a word, and a
   * negative value otherwise.
   * @see #hasWord
   * @see #hasPrefix
   * @see Arrays#binarySearch(Object[],Object)
   */
  public int find (String string) {
    int index = Arrays.binarySearch(dict, string);
    if(index >= 0) { return 1; }
    int newIndex = (index+1) * -1; // index is now where it would be inserted
    if(newIndex < dict.length && dict[newIndex].startsWith(string)){ 
        return 0;
    }
    return -1;
  }

  /** Dictionary size.
   *
   * @return the number of words in the dictionary
   */
  public int size () {
    return dict.length;
  }

  /** Whether the given string is a word in the dictionary.
   *
   * @param word the word to look for
   * @return true iff the string belongs to the dictionary, as a word
   * @see #find
   */
  public boolean hasWord (String word) {
    return Arrays.binarySearch(dict, word) >= 0;
  }

  
  class CMP_PRE implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if(o1.startsWith(o2)) {
                return 0;
            }
            else {
                return o1.compareTo(o2);
            }
        }
  }
  /** Whether the given string is a prefix of a word in the
   * dictionary.  Note that if the string is a dictionary word, the
   * method returns true.
   *
   * @param prefix the prefix to look for
   * @return true iff the string is a prefix of a word that belongs
   * to the dictionary
   * @see #find
   */
  public boolean hasPrefix (String prefix ) {
    return Arrays.binarySearch(dict, prefix, new CMP_PRE()) >= 0;
  }
}
