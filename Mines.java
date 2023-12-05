import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mines {

    public static void main(String[] args) {
        startGame();
    }

    public static void startGame() {
        Game mines = setDefaultGameSize();
        mines.play(mines);
    }

    private static Game setDefaultGameSize() {
        return new Game(300, 300, 9, 9, 10);
    }
}

class Game extends JPanel {
    private int height;
    private int width;
    private int rows;
    private int cols;
    private int numMines;
    private boolean[][] mineLocations;  // true if there is a mine in a location
    private JButton[][] buttonGrid;

    private boolean[][] uncoveredLocations; // true if uncovered, false if covered
    AtomicBoolean isFirstClick = new AtomicBoolean(true);   // true when user has not selected their first click yet

    private int markedCounter;
    private int deathCounter;
    public Game (int height, int width, int rows, int cols, int numMines) {
        this.height = height;
        this.width = width;
        this.rows = rows;
        this.cols = cols;
        this.numMines = numMines;
    }

    void play(Game mines) {
        int rows = mines.getRows();
        int cols = mines.getCols();
        buttonGrid = new JButton[rows][cols];
        uncoveredLocations = new boolean[rows][cols];

        setLayout(new GridLayout(rows, cols));
        Color backgroundColor = new Color(211, 211, 211);
        Border border = new LineBorder(Color.GRAY, 1);

        JLabel timerLabel = new JLabel("[0:00]  ");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 15));

        JLabel markedLabel = new JLabel("Marked: " + mines.getMarked() + " / " + mines.getNumMines());
        markedLabel.setFont(new Font("Arial", Font.PLAIN, 15));

        JLabel deathLabel = new JLabel();
        deathLabel.setFont(new Font("Arial", Font.PLAIN, 15));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                JButton button = new JButton();
                button.setBackground(backgroundColor);
                button.setBorder(border);
                button.setBorder(new BevelBorder(BevelBorder.RAISED));
                button.setActionCommand(row + "," + col);
                button.setFont(new Font("Arial", Font.PLAIN, 25));

                button.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        button.setBorder(new BevelBorder(BevelBorder.LOWERED));
                        button.setBackground(Color.LIGHT_GRAY);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        button.setBorder(new BevelBorder(BevelBorder.RAISED));
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JButton clickedButton = (JButton) e.getSource();
                        String command = clickedButton.getActionCommand();
                        String[] parts = command.split(",");
                        int clickedRow = Integer.parseInt(parts[0]);
                        int clickedCol = Integer.parseInt(parts[1]);

                        if (SwingUtilities.isLeftMouseButton(e)) {

                            if (isFirstClick.get()) {
                                isFirstClick.set(false);

                                generateMines(clickedRow, clickedCol);

                                floodFill(clickedRow, clickedCol);
                            }

                            if (mineLocations[clickedRow][clickedCol]) {
                                buttonGrid[clickedRow][clickedCol].setBackground(Color.RED);
                                buttonGrid[clickedRow][clickedCol].setBackground(backgroundColor);
                                buttonGrid[clickedRow][clickedCol].setBackground(Color.RED);

                                markedLabel.setText("DEAD!");
                                deathCounter++;
                                deathLabel.setText("Deaths: " + mines.getDeaths());
                            } else {

                                int nearbyMines = countNearbyMines(clickedRow, clickedCol);

                                button.setBorder(new BevelBorder(BevelBorder.LOWERED));
                                if (nearbyMines != 0) {
                                    button.setForeground(getColor(nearbyMines));
                                    buttonGrid[clickedRow][clickedCol].setText(String.valueOf(nearbyMines));
                                    uncoveredLocations[clickedRow][clickedCol] = true;
                                } else {
                                    floodFill(clickedRow, clickedCol);
                                }

                                if (checkWin()) {
                                    markedLabel.setText("COMPLETED!");
                                }
                            }

                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            buttonGrid[clickedRow][clickedCol].setText("F");
                            markedCounter++;
                            markedLabel.setText("Marked: " + mines.getMarked() + " / " + mines.getNumMines());
                        }
                    }
                });

                buttonGrid[row][col] = button;
                add(button);
            }
        }

        int height = mines.getGameHeight();
        int width = mines.getGameWidth();

        JFrame frame = new JFrame("Mines");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.add(mines);

        JMenuBar menuBar = new JMenuBar();

        // game type menu and sub menu
        JMenu gameTypeMenu = new JMenu("Game type");
        JMenuItem gameTypeEasy = new JMenuItem("Easy 9x9, 10 mines");
        JMenuItem gameTypeMedium = new JMenuItem("Medium 16x16, 40 mines");
        JMenuItem gameTypeHard = new JMenuItem("Hard 16x30, 99 mines");

        JMenuItem newGameItem = new JMenuItem("New game");
        JMenuItem restartGameItem = new JMenuItem("Restart game");  // TODO: Implement

        JMenuItem undoMoveItem = new JMenuItem("Undo move");    // TODO: Implement
        JMenuItem redoMoveItem = new JMenuItem("Redo move");    // TODO: Implement
        JMenuItem solveGameItem = new JMenuItem("Solve game");


        // add listeners

        // start a new game
        newGameItem.addActionListener(e -> {
            Game currentGame = getGameSize();
            frame.setVisible(false);
            refreshFrame(frame, currentGame);
        });

        // for changing game types (size)
        gameTypeEasy.addActionListener(e -> {
            Game easy = new Game(300, 300, 9, 9, 10);
            frame.setVisible(false);
            refreshFrame(frame, easy);
        });

        gameTypeMedium.addActionListener(e -> {
            Game medium = new Game(500, 500,16, 16, 40);
            frame.setVisible(false);
            refreshFrame(frame, medium);
        });

        gameTypeHard.addActionListener(e -> {
            Game hard = new Game(600, 1100, 16, 30, 99);
            frame.setVisible(false);
            refreshFrame(frame, hard);
        });

        gameTypeMenu.add(gameTypeEasy);
        gameTypeMenu.add(gameTypeMedium);
        gameTypeMenu.add(gameTypeHard);

        // solves the current game
        solveGameItem.addActionListener(e -> {
            if (!isFirstClick.get()) {
                for (int i = 0; i < mineLocations.length; i++) {
                    for (int j = 0; j < mineLocations.length; j++) {
                        JButton button = buttonGrid[i][j];

                        // check if it's a mine
                        if (mineLocations[i][j]) {
                            button.setText("F");
                        } else {
                            // lower button and change color
                            int nearbyMines = countNearbyMines(i, j);
                            button.setBorder(new BevelBorder(BevelBorder.LOWERED));
                            button.setBackground(Color.LIGHT_GRAY);

                            // if there are nearby mines, show the number
                            if (nearbyMines != 0) {
                                button.setForeground(getColor(nearbyMines));
                                button.setText(String.valueOf(nearbyMines));
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "The game has not been started yet", "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // add to menu bar
        menuBar.add(gameTypeMenu);
        menuBar.add(newGameItem);
        menuBar.add(restartGameItem);
        menuBar.add(undoMoveItem);
        menuBar.add(redoMoveItem);
        menuBar.add(solveGameItem);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        bottomPanel.add(timerLabel, BorderLayout.WEST);
        bottomPanel.add(markedLabel, BorderLayout.CENTER);
        bottomPanel.add(deathLabel, BorderLayout.EAST);


        frame.setJMenuBar(menuBar);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private boolean checkWin() {
        for (int i = 0; i < uncoveredLocations.length; i++) {
            for (int j = 0; j < uncoveredLocations[i].length; j++) {

                if (!mineLocations[i][j] && !uncoveredLocations[i][j]) {
                    return false;  // found a covered non-mine button
                }
            }
        }
        return true;    // all non-mine buttons are uncovered. game is won
    }

    private void refreshFrame(JFrame frame, Game newGame) {
        frame.getContentPane().removeAll();
        frame.add(newGame);
        newGame.play(newGame);
        frame.revalidate();
        frame.repaint();
    }

    private Color getColor(int nearbyMines) {
        if (nearbyMines != 0) {
            if (nearbyMines == 1) {
                return Color.BLUE;
            } else if (nearbyMines == 2) {
                return Color.GREEN;
            } else if (nearbyMines == 3) {
                return Color.RED;
            } else if (nearbyMines == 4) {
                return new Color(0, 0, 139);
            } else if (nearbyMines == 5) {
                return new Color(139, 0, 0);
            } else if (nearbyMines == 6) {
                return Color.CYAN;
            } else if (nearbyMines == 7) {
                return Color.BLACK;
            } else if (nearbyMines == 8) {
                return Color.GRAY;
            }
        }
        return null;
    }

    private void floodFill(int row, int col) {
        int numNearbyMines = countNearbyMines(row, col);

        // base cases
        if (row < 0 || row >= mineLocations.length || col < 0 || col >= mineLocations[0].length ||
                uncoveredLocations[row][col] || mineLocations[row][col]) {
            return;
        }

        uncoveredLocations[row][col] = true;

        JButton button = buttonGrid[row][col];
        if (numNearbyMines > 0) {
            button.setText(String.valueOf(numNearbyMines));
            button.setForeground(getColor(numNearbyMines));
            button.setBorder(new BevelBorder(BevelBorder.LOWERED));
            return;
        } else if (numNearbyMines == 0) {
            button.setBorder(new BevelBorder(BevelBorder.LOWERED));
        }

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                if (i == 0 && j == 0) {
                    continue;
                }
                floodFill(row + i, col + j);
            }
        }
    }

    private int countNearbyMines(int row, int col) {
        boolean[][] mineLocations = getMineLocations();
        int mineCount = 0;

        for (int i = -1; i <= 1; i++) {     // checks rows above, same, below as clicked row
            for (int j = -1; j <= 1; j++) {  // checks col left, same, right as clicked col
                int newRow = row + i;
                int newCol = col + j;

                if (newRow == row && newCol == col) {   // don't check clicked row & col
                    continue;
                }

                // check boundaries
                if (newRow >= 0 && newRow < mineLocations.length && newCol >= 0 && newCol < mineLocations[0].length) {
                    if (mineLocations[newRow][newCol]) {
                        mineCount++;
                    }
                }
            }
        }
        return mineCount;
    }

    private void generateMines(int safeRow, int safeCol) {
        int maxMines = getNumMines();
        int mineCount = 0;
        int rows = getRows();
        int cols = getCols();
        Random random = new Random();
        boolean[][] mineLocations = new boolean[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (Math.abs(row - safeRow) > 1 || Math.abs(col - safeCol) > 1) {
                    if (random.nextInt(4) == 1 && mineCount != maxMines) {
                        mineLocations[row][col] = true;         // a cell that has a mine has boolean value true
                        //buttonGrid[row][col].setText("b");      // TODO: remove
                        uncoveredLocations[row][col] = false;   // every cell is currently covered
                        mineCount++;
                    }
                }
            }
        }
        setMineLocations(mineLocations);

    }

    int getGameHeight() {
        return height;
    }
    int getGameWidth() {
        return width;
    }
    int getRows() {
        return rows;
    }
    int getCols() {
        return cols;
    }

    int getNumMines() {
        return numMines;
    }

    boolean[][] getMineLocations() {
        return mineLocations;
    }

    void setMineLocations(boolean[][] mineLocations) {
        this.mineLocations = mineLocations;
    }

    int getMarked() {
        return markedCounter;
    }

    int getDeaths() {
        return deathCounter;
    }

    Game getGameSize() {
        return new Game(getGameHeight(), getGameWidth(), getRows(), getCols(), getNumMines());
    }
}
