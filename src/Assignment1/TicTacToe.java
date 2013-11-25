/**
 * Description: CIS*3760 - Assignment #1
 * @author Jennfier Winer
 * @since 2013-01-10
 * Created: January 10, 2013
 * Last Modified: January 22, 2013
 */
package Assignment1;

import java.net.*;
import javax.swing.*;

/**
 * The class that contains the main method and various startup 
 * options for the game
 * 
 * @author Jennifer Winer
 */
public class TicTacToe{
    
    /**
     * It is responsible for setting up the game. All of the different startup
     * configurations are controlled by the JOptionPane within this method.
     */
    public static void main(String[] args){
        Object[] startupOptions = {"Create Game", "Join Game", "Quit"};
        Object serverIP;
        int choice;
        
        if(args.length != 1 || (!args[0].equalsIgnoreCase("true") && !args[0].equalsIgnoreCase("false"))){
            System.out.println("This file MUST be run with exactly one argumet (a boolean)");
            return;
        }
        
        
        choice = JOptionPane.showOptionDialog(null, "Would you like to create a new game or join a friend's game?", "Start Game",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, startupOptions, startupOptions[0]);
        
        if(choice == JOptionPane.YES_OPTION){
            try{
                InetAddress myAddress = InetAddress.getLocalHost();
                String myIP = myAddress.getHostAddress();
                
                System.out.println("My IP is : " + myIP);
            }
            catch(Exception e){
                System.out.println("I have no IP address????");
            }
            
            (new Server()).start();
            Gui game2 = new Gui("localhost", Boolean.parseBoolean(args[0]));
            game2.setVisible(true);
        }
        
        if(choice == JOptionPane.NO_OPTION){
            serverIP = JOptionPane.showInputDialog(null, "Please enter the IP address of the game that you wish to join",
                    "Join a Game", JOptionPane.PLAIN_MESSAGE);
            
            if(serverIP == null)
                return;
            
            Gui game1 = new Gui((String)serverIP, Boolean.parseBoolean(args[0]));
            game1.setVisible(true);
        }
    } 
}



/******* SHIT LEFT TO DO ************
 * 
 * 
 * readme in PDF format
 *      - README.pdf
 *      - how to execute the program
 *      - what functionality works
 *      - which requirements have not been implemented
 *      - known bugs
 * 
 * Test in the Mac Lab!!!
 * 
 * submit assignmnet to svn for grading
 *      - use tag cis3760-a1
 * 
 */