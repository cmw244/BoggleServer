/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs671;

/**
 * Class which represents a Boggle Player
 * @author Chris
 */
public class Player implements BogglePlayer {
    /** Name of the boggle player */
    private String name;
    /** Determines if the player is playing the game */
    private boolean isPlaying = false;
    /** Determines if the player is registered and ready to play a game */
    private boolean isReady = false;
    /** Determines if the player wants to quit */
    private boolean quitting = false;
    /** Determines if the player is registered for the game */
    private boolean isRegistered = false;
    /** Board for the player to play on */
    private BoggleBoard board;
    
    /**
     * Contstructs player with name <code>s</code>
     * @param s Name of player
     */
    public Player(String s) {
        name = s;
    }
    
    /**
     * Constructs player without a name initially
     */
    public Player() { }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the player
     * @param s Name 
     */
    public void setName(String s) {
        name = s;
    }

    @Override
    public void sendMessage(BoggleMessage msg) {
        if(msg.type == BoggleMessage.Type.BOARD) {
            String line = msg.body;
            int width = Integer.parseInt(line.substring(0,1));
            int height = Integer.parseInt(line.substring(2,3));
            char [] charArr = new char[4];
            for(int i = 3; i < line.length(); i++) {
                charArr[i-3] = line.charAt(i);
            }
            board = new FixedBoggleBoard(width, height, charArr);
            System.out.println("---------------BOARD---------------");
            System.out.println(board.toString());
        }
        else if(msg.type == BoggleMessage.Type.TEXT) {
            String line = msg.body;
            System.out.println("Text is " + line);
        }
    }      
    
    /**
     * 
     * @return True if player is playing 
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * Sets whether or not the player is playing
     * @param b Boolean to see if player is playing
     */
    public void setPlaying(boolean b) {
        isPlaying = b;
    }
    
        /**
     * 
     * @return True if player is ready to quit 
     */
    public boolean isQuitting() {
        return quitting;
    }
    
    /**
     * Alerts player it is ready to quit
     * @param b Boolean to see if player is ready to quit
     */
    public void setQuit(boolean b) {
        quitting = true;
    }
    
        /**
     * 
     * @return True if player is ready 
     */
    public boolean isReady() {
        return isReady;
    }
    
    /**
     * Sets whether or not the player is ready to play
     * @param b Boolean to see if player is ready to play
     */
    public void setReady(boolean b) {
        isReady = b;
    }
    
    /**
     * 
     * @return True if player is registered
     */
    public boolean isRegistered() {
        return isRegistered;
    }
    
    /**
     * Registers the player for the game
     */
    public void register() {
        
        isRegistered = true;
    }
}
