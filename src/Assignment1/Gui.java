/**
 * Description: CIS*3760 - Assignment #1
 * @author Jennfier Winer
 * @since 2013-01-18
 * Created: January 18, 2013
 * Last Modified: January 22, 2013
 */
package Assignment1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.MatteBorder;


/**
 * A class that models the client's graphical user interface.
 * It creates a Tic Tac Toe board upon which the game is played.
 * 
 * @author Jennifer Winer
 */
public class Gui extends JFrame implements ActionListener{
    public static final int WINDOW_HEIGHT = 700;
    public static final int WINDOW_WIDTH = 500;
    private static final int BORDER_SIZE = 7;
    
    private JPanel gameGrid;
    private JButton[][] grid = new JButton[3][3];
    private JLabel statusBar;
    
    private Theme myTheme;
    private boolean gameOver = false;
    
    private Client myClient;
    
    
    /**
     * A constructor that initializes both the Gui and its corresponding client thread
     * 
     * @param ip the IP address of the server that you will be connecting to
     * @param creative A toggle for making the game pretty 
     *      (true = beautiful, false = strictly adhere to the assignment 1 specification)
     */
    public Gui(String ip, boolean creative){
        super("TicTacToe");
        
        this.setAlwaysOnTop(true);
        this.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        
        myTheme = new Theme(creative);
        
        JLabel topBar = myTheme.topField;
        topBar.setPreferredSize(new Dimension(500, 140));
        this.add(topBar, BorderLayout.NORTH);
        
        gameGrid = new JPanel();
        gameGrid.setPreferredSize(new Dimension(500, 500));
        gameGrid.setLayout(new GridLayout(3, 3, BORDER_SIZE, BORDER_SIZE));
        gameGrid.setBackground(new Color(0x71450B));

        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                grid[i][j] = new JButton();
                grid[i][j].setName(Integer.toString(i*3 + j));
                grid[i][j].setIcon(myTheme.blankSquare);
                gameGrid.add(grid[i][j]);
            }
        }
        gameGrid.setBorder(new MatteBorder(BORDER_SIZE, BORDER_SIZE,
                BORDER_SIZE, BORDER_SIZE, new Color(0x71450B)));
        this.add(gameGrid, BorderLayout.CENTER);
        
        statusBar = myTheme.statusField;
        statusBar.setPreferredSize(new Dimension(500, 60));
        statusBar.setFont(new Font("Dialog", 1, 20));
        statusBar.setText("Waiting for an opponent ...");
        statusBar.setHorizontalTextPosition(JLabel.CENTER);
        this.add(statusBar, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        (myClient = new Client(this, ip)).execute();
    }

    /**
     * Disables all buttons on the game board
     */
    public void disconnectListeners(){
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                grid[i][j].removeActionListener(this);
            }
        }
    }

    /**
     * Enables all buttons on the game board
     */
    public void connectListeners(){
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                grid[i][j].addActionListener(this);
            }
        }
    }
    
    
    /**
     * Informs the server which button was pressed
     * 
     * @param e The ActionEvent triggered by clicking on a square in the grid
     */
    @Override
    public void actionPerformed(ActionEvent e){
        JButton button;
        int message;
        
        disconnectListeners();

        if(gameOver == true)
            return;

        button = (JButton)e.getSource();
        message = Integer.parseInt(button.getName());
        myClient.sendMessage(message);
    }
    
    /**
     * Update the game board based on the server's reply
     * 
     * @param receivedMessage The message received back from the server
     */
    public void updateGui(int receivedMessage){
        // The game has been won / lost / is a draw
        if(receivedMessage < 4){
            this.disconnectListeners();
            this.endGame(receivedMessage);
        }
        
        // YOUR move was valid, now display it
        else if(receivedMessage < 19){
            receivedMessage = receivedMessage - 10;
            grid[receivedMessage/3][receivedMessage%3].setIcon(myTheme.playerSquare);
            statusBar.setText("Waiting for your opponent's move");
        }
        
        // display your opponent's move 
        else{
            if(receivedMessage < 29){
                receivedMessage = receivedMessage - 20;
                grid[receivedMessage/3][receivedMessage%3].setIcon(myTheme.opponentSquare);             
            }
            
            // if YOUR move was invalid, it is still your turn
            statusBar.setText("Your turn to make a move");
            this.connectListeners();
        }
        this.repaint();
    }
    
    
    /**
     * Update the Gui to reflect that the game has now concluded
     * 
     * @param winner The player who has won the game
     */
    public void endGame(int winner){
        this.gameOver = true;
        JLabel statusScreen;
        
        if(winner == 1){
            this.remove(gameGrid);
            statusScreen = new JLabel(new ImageIcon(getClass().getResource("/win.jpg")));
            this.add(statusScreen, BorderLayout.CENTER);
            statusBar.setText("You Win !!!");
        }
        
        else if (winner == 2){
            this.remove(gameGrid);
            statusScreen = new JLabel(new ImageIcon(getClass().getResource("/loose.gif")));
            this.add(statusScreen, BorderLayout.CENTER);
            statusBar.setText("You Loose ...");
        }
        
        else if (winner == 3){
            statusBar.setText("Tie");
        }
            
        else{ 
            statusBar.setText("Your partner has forfeit!");
            gameGrid.setEnabled(false);
        }
    }
    
    /**
    * A container for all the design elements of the Gui (NOTE: This was my attempt to 
    * implement the Strategy Design pattern. Unfortunately, due to time constraints this
    * class is now somewhat vestigial....)
    * 
    * @author Jennifer Winer
    */
    private class Theme{
        public JLabel topField;
        public JLabel statusField;
        
        public ImageIcon blankSquare;
        public ImageIcon playerSquare;
        public ImageIcon opponentSquare;
        
        /**
        * Initializes all the theme variables based on command line arguments
        * 
        * @param creative A toggle for making the game pretty 
        *      (true = beautiful, false = strictly adhere to the assignment 1 specification)
        */
        public Theme(boolean creative){
            // make the GUI GORGEOUS!!!!! (for creativity points)
            if(creative){
                topField = new JLabel(new ImageIcon(getClass().getResource("/logo.jpg")));
                
                statusField = new JLabel(new ImageIcon(getClass().getResource("/gravel.jpg")));
                statusField.setForeground(new Color(0xE7C526));
                
                blankSquare = new ImageIcon(getClass().getResource("/grass.jpg"));
                playerSquare = new ImageIcon(getClass().getResource("/cowSquare.jpg"));
                opponentSquare = new ImageIcon(getClass().getResource("/opponentSquare.jpg"));
            }
            
            // make the Gui strictly adhere to the assignment guidelines
            else{
                topField = new JLabel("Tic Tac Toe");
                topField.setOpaque(true);
                topField.setBackground(Color.WHITE);
                topField.setFont(new Font("Dialog", 1, 80));
                topField.setHorizontalAlignment(JLabel.CENTER);

                statusField = new JLabel();
                statusField.setOpaque(true);
                statusField.setBackground(Color.WHITE);
                statusField.setHorizontalAlignment(JLabel.CENTER);
                
                blankSquare = new ImageIcon(getClass().getResource("/blank.png"));
                playerSquare = new ImageIcon(getClass().getResource("/boringPlayer.png"));
                opponentSquare = new ImageIcon(getClass().getResource("/boringOpponent.png"));
            }
        }
    }
}