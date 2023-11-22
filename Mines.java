import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class Mines {

    public static void main(String[] args) {
        Game mines = null;

        System.out.println(" | Easy 9x9 | Medium 16x16 | Hard 16x30 | ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();

        if (input.equalsIgnoreCase("Easy")) {
            mines = setGameSize(300, 300, 9, 9, 10);
        } else if (input.equalsIgnoreCase("Medium")) {
            mines = setGameSize(500, 500,16, 16, 40);
        } else if (input.equalsIgnoreCase("Hard")) {
            mines = setGameSize(600, 1100,16, 30, 99);
        } else {
            System.out.println("[ERROR] Invalid input ");
            System.exit(1);
        }

        mines.play();

        int height = mines.getGameHeight();
        int width = mines.getGameWidth();

        JFrame frame = new JFrame("Mines");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.add(mines);
        frame.setVisible(true);

    }

    private static Game setGameSize(int height, int width, int rows, int cols, int numMines) {
        return new Game(height, width, rows, cols, numMines);
    }
}

class Game extends JPanel {
    private final int height;
    private final int width;
    private final int rows;
    private final int cols;
    private final int numMines;
    private boolean[][] mineLocations;
    private JButton[][] buttonGrid;

    public Game (int height, int width, int rows, int cols, int numMines) {
        this.height = height;
        this.width = width;
        this.rows = rows;
        this.cols = cols;
        this.numMines = numMines;
    }

    void play() {
        int rows = getRows();
        int cols = getCols();
        buttonGrid = new JButton[rows][cols];
        AtomicBoolean isFirstClick = new AtomicBoolean(true);

        setLayout(new GridLayout(rows, cols));
        Color backgroundColor = new Color(211, 211, 211);
        Border border = new LineBorder(Color.GRAY, 1);

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

                                if (initialFloodFill(clickedRow, clickedCol)) {
                                    button.setBorder(new BevelBorder(BevelBorder.LOWERED));
                                }
                            }

                            if (mineLocations[clickedRow][clickedCol]) {
                                System.out.println("Game over. ");
                            } else {
                                int nearbyMines = countNearbyMines(clickedRow, clickedCol);

                                button.setBorder(new BevelBorder(BevelBorder.LOWERED));
                                if (nearbyMines != 0) {
                                    if (nearbyMines == 1) {
                                        button.setForeground(Color.BLUE);
                                    } else if (nearbyMines == 2) {
                                        button.setForeground(Color.GREEN);
                                    } else if (nearbyMines == 3) {
                                        button.setForeground(Color.RED);
                                    } else if (nearbyMines == 4) {
                                        button.setForeground(new Color(0, 0, 139));
                                    } else if (nearbyMines == 5) {
                                        button.setForeground(new Color(139, 0, 0));
                                    } else if (nearbyMines == 6) {
                                        button.setForeground(Color.CYAN);
                                    } else if (nearbyMines == 7) {
                                        button.setForeground(Color.BLACK);
                                    } else if (nearbyMines == 8) {
                                        button.setForeground(Color.GRAY);
                                    }

                                    buttonGrid[clickedRow][clickedCol].setText(String.valueOf(nearbyMines));
                                } else {
                                    floodFill(clickedRow, clickedCol);
                                }
                            }

                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            buttonGrid[clickedRow][clickedCol].setText("F");
                        }
                    }
                });

                buttonGrid[row][col] = button;
                add(button);
            }
        }
    }

    private boolean initialFloodFill(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j < 1; j++) {
                int newRow = row + i;
                int newCol = col + i;

                if (newRow >= 0 && newRow < mineLocations.length && newCol >= 0 && newCol < mineLocations[0].length) {
                    if (!mineLocations[newRow][newCol]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void floodFill(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j < 1; j++) {

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
                if (row != safeRow && col != safeCol) {
                    if (random.nextInt(4) == 1 && mineCount != maxMines) {
                        mineLocations[row][col] = true;
                        buttonGrid[row][col].setText("b");
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
}
