package cs671;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** A simple text-based interface for the Boggle game.  This interface
 * accepts the following commands:
 * <ul>
 * <li><tt>PLAY</tt>: indicates that the player is ready to start a new game
 * <li><tt>END</tt>: indicates that the player is ready to end the current game
 * <li><tt>BOARD</tt>: redisplays the board
 * <li>any other string is submitted as a word
 * </ul>
 * Words can be submitted is lowecase or uppercase, which means that
 * <tt>play</tt>, <tt>end</tt> and <tt>board</tt> can be submitted as
 * words by not having them be uppercase.
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 * @see BoggleServer
 */
public class BoggleTextUI {

  private final ClientPlayer player;
  private final TerminalHandler terminalHandler;
  private String board;

  /** Constructor. */
  private BoggleTextUI (String name, String hostname, int port) {
    player = new ClientPlayer(name, hostname, port);
    terminalHandler = new TerminalHandler();
  }

  private class TerminalHandler extends Thread {
    public TerminalHandler () {
      super("Terminal Handler");
    }
    public void run () {
      usage();
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      while (player.isAlive()) {
        String line = null;
        try {
          line = in.readLine();
        } catch (java.io.IOException e) {
          System.err.println("I/O exception on terminal");
        }
        if (line == null) {
          player.bye();
          break;
        }
        line = line.trim();
        if (line.equals("PLAY")) {
          player.play();
          continue;
        }
        if (line.equals("END")) {
          player.end();
          continue;
        }
        if (line.equals("BOARD")) {
          displayBoard();
          continue;
        }
        if (!line.equals(""))
          player.submitWord(line);
      }
    }
  }

  private static final BoggleMessage play
    = new BoggleMessage(BoggleMessage.Type.PLAY,"");
  private static final BoggleMessage end
    = new BoggleMessage(BoggleMessage.Type.END,"");

  /** Matches the body of a BOARD message: <width>x<height><letters> */
  private static final Pattern BOARD
    = Pattern.compile("(?i:([0-9]+)x([0-9]+)(.*))");

  private class ClientPlayer extends Thread {

    private final String hostname;
    private final int port;

    private OutputStreamWriter out;
    private BufferedReader in;
    private Socket socket;

    public ClientPlayer (String name, String hostname, int port) {
      super(name);
      this.hostname = hostname;
      this.port = port;
    }

    public void run () {
      if (port < 0 || port > 65535) {
        displayText("Invalid port number [0..65535].");
        bye();
        return;
      }
      synchronized (this) {
        try {
          socket = new Socket(hostname, port);
          socket.setKeepAlive(true);
        } catch (java.net.UnknownHostException e) {
          displayText("Unknown host: "+hostname);
          bye();
          return;
        } catch (java.net.ConnectException e) {
          displayText("Connection refused by server.");
          bye();
          return;
        } catch (java.io.IOException e) {
          displayText("Cannot create socket.");
          bye();
          return;
        }
        try {
          out = new OutputStreamWriter(socket.getOutputStream());
          in = new BufferedReader
            (new InputStreamReader(socket.getInputStream()));
        } catch (java.io.IOException e) {
          displayText("Cannot open socket streams.");
          bye();
          return;
        }
      }
      sendMessage(new BoggleMessage(BoggleMessage.Type.JOIN, getName()));
      displayText(hostname + " successfully contacted.");
      while (!socketClosed()) {
        String line;
        try {
          line = in.readLine();
        } catch (java.io.IOException e) {
          line = null;
        }
        if (line == null) {
          break;
        }
        BoggleMessage m;
        try {
          m = BoggleMessage.parse(line);
        } catch (BoggleMessage.Exception e) {
          displayText("Incoherent message from server.  Quitting.");
          bye();
          break;
        }
        if (m.type == BoggleMessage.Type.TEXT) {
          displayText(m.body);
          continue;
        }
        if (m.type == BoggleMessage.Type.BOARD) {
          Matcher ma = BOARD.matcher(m.body);
          if (ma.matches())
            try {
              int w = Integer.parseInt(ma.group(1));
              int h = Integer.parseInt(ma.group(2));
              displayBoard(w,h,ma.group(3).toCharArray());
              continue;
            } catch (NumberFormatException e) {}
          displayText("Cannot parse board from server.  Quitting.");
          bye();
          break;
        }
      }
    }

    public synchronized void bye() {
      try {
        if (out != null) out.close();
        if (in != null) in.close();
        if (socket != null) socket.close();
      } catch (java.io.IOException e) {
        // do nothing
      } finally {
        displayText("Client player terminated.");
      }
    }

    private synchronized void sendMessage (BoggleMessage m) {
      try {
        if (out != null) {
          out.write(m.toString());
          out.write('\n');
          out.flush();
        }
      } catch (java.io.IOException e) {
        displayText("Cannot contact server.");
      }
    }

    public void play () {
      sendMessage(play);
    }

    public void end () {
      sendMessage(end);
    }

    public void submitWord (String word) {
      word = word.trim().toUpperCase();
      sendMessage(new BoggleMessage(BoggleMessage.Type.WORD, word));
    }

    private synchronized boolean socketClosed () {
      return socket.isClosed();
    }
  }

  private void launch () {
    player.start();
    terminalHandler.start();
  }

  private void displayText (String text) {
    System.out.println(text);
  }

  private void displayBoard (int w, int h, char[] letters) {
    if (w*h != letters.length)
      throw new IllegalArgumentException
        ("Cannot display board: wrong dimensions.");
    StringBuilder b = new StringBuilder();
    int n = 0;
    for (int i=0; i<h; i++) {
      for (int j=0; j<w; j++)
        b.append(letters[n++]).append(" ");
      b.append("\n");
    }
    board = b.toString();
    displayBoard();
  }

  private void displayBoard () {
    if (board == null)
      System.out.println("Board is unknown.");
    else
      System.out.print(board);
  }

  private static void usage () {
    System.out.println
      ("This simple interface accepts the following commands:\n"+
       "  PLAY: indicates that the player is ready to start a new game\n"+
       "  END: indicates that the player is ready to end the current game\n"+
       "  BOARD: redisplays the board.\n"+
       "Any other string is submitted as a word.\n");
  }

  public static void main (String[] args) throws NumberFormatException {
    if (args.length != 3) {
      System.err.println("Parameters: player-name hostname port");
      return;
    }
    BoggleTextUI ui
      = new BoggleTextUI(args[0],args[1], Integer.parseInt(args[2]));
    ui.launch();
  }
}
