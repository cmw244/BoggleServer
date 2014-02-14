package cs671;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/** A playable Boggle board (or grid).  The board contains dice and
 * can be "rattled" to get random configurations.
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 */
public class DiceBoggleBoard extends BoggleBoardMain implements BoggleBoard {
  /** Saves die array so we can rattle easier */
  private Die[] saveDie;
  /** Creates a new board.  The board has the specified dimensions
   * and uses <em>copies</em> of the dice given in argument.  If
   * there aren't enough dice in the array, some dice will be copied
   * more than once.
   *
   * @param w the width of the board
   * @param h the height of the board
   * @param d an array of dice
   * @throws IllegalArgumentException if the width, the height or
   * the number of dice is less than 1
   */
  public DiceBoggleBoard (int w, int h, Die[] d) {
    super(w,h,d);
    saveDie = d;
  }

  /** "Rattles" the board by shaking it to randomize its letters.
   * This method involves two operations: first, dice are randomly
   * permuted, then each die is "rolled".
   *
   * @see Die#roll
   */
  public void rattle () {
    // Part one random permuattion
    Collections.shuffle(Arrays.asList(saveDie)); // shuffles single-d array
    // reput dice into array
    int count = 0;
    for(int row = 0; row < height; row++) {
        for(int col = 0; col < width; col++) {
        board[row][col] = saveDie[count++];
        if(count >= saveDie.length) { count = 0; }
      }    
    } 
    // Part two rolling
    for(int row = 0; row < height; row++) {
      for(int col = 0; col < width; col++) {
        char c = board[row][col].top();
        board[row][col].roll();
        letters[row][col] = board[row][col].top();
      }    
    }  
  }
}