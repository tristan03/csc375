package Assignment1;

/*
    Tristan Allen
    Suny Oswego CSC375
    Assignment1

    A parallel genetic algorithm for a Facilities Layout problem
 */

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class Assignment1 {
    public static void main(String[] args) {
        // define dimensions of the factory floor
        int height = 10;
        int width = 10;

        // create factory floor
        FactoryFloor factoryFloor = new FactoryFloor(height, width);

        // create stations
        Station station = new Station(new int[] {}, "");

        // randomly generate population, ignoring duplicates
        int[] stationPosition = randomlyGeneratePopulation(factoryFloor, station);


        //System.out.println(Arrays.toString(randomPosition));
        printFloor(factoryFloor, stationPosition);
        System.out.println("Position of station: " + Arrays.toString(station.getPosition()));
    }

    // print floor for visualization

    static int[] randomlyGeneratePopulation(FactoryFloor factoryFloor, Station station) {
        Random random = new Random();
        int height = factoryFloor.getHeight();
        int width = factoryFloor.getWidth();

        int randomHeight = random.nextInt(height);
        int randomWidth = random.nextInt(width);

        int[] position = {randomHeight, randomWidth};

        station.setPosition(position);

        return position;
    }
    static void printFloor(FactoryFloor factoryFloor, int[] stationPosition) {
        int height = factoryFloor.getHeight();
        int width = factoryFloor.getWidth();

        // create a 2D character array to represent the factory floor
        char[][] floorGrid = new char[height][width];

        // initialize the grid with empty spaces
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                floorGrid[i][j] = '.';
            }
        }

        // place the station on the grid
        int stationRow = stationPosition[0];
        int stationCol = stationPosition[1];
        floorGrid[stationRow][stationCol] = 'S';

        // print the grid
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(floorGrid[i][j] + " ");
            }
            System.out.println(); // move to the next row
        }
    }
}

class FactoryFloor {
    private final int height;
    private final int width;

    public FactoryFloor(int height, int width) {
        this.height = height;
        this.width = width;
    }

    int getHeight() { return height; }
    int getWidth() { return width; }
}

class Station {
    private int[] position;
    private String function;

    public Station(int[] position, String function) {
        this.position = position;
        this.function = function;
    }

    int[] getPosition() { return position; }
    String getFunction() { return function; }

    void setPosition(int[] position) { this.position = position; }
    void setFunction(String function) { this.function = function; }

}
