import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Random;

public class Minesweeper {
    public static void main(String[] args) {
        openDefaultBoard();
    }

    static void openDefaultBoard() {
        int height = 500;
        int width = 500;
        int rows = 9;
        int cols = 9;

        Board board = new Board(height, width, rows, cols);
        board.createBoard();

        JFrame frame = new JFrame("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.add(board);
        frame.setVisible(true);
    }
}

class Board extends JPanel {
    int height;
    int width;
    int rows;
    int cols;

    public Board(int height, int width, int rows, int cols) {
        this.height = height;
        this.width = width;
        this.rows = rows;
        this.cols = cols;
    }

    public void createBoard() {
        int rows = getRows();
        int cols = getCols();

        boolean[][] bombLocations = new boolean[rows][cols]; // to keep track of bombs

        setLayout(new GridLayout(rows, cols));
        Color backgroundColor = new Color(211, 211, 211);
        Border border = new LineBorder(Color.GRAY, 1);

        Random random = new Random();
        int mineCount = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                JButton button = new JButton();
                button.setBackground(backgroundColor);
                button.setBorder(border);
                button.setActionCommand(row + "," + col);

                if (random.nextInt(6) == 1 && mineCount != 10) {
                    bombLocations[row][col] = true;
                    mineCount++;
                }

                button.addActionListener(e -> {
                    JButton clickedButton = (JButton) e.getSource();
                    String command = clickedButton.getActionCommand();
                    String[] parts = command.split(",");
                    int clickedRow = Integer.parseInt(parts[0]);
                    int clickedCol = Integer.parseInt(parts[1]);

                    if (bombLocations[clickedRow][clickedCol]) {
                        System.out.println("Game over. ");
                    } else {
                        System.out.println("Clear. ");
                    }
                });
                add(button);
            }
        }
    }

    void setHeight(int height) {
        this.height = height;
    }

    void setWidth(int width) {
        this.width = width;
    }

    int getBoardHeight() {
        return height;
    }

    int getBoardWidth() {
        return width;
    }

    int getRows() {
        return rows;
    }

    int getCols() {
        return cols;
    }
}


