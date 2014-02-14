package cs671;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** Implements the messages used in the boggle protocol.  The protocol
 * is described in the description of class {@code BoggleServer}.
 * This class offers utility methods to parse and display messages.
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 * @see BoggleServer
 */
public class BoggleMessage {

  /** Character used to separate the header and body parts
   * of a message.  Following the protocol specification, this is a
   * colon (:).
   */
  public static final char SEPARATOR = ':';

  /** Message types.  These are used as headers when messages are
   * transmitted.
   */
  public static enum Type {
    /** The board (server -> client).  The body of a message
     * of type {@code BOARD} consists of 4 parts: the width of
     * the board, the letter "x", the height of the board, and
     * the letters in the board (in upper case).
     */
      BOARD,
      /** A line of text (server -> client)
       */
      TEXT,
      /** First message to join the game server (client -> server)
       */
      JOIN,
      /** To start the game (client -> server)
       */
      PLAY,
      /** To submit a word (client -> server)
       */
      WORD,
      /** To finish a game (client -> server)
       */
      END
      }

  /** The 'header' (or 'type') or this message. */
  public final Type type;

  /** The 'body' part of this message. This should be an empty string (not
   * a <code>null</code>) if the body of the message is empty
   */
  public final String body;

  /** Message constructor.  This builds a new instance with the given
   * header and body.  If the body contains newlines, they are
   * replaced by whitespaces.
   *
   * @param type the type of the message
   * @param body the content of the message
   * @throws NullPointerException if {@code body} is {@code null}
   */
  public BoggleMessage (Type type, String body) {
    if (body == null)
      throw new NullPointerException();
    this.type = type;
    this.body = body.replace('\n', ' ');
  }

  /** A string representation of this message.  It consists of the
   * header and body parts, separated by colons (:).  This
   * string can be used in I/O between entities that communicate
   * using the boggle protocol.
   *
   * @return a string representation of this message
   */
  @Override public String toString () {
    return type+String.valueOf(SEPARATOR)+body;
  }

  /** Message equality. */
  @Override public boolean equals (Object o) {
    if (o instanceof BoggleMessage) {
      BoggleMessage m = (BoggleMessage)o;
      return m.type == this.type && m.body.equals(this.body);
    }
    return false;
  }

  /** HashCode, consistent with equality. */
  @Override public int hashCode () {
    return 17 * type.hashCode() + body.hashCode();
  }

  private static final Pattern msg =
    Pattern.compile("([^"+SEPARATOR+"]*)"+SEPARATOR+"(.*)");

  /** Utility method to parse a string into a message.
   *
   * @param s a string to parse
   * @return a new message based on the parsed string
   * @throws Message.Exception if the string cannot be parsed into a message
   */
  public static BoggleMessage parse (String s) throws BoggleMessage.Exception {
    Matcher m = msg.matcher(s);
    if (m.matches())
      try {
        return new BoggleMessage(Type.valueOf(m.group(1)),m.group(2));
      } catch (IllegalArgumentException e) {
        throw new BoggleMessage.Exception("Unknown message type: "+s);
      }
    throw new BoggleMessage.Exception("Missing separator");
  }

  /** Message related exception.  This exception occurs when a
   * string cannot be parsed into a message.
   *
   * @see #parse
   */
  public static class Exception extends java.lang.Exception {
    static final long serialVersionUID = 1;
    /** Constructs a new exception with the given message. */
    public Exception (String message) {
      super(message);
    }
  }
}
