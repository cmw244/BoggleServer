package cs671;

import java.io.Reader;
import java.io.BufferedReader;

import java.util.List;

/** An implementation of a (generalized) Boggle die.  Dice can have
 * any number of faces (at least one) and each face contains a letter.
 * Dice can be rolled for randomization.
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 */
public class Die {

  static final java.util.Random rand = new java.util.Random();

  private final char[] faces;

  private int top;

  /** Constructs a new die.  The number of faces in the length of
   * the array, and the top of the dice is the first letter in the
   * array.
   *
   * @param letters an array of letters, one per face, top face first
   * @throws IllegalArgumentException if the array is empty
   */
  public Die (char[] letters) {
    if (letters.length < 1)
      throw new IllegalArgumentException("no empty die");
    faces = new char[letters.length];
    for (int i=0, l=letters.length; i<l; i++)
      faces[i] = Character.toUpperCase(letters[i]);
  }

  /** Makes a copy of the given die.  The new die is identical to
   * the source: it shows the same letter on top.
   *
   * @param source the die to copy
   */
  public Die (Die source) {
    this(source.faces);
    this.top = source.top;
  }

  /** Reads a list of die definitions, one per line.  Die definitions
   * are strings of characters.  Each string denotes a die that has as
   * many faces as there are characters in the string, top face first.
   * It is not required that all strings have the same length.
   * Leading and trailing whitespaces are ignored.
   *
   * @param r a reader from which to read die definitions, as strings
   * @return the newly constructed dice, in the order in which
   * strings were returned by the reader.
   */
  public static Die[] makeDice (Reader r) {
    BufferedReader in = new BufferedReader(r);
    List<Die> dice = new java.util.ArrayList<Die>();
    String line;
    try {
      while ((line = in.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty())
          dice.add(new Die(line.toCharArray()));
      }
    } catch (java.io.IOException e) {
      System.err.println(e);
    }
    return dice.toArray(new Die[dice.size()]);
  }

  /** This die's dimension.
   *
   * @return the number of faces of this die
   */
  public int dimension () {
    return faces.length;
  }

  /** The top face of this die.  This is the letter currently "showing".
   *
   * @return the letter on top of this die
   */
  public char top () {
    return faces[top];
  }

  /** Rolls the die.  One of the die faces is randomly chosen to
   * become the new top.
   */
  public void roll () {
    top = rand.nextInt(faces.length);
  }

  /** A string representation of the die.
   *
   * @return the character on the top face, as a one character string
   */
  public String toString () {
    return Character.toString(top());
  }
}
