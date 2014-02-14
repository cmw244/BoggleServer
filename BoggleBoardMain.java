package cs671;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/** Boggle board super class which contains getter and setter methods and 
 *  implements the algorithms 
 *
 * @author Chris Len
 * @version 2.0, 02/27/13
 */
public class BoggleBoardMain implements BoggleBoard {
    /** Save value of the width of the board */
    protected final int width;
    /** Save value of the height of the board */
    protected final int height;
    /** Save value of the characters on the board */
    protected char[][] letters;
    /** 2-d array of the dice on the board */
    protected Die[][] board;
    /** boolean 2-d array corresponds to the board, used to determine if a letters lisited */
    protected boolean[][] marked;
    /** Set which contains all words that have been founds in the dictionary */
    protected SortedSet<String> set;

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
    public BoggleBoardMain (int w, int h, Die[] d) {
    if(w < 1) { throw new IllegalArgumentException("Width must be greather then one"); }
    if(h < 1) { throw new IllegalArgumentException("Height must be greater then one"); }
    if(d.length < 1) { throw new IllegalArgumentException("Number of dice must be greater then one"); }
           
    width = w;
    height = h;
      
    board = new Die[height][width];
    letters = new char[height][width];
    marked = new boolean[height][width];
  
    Die[] copyLoc = new Die[d.length];
    for(int i = 0; i < d.length; i++) {
        copyLoc[i] = new Die(d[i]);
    }
      
    // Put dice on the board in board-order
    int count = 0;
    for(int row = 0; row < h; row++) {
        for(int col = 0; col < w; col++) {
        letters[row][col] = copyLoc[count].top();
        board[row][col] = copyLoc[count++];
        marked[row][col] = false;
        if(count >= d.length) { count = 0; }
      }    
    }   
  }
    
   /** Builds a fixed board.  The array of letters must contain
   * exactly <code>w*h</code> characters, which are used to fill the
   * board in a top-to-bottom, left-to-right fashion.
   *
   * @param w the width of the board
   * @param h the height of the board
   * @param letters exactly enough letters to fill the board
   */
  public BoggleBoardMain (int w, int h, char[] letters) {
      if(w < 1) { throw new IllegalArgumentException("Width must be greather then one"); }
      if(h < 1) { throw new IllegalArgumentException("Height must be greater then one"); }
      
      width = w;
      height = h;
      
      if(letters.length != w*h) {
          System.err.println("Not enough characters given!");
          return;
      }
      
      this.letters = new char[h][w];
      marked = new boolean[height][width];

      
      int count = 0;
      for(int row = 0; row < h; row++) {
          for(int col = 0; col < w; col++) {
              this.letters[row][col] = letters[count++];
              marked[row][col] = false;
          }
      }
  }

    @Override
  public char letterAt (int row, int col) {
    if(row >= height || row < 0 || col >= width || col < 0) {
        System.err.println("Invalid coordinate: " + row + ", " + col);
        return '\0';
    }
    return letters[row][col];
  }

    @Override
  public int getWidth () {
    return width;
  }

    @Override
  public int getHeight () {
    return height;
  }

    @Override
  public boolean containsString (String string) {
    if(string.isEmpty()) { return true; }
    for(int row = 0; row < height; row++) {
      for(int col = 0; col < width; col++) {
        if(searchContain(row, col, string)) {
            return true;
        }
      }    
    } 
    return false;
  }
    
    
  /**
   * Helper function for determining if a string is on the board
   * @param row Row on the board
   * @param col Col on the board
   * @param search String to determine if it is on the board
   * @return true is contained
   */
  private boolean searchContain(int row, int col, String search) {
    if(row >= height || row < 0 || col >= width || col < 0) {
        return false;
    }
    
    if(search.isEmpty()) { return true; }

    if(search.charAt(0) != letters[row][col] || marked[row][col]) {
        return false;
    }
    String newSearch = search.substring(1);
    marked[row][col] = true;
    
    for (int r = -1; r <= 1; r++) {
        for (int c = -1; c <= 1; c++) {
            if(searchContain(row + r, col + c, newSearch)) {
                marked[row][col] = false;
                return true;
            }
        }
     }
    marked[row][col] = false;
    return false;
  }

    @Override
  public SortedSet<String> allWords (BoggleDictionary dict) {
    // Orders the set first based on length then alphabetically
    set = new TreeSet<>(new SET_SORT());
    for(int row = 0; row < height; row++) {
      for(int col = 0; col < width; col++) {
        searchAll(row, col, "", dict);
      }
    }
    
    return set;
  }
    
    /**
     * Sorting class; sorts first based on length then alphabetically
     */
   class SET_SORT implements Comparator<String>{
    @Override
    public int compare(String o1, String o2) {
        if(o1.length() > o2.length()) {
            return -1;
        }
        else if(o1.length() < o2.length()) {
            return 1;
        }
        else {
            return o1.compareTo(o2);
        }
    }
  }

  /**
   * 
   * @param row Row on the board we are at
   * @param col Col on the board we are at
   * @param search String which has been populated so far
   * @param dict String dictionary
   */
  private void searchAll(int row, int col, String search, BoggleDictionary dict) {
    if(row >= height || row < 0 || col >= width || col < 0) {
        return;
    }
    if(marked[row][col]) {
        return;
    }
    search += letters[row][col];
    
    if(dict.hasWord(search)) {
        set.add(search);
    }
    if(dict.hasPrefix(search)) {
     marked[row][col] = true;
     for (int r = -1; r <= 1; r++) {
        for (int c = -1; c <= 1; c++) {
           searchAll(row + r, col + c, search, dict);
        }
     }
     marked[row][col] = false;
    }
  }
  
    @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for(int row = 0; row < height; row++) {
        for(int col = 0; col < width; col++) {
            b.append(letters[row][col]).append(" ");
        }
        b.append("\n");
    }
    return b.toString();
  }
}