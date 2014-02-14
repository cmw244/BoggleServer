package cs671;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;

/** A multi-threaded server for the Boggle game.  This server
 * implements the server side of the Boggle protocol described below.
 *
 * <p>Messages are made of 2 parts: a header and a body, separated by
 * a colon (:).  There are 6 different headers: <tt>BOARD</tt>,
 * <tt>TEXT</tt>, <tt>JOIN</tt>, <tt>PLAY</tt>, <tt>WORD</tt> and
 * <tt>END</tt>.</p>
 *
 * <p>From server to client:
 * <ul>
 * <li><tt><b>BOARD</b></tt>: the board (letter grid) at the beginning
 * of a new game.  The body of this message is structured as follows:
 * <tt>&lt;width&gt;x&lt;height&gt;&lt;LETTERS&gt;</tt>.  Letters are
 * uppercase and are listed top-to-bottom and, within each row,
 * left-to-right (like English reading).</li>
 *
 * <li><tt><b>TEXT</b></tt>: a string of text, which represents
 * information sent by the server to a player.  The body of the
 * message is the string.  It cannot contain newlines.</li>
 * </ul></p>
 *
 * <p>From client to server:
 * <ul>
 * <li><tt><b>JOIN</b></tt>: the player wants to join the server.  The
 * body of the message is the player's name.  It cannot be an empty
 * string.  This must be the first message sent by a client to the
 * server to initiate a connection.</li>
 *
 * <li><tt><b>PLAY</b></tt>: the player is ready to play.  The body of
 * this message is an empty string.  A game starts when all the
 * players are ready, or after a timeout.</li>
 *
 * <li><tt><b>WORD</b></tt>: the player submits a word to the server.
 * The body of the message is the word being submitted, in uppercase.
 * A player cannot submit words before a game has started.</li>
 *
 * <li><tt><b>END</b></tt>: the player is ready to end the game.  The
 * body of this message is an empty string.  A game ends when all the
 * players are ready to end, or after a timeout.</li>
 * </ul>
 * </p> 
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 */
public class BoggleServer {
    /** Port number used to connect the server */
    private final int port;
    /** BoggleGameManager used to play game */
    private final BoggleGameManager game;
    
    /** Determines if server is allowed to accept new connecitons */
    private boolean running;
    /** Makes sure start can not be double called */
    private boolean serverRunning = false;
    /** ServerSocket used to connect to clients */
    private ServerSocket listener = null;
    /** Lock to synchronized code */
    private final Object lock;
    
  /** Creates a server
   *
   * @param port the port number ([0..65535])
   * @param g a game manager
   */
  public BoggleServer (int port, BoggleGameManager g) {
    this.port = port;
    game = g;
    
    lock = new Object();
  }

  /** Starts listening and accepting connections. */
  public void start () throws java.io.IOException {
    if(!serverRunning) {
        if(listener == null) {
            listener = new ServerSocket(port);
        }
        new Thread(new ClientListener()).start();
    }

    synchronized(lock) {
        serverRunning = true;
        running = true;
    }
  }
  
  /**
   * Run by a thread, wait for new connections, creates a new player on new 
   * thread if connection(client) is established 
   */
  class ClientListener implements Runnable {
    @Override
    public void run() {
        Socket server;
        try {
            synchronized(lock) {
                while(running) {
                    server = listener.accept();
                    Player cp = new Player();
                    Thread t = new Thread(new ServerClass(server, cp));
                    t.start();
                }
            }
        }
        catch(Exception | Error e) {
            System.err.println("Failed on port "  + port);
        }
        finally {
            if(listener != null) {
                try {
                    listener.close();
                } 
                catch (IOException ex) { }
            }
        }    
    }
  }
  
  /**
   * Class represents a client connnection, we sit and wait for data, once we get 
   * it we parse it and hand it off to the game manager 
   */
  class ServerClass implements Runnable {
    private Socket socket;
    BufferedReader in;
    PrintWriter out;
    Player cp;
    
    public ServerClass(Socket s, Player p) {
        socket = s;
        cp = p;
    }
    
    @Override
    public void run() {
        boolean calledJoin = false;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            String line;
            while(true) {
                line = getLine();
                if(line == null) {
                    break;
                }
                // Handle line given
                BoggleMessage m = null;
                try {   
                    m = BoggleMessage.parse(line);
                }
                catch(BoggleMessage.Exception e) {
                    System.out.println("Incoherent message from server.  Quitting...");
                    break;
                }
                if(m.type == BoggleMessage.Type.JOIN) {
                    String name = m.body;
                    if(name.isEmpty()) {
                        System.err.println("Name can not be empty!  Skipping player");
                        break;
                    }
                    cp.setName(name);
                    if(!game.register(cp)) { // create new boggle player with name 
                        System.out.println("Could not register player " + cp.getName());
                        break;
                    }
                    calledJoin = true;
                }
                else if(m.type == BoggleMessage.Type.PLAY && calledJoin) {
                    game.start(cp);
                }
                else if(m.type == BoggleMessage.Type.WORD && calledJoin) {
                    String word = m.body;
                    if(game.playing()) {
                        game.submitWord(cp, word);
                    }
                }
                else if(m.type == BoggleMessage.Type.END && calledJoin) {
                    if(!game.remove(cp)) {
                        System.out.println("Could not be remove player " + cp.getName());
                    }
                }
            }
        }
        catch(Exception | Error e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        finally { 
            bye();
        }
    }
    
    /**
     * Gets line from connection
     * @return Line which was read by server
     */
    private String getLine() {
        String line;
        try {
            line = in.readLine();
        }
        catch(java.io.IOException e) {
            line = null;
        }
        return line;
    }
    
    /**
     * Closes all open file descriptors and streams gracefully
     */
    public synchronized void bye() {
      try {
        if (out != null) { out.close(); }
        if (in != null) { in.close(); }
        if (socket != null) { socket.close(); }
      } catch (java.io.IOException e) {
        // do nothing
      } finally {
        System.out.println("Server client terminated.");
      }
    }
      
  }

  /** Stops the server.  The server can later be restarted. */
  public void stop () {
    synchronized(lock) {
        running = false;
        serverRunning = false;
    }
  }

  private static void usage () {
    System.out.println
      ("port number required, followed by options:\n"+
       "-size <number> : creates a square board\n"+
       "-size <number>x<number> : creates a rectangular board\n"+
       "-length <number> : minimal length for valid words\n"+
       "-dict <file> : dictionary filename\n"+
       "-dice <file> : dice definition filename\n"+
       "-time <time> : timers, in seconds\n\n"+
       "default is: "+
       "-size 4 -length 3 -dict words.txt -dice dice.txt -time 180");
  }

  private final static java.util.regex.Pattern gridSize =
    java.util.regex.Pattern.compile("(?i:([0-9]+)x([0-9]+))");

  /** Starts the server with a new game manager.  The first
   * command-line argument is the port number to listen to.  It can
   * be followed by several options:
   * <pre>
   -size &lt;number&gt; : creates a square board
   -size &lt;number&gt;x&lt;number&gt; : creates a rectangular board
   -length &lt;number&gt; : minimal length for valid words
   -dict &lt;file&gt; : dictionary filename
   -dice &lt;file&gt; : dice definition filename
   -time &lt;time&gt; : timers, in seconds
   default is: -size 4 -length 3 -dict words.txt -dice dice.txt -time 180
   </pre>
   * @see BoggleGameManager
   */
  public static void main (String[] args) {
    int width = 4;
    int height = 4;
    String dictFile = "/words.txt";
    String diceFile = "/dice.txt";
    int minLength = 3;
    int time = 180;
    BoggleDictionary dict;
    Die[] dice;
    if (args.length < 1) {
      usage();
      return;
    }
    int port;
    try {
      port = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      usage();
      return;
    }
    for (int i=1; i<args.length; i++) {
      try {
        if (args[i].equals("-help")) {
          usage();
          return;
        }
        if (args[i].equals("-time")) {
          try {
            time = Integer.parseInt(args[++i]);
          } catch (NumberFormatException e) {
            System.err.printf("Unrecognized time; using %d%n", time);
          }
          continue;
        }
        if (args[i].equals("-size")) {
          String size = args[++i];
          try {
            java.util.regex.Matcher m = gridSize.matcher(size);
            if (m.matches()) {
              width = Integer.parseInt(m.group(1));
              height = Integer.parseInt(m.group(2));
            } else {
              width = height = Integer.parseInt(size);
            }
          } catch (NumberFormatException e) {
            System.err.printf("Unrecognized size; using %dx%d%n", width, height);
          }
          continue;
        }
        if (args[i].equals("-dict")) {
          dictFile = args[++i];
          continue;
        }
        if (args[i].equals("-dice")) {
          diceFile = args[++i];
          continue;
        }
        if (args[i].equals("-length")) {
          try {
            minLength = Integer.parseInt(args[++i]);
          } catch (NumberFormatException e) {
            System.err.printf("Unrecognized length; using %d%n", minLength);
          }
          continue;
        }
        System.err.printf("Unknown option: %s%n", args[i]);
      } catch (IndexOutOfBoundsException e) {
        System.err.printf("Incomplete option: %s%n", args[i-1]);
        break;
      }
    }
    try {
      InputStream dictStream
        = SingleBoggleGame.class.getResourceAsStream(dictFile);
      if (dictStream == null)
        dictStream = new java.io.FileInputStream(dictFile);
      Set<String> words = new java.util.HashSet<>();
      Scanner in = new Scanner(dictStream);
      while (in.hasNext()) {
        String w = in.next();
        if (w.length() >= minLength)
          words.add(w.toUpperCase());
      }
      in.close();
      dict = new BoggleDictionary(words);
    } catch (java.io.IOException e) {
      System.err.printf("Cannot open dictionary file: %s%n", e.getMessage());
      return;
    }
    try {
      InputStream diceStream
        = SingleBoggleGame.class.getResourceAsStream(diceFile);
      if (diceStream == null)
        diceStream = new java.io.FileInputStream(diceFile);
      dice = Die.makeDice(new java.io.InputStreamReader(diceStream));
    } catch (java.io.IOException e) {
      System.err.printf("Cannot open dice file: %s%n", e.getMessage());
      return;
    }
    System.out.printf("Dictionary has %d words.%n", dict.size());
    BoggleGameManager game = new BoggleGameManager(width, height, dict, dice);
    game.setTimer(time);
    BoggleServer server = new BoggleServer(port, game);
    try {
      server.start();
    } catch (java.io.IOException e) {
      System.err.printf("Cannot start server: %s%n", e.getMessage());
    }
  }
}
