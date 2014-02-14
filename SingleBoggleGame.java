// $Id: SingleBoggleGame.java 167 2013-03-09 16:14:56Z cs671a $

package cs671;

import java.util.Set;
import java.util.Scanner;
import java.util.regex.Pattern;

import java.io.InputStream;

/** A simple, one-player, no timing game of Boggle.
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 */
public class SingleBoggleGame implements Runnable {

  private final DiceBoggleBoard board;
  private final BoggleDictionary dict;
  private final Scanner in;

  /** Creates a playing board and associates it with the given dictionary.
   *
   * @param x the width of the board
   * @param y the height of the board
   * @param dice the dice
   * @param d the dictionary
   * @see DiceBoggleBoard
   */
  public SingleBoggleGame (int x, int y, Die[] dice, BoggleDictionary d) {
    board = new DiceBoggleBoard(x,y,dice);
    dict = d;
    in = new Scanner(System.in);
  }

  /** Score value of a word.  This implementation uses the following rule:
   * <ul>
   * <li>words with 0, 1 and 2 letters are worth 0
   * <li>words of 8 or more letters are worth 6 points
   * <li>all other words are worth the number of letters in the word minus two
   * </ul>
   * @param word a word
   * @return the score value of the given word
   */
  public static int score (String word) {
    return Math.max(Math.min(word.length() - 2, 6), 0);
  }

  private String readLine () {
    try {
      return in.nextLine();
    } catch (java.util.NoSuchElementException e) {
      return null;
    }
  }

  /** Starts a terminal based interaction with the player.
   */
  public void run () {
    System.out.printf("There are %d words in the dictionary.%n", dict.size());
    int totalScore;
    Set<String> playerWords = new java.util.HashSet<String>();
    while (true) {
      totalScore = 0;
      playerWords.clear();
      //board.rattle();
      System.out.println("\n"+board+"\n");
      Set<String> allWords = board.allWords(dict);
      int maxScore = 0;
      for (String word : allWords)
        maxScore += score(word);
      maxScore += 3;
      System.out.printf("There are %d possible words.%n", allWords.size());
      for(String s : allWords) {
          //System.out.println("   " + s + " : " + dict.find(s));
      }
      System.out.printf("The maximum possible score is %d.%n", maxScore);
      while (totalScore < maxScore) {
        System.out.print("> ");
        
        String word = readLine();
        if (word == null)
          return;
        word = word.trim().toUpperCase();
        if (word.equals("."))
          break;
        if (word.equals("?")) {
          System.out.println("\n"+board+"\n");
          continue;
        }
        System.out.println("Board thinks (contain) " + board.containsString(word));
        System.out.print("Dict thinks (find) " + dict.find(word) + "\n");
        if (!dict.hasWord(word)) {
          System.out.printf("%s is not in the dictionary.%n", word);
          continue;
        }
        if (!allWords.contains(word)) { // or use board.containsString
          System.out.printf("%s is not on the board.%n", word);
          continue;
        }
        if (playerWords.add(word)) {
          int score = score(word);
          totalScore += score;
          System.out.printf("%s accepted for %d point%s%n",
                            word, score, (score > 1? "s." : "."));
        } else {
          System.out.printf("%s was already found!%n", word);
          continue;
        }
      }
      if (totalScore == maxScore) {
        System.out.printf("%nCongratulation!  You found all the words!");
      } else {
        int n = playerWords.size();
        System.out.printf("You found %d word%s and your score is %d.%n",
                          n, (n > 1?"s":""), totalScore);
        System.out.printf("You didn't find the following words: ");
        allWords.removeAll(playerWords);
        for (String word : allWords)
          System.out.printf("%s ", word.toLowerCase());
      }
      System.out.printf("%n%nPlay again? ");
      String line = readLine();
      if (line == null || !line.toUpperCase().startsWith("Y"))
        break;
    }
    System.out.println("Bye");
  }

  private final static Pattern gridSize =
    Pattern.compile("(?i:([0-9]+)x([0-9]+))");

  private static void usage () {
    System.out.println
      ("Options:\n"+
       "-size <number> : creates a square board\n"+
       "-size <number>x<number> : creates a rectangular board\n"+
       "-length <number> : minimal length for valid words\n"+
       "-dict <file> : dictionary filename\n"+
       "-dice <file> : dice definition filename\n\n"+
       "default is: -size 4 -length 3 -dict words.txt -dice dice.txt");
  }

  /** Starts the program.  The game accepts the following options:
   * <pre>
   -size &lt;number&gt; : creates a square board
   -size &lt;number&gt;x&lt;number&gt; : creates a rectangular board
   -length &lt;number&gt; : minimal length for valid words
   -dict &lt;file&gt; : dictionary filename
   -dice &lt;file&gt; : dice definition filename
   default is: -size 4 -length 3 -dict words.txt -dice dice.txt
   </pre>
  */
  public static void main (String[] args) {
    int minLength = 3;
    String dictName = "/words.txt";
    String diceName = "/dice2.txt";
    int x = 4, y = 4;
    for (int i=0; i<args.length; i++) {
      try {
        if (args[i].equals("-help")) {
          usage();
          return;
        }
        if (args[i].equals("-size")) {
          String size = args[++i];
          try {
            java.util.regex.Matcher m = gridSize.matcher(size);
            if (m.matches()) {
              x = Integer.parseInt(m.group(1));
              y = Integer.parseInt(m.group(2));
            } else {
              x = y = Integer.parseInt(size);
            }
          } catch (NumberFormatException e) {
            System.err.println(e);
            System.err.printf("Unrecognized size; using %dx%d.%n", x, y);
          }
          continue;
        }
        if (args[i].equals("-dict")) {
          dictName = args[++i];
          continue;
        }
        if (args[i].equals("-dice")) {
          diceName = args[++i];
          continue;
        }
        if (args[i].equals("-length")) {
          try {
            minLength = Integer.parseInt(args[++i]);
          } catch (NumberFormatException e) {
            System.err.println(e);
            System.err.printf("Unrecognized length; using %d.%n", minLength);
          }
          continue;
        }
        System.err.printf("Unknown option: %s.%n", args[i]);
      } catch (IndexOutOfBoundsException e) {
        System.err.printf("Incomplete option: %s.%n", args[i-1]);
        break;
      }
    }
    InputStream dictStream
      = SingleBoggleGame.class.getResourceAsStream(dictName);
    if (dictStream == null) {
      System.err.println("Cannot open dictionary file");
      return;
    }
   Set<String> words = new java.util.HashSet<>();
   Scanner in = new Scanner(dictStream);
   while (in.hasNext()) {
     String w = in.next();
     if (w.length() >= minLength)
       words.add(w.toUpperCase());
   }
   in.close();
   BoggleDictionary dict = new BoggleDictionary(words);
   InputStream diceStream
     = SingleBoggleGame.class.getResourceAsStream(diceName);
   if (diceStream == null) {
     System.err.println("Cannot open dice file");
     return;
   }
   Die[] dice = Die.makeDice(new java.io.InputStreamReader(diceStream));
   SingleBoggleGame game = new SingleBoggleGame(x,y,dice,dict);
   game.run();
  }
}
