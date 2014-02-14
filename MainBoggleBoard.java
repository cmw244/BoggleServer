/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs671;

import java.util.SortedSet;

/**
 *
 * @author Chris
 */
public class MainBoggleBoard implements BoggleBoard{
    private final int width;
    private final int height;
    private Die[][] board;
    
    private MainBoggleBoard(int w, int h, Die[] d) {
        width = w;
        height = h;
    }
     
    @Override
    public int getWidth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getHeight() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char letterAt(int row, int col) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsString(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedSet<String> allWords(BoggleDictionary dict) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
