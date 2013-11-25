/**
 * Description: CIS*3760 - Assignment #1
 * @author Jennfier Winer
 * @since 2013-01-10
 * Created: January 10, 2013
 * Last Modified: January 22, 2013
 */
package Assignment1;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A class that models a TicTacToe server.
 * The server performs most computational tasks for the game, then simply relays
 * to the clients what they should now display.
 * 
 * @author jwiner
 */
public class Server extends Thread{   
    public static int portNum = 3761;
    private static ServerSocket hostSocket;
    
    private Socket[] clientSockets;
    private DataInputStream[] inbox;
    private DataOutputStream[] outbox;
    
    
    private int[][] gameGrid;
    private int turnsPassed = 0;
    private int currentTurn = 0;
    
    /**
     * The constructor initializes the game and sockets
     */
    public Server(){
        gameGrid = new int[3][3];
        
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                gameGrid[i][j] = -1;
            }
        }
        
        clientSockets = new Socket[2];
        inbox = new DataInputStream[2];
        outbox = new DataOutputStream[2];
    }
    
    /**
     * The "main" function of the server thread. It is responsible for reading from and
     * writing to the clients, and disconnecting clients when appropriate
     */
    @Override
    public void run(){
        int decodedMessage;
        
        acceptClients();
        while(isGameOver() == -1){
            try{
                decodedMessage = decodeMessage(inbox[currentTurn].readInt());

                if(decodedMessage < 30)
                    turnsPassed++;
                
                sendMessages(decodedMessage);
                
                if(decodedMessage < 30)
                    currentTurn = (currentTurn + 1)%2;
            }
            
            catch(Exception e){
                // one of my clients has disconnected, so inform the other that the game is over
                sendMessages(-1);
                break;
            }
        }
        this.disconnectClients();
    }
    
    
    
    /**
     * Sets up the server's socket and accepts 2 clients.
     */
    private void acceptClients(){
        try{
            hostSocket = new ServerSocket(portNum);
            
            clientSockets[0] = hostSocket.accept();
            inbox[0] = new DataInputStream(clientSockets[0].getInputStream());
            outbox[0] = new DataOutputStream(clientSockets[0].getOutputStream());
            System.out.println("Accepted first Client");
            
            clientSockets[1] = hostSocket.accept();
            inbox[1] = new DataInputStream(clientSockets[1].getInputStream());
            outbox[1] = new DataOutputStream(clientSockets[1].getOutputStream());
            System.out.println("Accepted Second Client");
            
            // Tell whoever is player one that it is their turn to make a move
            outbox[0].writeInt(50);
        }
        
        catch(Exception e){
            e.printStackTrace(); 
        }
    }
    
    /**
     * Closes the sockets and streams of all clients
     */
    private void disconnectClients() {
        try{
            /* This ensures that clients have enough time to potentially read 
             * a Game over message in adition to a "move made" message before
             * the stream is closed and an irritating exception is thrown */
            Thread.sleep(5000);
            hostSocket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        try{
            inbox[0].close();
            outbox[0].close();
            clientSockets[0].close();
        }
        catch(Exception e){
            // Player 1 must have already disconnected
        }

        try{
            inbox[1].close();
            outbox[1].close();
            clientSockets[1].close();
        }
        catch(Exception e){
            // Player 2 must have already disconnected
        }
    }
    
    
    
    /**
     * Interprets the message sent from a client. If the move was valid, record it.
     * 
     * @param message The client's move encoded in an integer
     * 
     * @return An integer X (between 0 and 8) corresponds to a valid move made to square X. <br/>
     * An integer 3X (between 30 and 38) means that the client attempted 
     * to make a move to the previously occupied square X, and thus no changes were made to the game.
     */
    private int decodeMessage(int message){
        if( 0 <= message && message < 9){
            if(gameGrid[message/3][message%3] == -1)
                gameGrid[message/3][message%3] = currentTurn;
            else
                message =  message + 30;
            
            return message;
        }
        
        else{
            System.out.println("Unknown message " + message + " recieved from the client!!");
            return -5;
        }
    }

    /**
     * It sends a detailed message back to each client through the sockets 
     * about how to alter their Gui to match the current state of the game.
     * 
     * @param message The client's move encoded in an integer
     */
    private void sendMessages(int message){
        int winner = isGameOver();
        int currentClient;
        
        try{
            for(currentClient=0; currentClient<2; currentClient++){
                try{
                    // If the game has ended
                    if(winner != -1){
                        if(winner == 3)
                            outbox[currentClient].writeInt(3);
                        else if(winner == currentClient)
                            outbox[currentClient].writeInt(1);
                        else
                            outbox[currentClient].writeInt(2);     
                    }
                    
                    // If the move was valid, tell both clients what move was made and whose turn it is
                    if(0 <= message && message < 9){  
                        outbox[currentClient].writeInt(message + 10 + 10*((currentTurn + currentClient)%2));
                    }

                    // if the move was NOT valid, tell the client who played that move to try again
                    else if (30 <= message && message < 39){
                        if(currentTurn == currentClient)
                            outbox[currentClient].writeInt(message);
                    }

                    // If someone disconnected, tell their partner that the game has ended
                    else if (message == -1 && currentTurn != currentClient){
                        outbox[currentClient].writeInt(-1);
                    }
                    else
                        System.out.println("Unknown message " + message);
                }
                catch(Exception e){
                    System.out.println("Partner " + (currentClient+1) + " disconnected");
                    outbox[(currentClient+1)%2].writeInt(-1);
                }
            }
        }
        catch(Exception e){
            System.out.println("Both Parties have disconnected???");
        }
    }
    
    
    
    /**
     * Checks to see if the game has been completed yet
     * @return -1 = The game has not yet ended. <br/>
     * 0 = client 1 has won. <br/>
     * 1 = client 2 has won. <br/>
     * 3 = Draw
     */
    private int isGameOver(){
        if(!(gameGrid[0][0] == -1) && (gameGrid[0][0] == gameGrid[0][1]) && (gameGrid[0][0] == gameGrid[0][2]))
            return gameGrid[0][0];
        
        else if(!(gameGrid[1][0] == -1) && (gameGrid[1][0] == gameGrid[1][1]) && (gameGrid[1][0] == gameGrid[1][2]))
            return gameGrid[1][0];
        
        else if(!(gameGrid[2][0] == -1) && (gameGrid[2][0] == gameGrid[2][1]) && (gameGrid[2][0] == gameGrid[2][2]))
            return gameGrid[2][0];
        
        else if(!(gameGrid[0][0] == -1) && (gameGrid[0][0] == gameGrid[1][0]) && (gameGrid[0][0] == gameGrid[2][0]))
            return gameGrid[0][0];
        
        else if(!(gameGrid[0][1] == -1) && (gameGrid[0][1] == gameGrid[1][1]) && (gameGrid[0][1] == gameGrid[2][1]))
            return gameGrid[0][1];

        else if(!(gameGrid[0][2] == -1) && (gameGrid[0][2] == gameGrid[1][2]) && (gameGrid[0][2] == gameGrid[2][2]))
            return gameGrid[0][2];
        
        else if(!(gameGrid[0][2] == -1) && (gameGrid[0][2] == gameGrid[1][1]) && (gameGrid[0][2] == gameGrid[2][0]))
            return gameGrid[0][2];
        
        else if(!(gameGrid[0][0] == -1) && (gameGrid[0][0] == gameGrid[1][1]) && (gameGrid[0][0] == gameGrid[2][2]))
            return gameGrid[0][0];
        
        else if(turnsPassed >= 9)
            return 3;
        
        else
            return -1;
    }

}