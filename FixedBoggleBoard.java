package cs671;

/** A fixed Boggle board.  These boards are intended for puzzle
 * solving and cannot be "rattled" (the {@code rattle} method has no
 * effect).
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 */
public class FixedBoggleBoard extends BoggleBoardMain implements BoggleBoard {

  /** Builds a fixed board.  The array of letters must contain
   * exactly <code>w*h</code> characters, which are used to fill the
   * board in a top-to-bottom, left-to-right fashion.
   *
   * @param w the width of the board
   * @param h the height of the board
   * @param letters exactly enough letters to fill the board
   */
  public FixedBoggleBoard (int w, int h, char[] letters) {
      super(w,h,letters);
  }

  /**
   * Sets the letter on the board to the given argument
   * @param row Row on the board to set the letter 
   * @param col Col on the board to set the letter
   * @param c Char to set the letter on the board to
   */
  public void setLetter (int row, int col, char c) {
    if(row >= height || row < 0 || col >= width || col < 0) {
        System.err.println("Invalid coordinate: " + row + ", " + col);
        return;
    }
    letters[row][col] = c;
  }
}