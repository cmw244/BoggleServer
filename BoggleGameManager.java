package cs671;

import java.util.ArrayList;
import java.util.Timer;

/** A Boggle game manager.  A game manager's responsibilities
 * include: build a board, send it to connected players, check words
 * submitted by players and compute final scores.  A game manager
 * also manages timers to force games to start or to finish after a
 * given delay.
 *
 * <p>Instances of this class are thread-safe.
 *
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 */
public class BoggleGameManager {
    private final int width;
    private final int height;
    private final BoggleDictionary dict;
    private final Die[] die;
    private final char[] letters;
    private final BoggleBoard board;
    private final Timer timer;
    private ArrayList<Player> players;
    private ArrayList<BogglePlayer> bogglePlayers;
    private boolean playing = false;
    private int delay = 20000; // intial delay is 20000 ms or 20 seconds
    /** Determines is a new game should start as soon as the current one ends */
    private boolean newGameStart = false;
    private int [] score;
    private ArrayList<WordPack> wordsSubmitted;
    
  /** Creates a new game manager.
   *
   * @param w the width of the board
   * @param h the height of the board
   * @param dict the dictionary
   * @param d the dice
   */
  public BoggleGameManager (int w, int h, BoggleDictionary dict, Die[] d) {
    width = w;
    height = h;
    this.dict = dict;
    die = d;
    board = new DiceBoggleBoard(w, h, d);
    timer = new Timer(true);
    
    letters = new char[w*h];

    for(int i = 0; i < w*h; i++) {
        letters[i] = d[i%d.length].top();
    }
      
    players = new ArrayList<>();
    bogglePlayers = new ArrayList<>();
    wordsSubmitted = new ArrayList<>();
  }
  
  class WordPack {
      private String word;
      private String name;
      
      public WordPack(String word, String name) {
          this.word = word;
          this.name = name;
      }
      
      public String getWord(){ 
          return word;
      }
      
      public String getName() {
          return name;
      }
      
  }

  /** Whether a game is currently on.
   * @return true iff there is a game running
   */
  public boolean playing () {
      return playing;
  }

  private Player findPlayer(BogglePlayer p) {
      Player temp = null;
      for(Player t : players) {
          if(t.getName().equals(p.getName())) {
              temp = t;
          }
      }
      return temp;
  }
  
  /** Whether a player is currently playing a game.
   *
   * @param p the player
   * @return true iff there is a game running and {@code p} is a
   * participant in that game
   */
  public boolean isPlaying (BogglePlayer p) {
    Player pl = findPlayer(p);
    if(pl == null) { return false; }

    return playing && pl.isPlaying();
  }

  /** Adds a player.  The player is not added if its name is already in
   * use.  For convenience, name comparison is case insensitive (i.e.,
   * "John" and "john" are the same player).  The added player is
   * initially "passive", i.e., registered but not wanting to play a
   * game.
   *
   * @param p the player to add
   * @return true iff player {@code p} is effectively registered
   */
  public boolean register (BogglePlayer p) {
    String name = p.getName();
    
    // Look through players to make sure name does not already exist
    for(Player pl : players) {
        if(name.equalsIgnoreCase(pl.getName())) {
            return false;
        }
    }
    Player newPlayer = new Player(p.getName());
    newPlayer.register();
    p.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
            "Player + " + p.getName() + " is registered"));
    players.add(newPlayer);
    bogglePlayers.add(p);
    return true;
  }

  /** Removes a player.  The player can no longer use the services
   * of the server.  If all the remaining players are ready to play/end,
   * a game starts/stops.
   *
   * @param p the player to remove
   * @return true iff the player is effectively removed
   */
  public boolean remove (BogglePlayer p) {
    Player gone = findPlayer(p);
    if(gone == null) { return false; }    
    boolean ret = players.remove(gone) && bogglePlayers.remove(p);
    // NEED TO CHECK TO SEE IF ALL REMAINING PLAYERS ARE READY/DONE
    if(playing) {
        // need to see if everyone is now wanting to end
    }
    else {
        // need to see if everyone is now ready to play
    }
    
    return ret;
  }

  /** The board, as a string.
   * @return a string of the form {@code "<width>x<height><letters>"}.
   * Letters are listed from the top row to the bottom row and each
   * row is enumerated from left to right.
   */
  public String getBoardString () {
    StringBuilder s = new StringBuilder();
    s.append(width).append("x").append(height);
    for(char c : letters) {
        s.append(c);
    }
    return s.toString();
  }

  /** The number of connected players (whether they are currently
   * playing or not).
   */
  public int playerCount () {
    return players.size();
  }

  /** Whether a player is registered with the server.
   *
   * @param p the player
   * @return true iff the player is registered
   */
  public boolean isRegistered (BogglePlayer p) {
    Player temp = findPlayer(p);
    if(temp == null) {
        return false;
    }
    return temp.isRegistered();
  }

  /** Submits a word.  The word is submitted in the name of player
   * {@code p}.  If there is no game on or {@code p} is not part of
   * the current game, the submission is rejected and the player is
   * notified.
   *
   * @param p the player submitting the word
   * @param word the word submitted, in upper case
   * @see BogglePlayer#sendMessage
   */
  public void submitWord (BogglePlayer p, String word) {
    Player play = findPlayer(p);
    if(play == null) {
        return;
    }
    
    for(WordPack w : wordsSubmitted) {
        if(w.getWord() == word) {
            p.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
                "Word submission rejected! " + word + " already submitted by " + p.getName()));
            return;
        }
    }
   
    if(!isPlaying(play)) {
        // Notify player that game is not being played
        p.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
                "Word submission rejected! Player " + play.getName() + " is not playing"));
    }
    else if(!dict.hasWord(word)) {
        p.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
                "Word " + word + " is not in the dictionary"));
    }
    else if(!board.containsString(word)) {
        p.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
                "Word " + word + " is not on the board"));
    }
    else {
        p.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
                "Word " + word + " is found by " + p.getName()));
        WordPack w = new WordPack(word, p.getName());
        wordsSubmitted.add(w);
    }
    
  }

  /** Indicates that player {@code p} wants to play.  All registered
   * players are notified.  If all the registered players want to
   * play, a game starts immediately.  If at least one player wants to
   * play and timers are used, a game will start after the timer delay
   * or after all other players have indicated their desire to play,
   * whichever comes first.  If the player is not registered with the
   * server of is already in the game, the player is notified and the
   * method has no effect.
   *
   * <p>If a game that does not involve the player is already on, this
   * game will have to finish first.  After it is done, a new game
   * will start when all other players are ready or a timer expires.
   * The timer is only started at the end of the current game.
   *
   * @param p the player
   * @see #setTimer
   * @see BogglePlayer#sendMessage
   */
  public void start (BogglePlayer p) {
    Player play = findPlayer(p);
    if(play == null) {
        p.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
                "Player " + p.getName() + " is not registered with server"));
        return;
    }
    
    boolean allPlayersReady = true;
    play.setReady(true);
    for(BogglePlayer t : bogglePlayers) {
        t.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
                "Player " + play.getName() + " is now ready to play"));
    }
    for(Player t : players) {
        if(!t.isReady()) {
            allPlayersReady = false;
            break;
        }
    }
    
    if(allPlayersReady) { // start the game
        beginGame(true);
    }
    else { // start timer
        if(playing) { // dont start timer till current game is over
            newGameStart = true;
        }
        else {
            //timer.
        }
    }
  }

  /** Starts a game.  If all the players are ready, a game starts and
   * players are notified.  If the boolean {@code force} is {@code
   * true}, a game starts even it some registered players are not
   * ready.  These players are not included in the game.  If a game is
   * actually started, the board is sent to all participating players
   * and the method returns {@code true}.
   *
   * @see BogglePlayer#sendMessage
   */
  public boolean beginGame (boolean force) {
    boolean allPlayersReady = true;
    if(!playing) {
        for(Player t : players) {
            if(!t.isReady()) {
                allPlayersReady = false;
                break;
            }
        }
        if(allPlayersReady || force) { // end game
            for(BogglePlayer t : bogglePlayers) {
                t.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT,
                    "Game is starting!"));
            }
            for(Player t : players) {
                if(t.isReady()) {
                    t.setPlaying(true);
                    t.setQuit(false);
                }
            }
            playing = true;
        }
    }
    else { // send board to all players
        for(BogglePlayer t : bogglePlayers) {
            t.sendMessage(new BoggleMessage(BoggleMessage.Type.BOARD,
                width + "x" + height + letters));
        }
        return true;
    }
    return false;
  }

  /** Indicates that player {@code p} wants to end the game.  All
   * players in the current game are notified.  If all the players
   * involved in a game indicate their desire to end the game, the
   * game finishes immediately.  Otherwise, the game finishes after
   * the timer delay, if timers are used.
   *
   * @param p the player
   * @see #setTimer
   * @see BogglePlayer#sendMessage
   */
  public void stop (BogglePlayer p) {
    Player play = findPlayer(p);
    if(play == null) {
        p.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
            "Player " + p.getName() + " was not registered"));
        return;
    }
    boolean allPlayersReady = true;
    
    play.setQuit(true);
    for(BogglePlayer t : bogglePlayers) {
        t.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT, 
                "Player " + play.getName() + " wants the game to be over"));
    }
    for(Player t : players) {
        if(!t.isQuitting()) {
            allPlayersReady = false;
            break;
        }
    }
    if(allPlayersReady) { // end game
        endGame(true);
    }
    else { // have timer get ready to end game
        
    }
    
  }

  /** Ends a game.  If a game is currently running and all the players
   * are ready, the game stops and players are notified.  If the
   * boolean {@code force} is {@code true}, the game stops even it
   * some players are not ready.  If a game is actually stopped,
   * scores are sent to players and the method returns {@code true}.
   *
   * @see BogglePlayer#sendMessage
   */
  public boolean endGame (boolean force) {
    boolean allPlayersReady = true;
    if(playing) {
        for(Player t : players) {
            if(!t.isQuitting()) {
                allPlayersReady = false;
                break;
            }
        }
        if(allPlayersReady || force) { // end game
            for(BogglePlayer t : bogglePlayers) {
                t.sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT,
                    "Game is ending!"));
            }  
            System.err.println("Game is over");
            for(Player t : players) {
                t.setPlaying(false);
                t.setReady(false);
                t.setQuit(false);
            } 
            score = new int[bogglePlayers.size()];
            for(int i = 0; i < bogglePlayers.size(); i++) {
                for(int j = 0; j < wordsSubmitted.size(); j++) {
                    WordPack w = wordsSubmitted.get(i);
                    String word = w.getWord();
                    String name = w.getName();
                    boolean dup = false;
                    System.out.println("words sub " + wordsSubmitted.size());
                    for(WordPack wp : wordsSubmitted) {
                        if(wp.getWord().equals(word) && !wp.getName().equals(name)) { // duplicate found
                            dup = true;
                            break;
                        }
                    }
                    if(!dup) {
                        score[i] += SingleBoggleGame.score(word);
                    }
                }
            }
            for(int i = 0; i < bogglePlayers.size(); i++) {
                bogglePlayers.get(i).sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT,
                        bogglePlayers.get(i).getName() + " score: " + score[i]));
            }
            playing = false;
        }
    }
    else {
        for(int i = 0; i < bogglePlayers.size(); i++) {
            bogglePlayers.get(i).sendMessage(new BoggleMessage(BoggleMessage.Type.TEXT,
                "Game is over. Your score is " + score[i]));
        }
        return true;
    }
    return false;
  }

  /** Sets the delay for timers.  A value of 0 means timers are killed.
   * Otherwise, the value becomes the delay used to start or
   * to end games when not all players are ready.
   *
   * @param seconds the new delay, in seconds
   * @return the previous timer value
   */
  public int setTimer (int seconds) {
      int old = delay;
      delay = seconds;
      if(seconds == 0) {
          timer.cancel();
      }
      return old;
  }
}
