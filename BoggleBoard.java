package cs671;

import java.util.SortedSet;

/** A Boggle board (or grid).  Two types of boards can be created in
 * this package.  Instances of {@code DiceBoggleBoard} are intended
 * for actual play; they contain dice and can be "rattled" to get
 * random configurations.  Boards created with
 * <code>FixedBoggleBoard</code>, on the other hand, are intended for
 * puzzle solving and cannot be rattled.  Boards offer methods to
 * check if a string can be found and to produce all the words on the
 * board that belong to a dictionary.
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 */
public interface BoggleBoard {

  /** Number of columns in board. */
  public int getWidth ();

  /** Number of rows in board. */
  public int getHeight ();

  /** The board letter at the designated position.  Indexes start with 0.
   *
   * @param col the column
   * @param row the row
   * @return the letter at (row,column)
   */
  public char letterAt (int row, int col);

  /** Checks if a string can be found on the board, according to the
   * rules of Boggle.  The string does not have to be a valid word.
   *
   * @param string the string to look for
   * @return true iff the string is found on the board
   */
  public boolean containsString (String string);

  /** All the dictionary words that can be found on the board.  Words
   * are retuned in order: longest words first and all the words of a
   * given length in alphabetical order.  Dictionary words are
   * expected to be in uppercase letters; words are also returned by
   * this method in uppercase letters.
   *
   * @param dict a dictionary of words
   * @return all the words from the the dictionary that can be found
   * on the board, in order
   */
  public SortedSet<String> allWords (BoggleDictionary dict);
}