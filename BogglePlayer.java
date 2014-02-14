package cs671;

/** A Boggle player, as used by a {@code BoggleGameManager} (server side).
 *
 * @author Michel Charpentier
 * @version 2.0, 02/27/13
 * @see BoggleGameManager
 */
public interface BogglePlayer {

  /** The player's name. */
  public String getName ();

  /** Sends a message to the player. */
  public void sendMessage (BoggleMessage msg);

}