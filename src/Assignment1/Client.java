/**
 * Description: CIS*3760 - Assignment #1
 * @author Jennfier Winer
 * @since 2013-01-10
 * Created: January 10, 2013
 * Last Modified: January 22, 2013
 */
package Assignment1;

import java.io.*;
import java.net.*;
import javax.swing.*;


// FIX - if a socket is closed, display gameover???

/**
 * A class that models a TicTacToe client.
 * The client acts as a messenger, sending the user's input to the server to be
 * processed, then sends the server's reply to the GUI to be displayed.
 * 
 * @author Jennifer Winer
 */
public class Client extends SwingWorker<Void, Integer>{
    private Socket mySocket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private String serverIP;
    
    private Gui display;
    
    /**
     * Initializes the private variables and tries to connect to the server at address ip
     * 
     * @param myGui The Gui that displays all information relayed by this client
     * @param ip = the IP address of the server
     */
    public Client(Gui myGui, String ip){
        display = myGui;
        serverIP = ip;
        
        try{
            mySocket = new Socket(serverIP, Server.portNum);
            dataIn = new DataInputStream(mySocket.getInputStream());
            dataOut = new DataOutputStream(mySocket.getOutputStream()); 
        }
        
        catch(Exception e){
            String errorMessage = "Could not connect to the server at " + serverIP
                    + "\nPlease ensure that you have started a new game before you try to join one!";
            
            JOptionPane.showMessageDialog(display, errorMessage, "Opponent Not Found",JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    
    
    /**
     * The "main" function of the client thread. It communicates with the server
     * on the Gui's behalf.
     */
    @Override
    protected Void doInBackground(){
        int message;
        
        try{
            while(true){
                message = dataIn.readInt();
                publish(message);

                if(message < 10)
                    break;
            }
        }
            
        catch(Exception e){
            // You have been disconnected from the server for some reason
        }
        
        return null;
    }
    
    /**
     * Update the Gui based on the input that was just received 
     * 
     * @param messages A list of messages to process
     */
    @Override
    protected void process(java.util.List<Integer> messages){
        int message;
        
        message = messages.get(0);
        messages.remove(0);
        decodeMessage(message);
    }
    
    /**
     * Closes the sockets and the streams before exiting the thread
     */
    @Override
    protected void done(){
        try{
            dataIn.close();
            dataOut.close();
            mySocket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    

    /**
     * Sends a message to the server
     * 
     * @param message the message that you wish to send
     */
    protected void sendMessage(int message){
        try{
            dataOut.writeInt(message);
        }
        catch(Exception e){
            // You are no longer connected to the server
            decodeMessage(-1);
        }
    }

    /**
     * Update the Gui according to the message received
     * 
     * @param message the message that the 
     */
    private void decodeMessage(int message){
        try{
            if (0 < message && message < 4){
                int gameOver = message;
                
                message = dataIn.readInt();
                display.updateGui(message);
                display.updateGui(gameOver);
            }
            
            else{
                display.updateGui(message);
            }
        }
        catch(Exception e){
        }
    }
}