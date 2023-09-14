package Assignment1;

/*
    Tristan Allen
    Suny Oswego CSC375
    Assignment1

    A parallel genetic algorithm for a Facilities Layout problem
 */

import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Assignment1 {
    public static void main(String[] args) {
        // define dimensions of the factory floor
        int height = 30;
        int width = 20;

        // create factory floor
        FactoryFloor factoryFloor = new FactoryFloor(height, width);

        List<Station> stationList = new ArrayList<>(); // list to hold at least 48 stations

        // randomly generate population of stations, TODO: ignoring duplicate positions
        Map<String, Station> stationMap = randomlyGeneratePopulation(factoryFloor, stationList); // map to hold station# -> station info



        //System.out.println(Arrays.toString(randomPosition));
        //printFloor(factoryFloor, stationPosition);
        System.out.println();


    }

    // print floor for visualization

    static Map<String, Station> randomlyGeneratePopulation(FactoryFloor factoryFloor, List<Station> stationList) {
        int[] position;
        Map<String, Station> stationMap = new HashMap<>();
        Station station = new Station("", new int[]{}, "");

        for (int i = 0; i < 49; i++) {
            Random random = new Random();
            int height = factoryFloor.getHeight();
            int width = factoryFloor.getWidth();

            int randomHeight = random.nextInt(height);
            int randomWidth = random.nextInt(width);

            position = new int[]{randomHeight, randomWidth};

            station.setName("Station" + i);
            station.setPosition(position);
            station.setFunction(randomlyGenerateFunction());

            stationList.add(new Station(station.getName(), station.getPosition(), randomlyGenerateFunction()));
            stationMap.put(station.getName(), new Station(station.getName(), station.getPosition(), randomlyGenerateFunction()));
        }

        return stationMap;
    }

    // TODO: probably delete this and make an actual function solution
    static String randomlyGenerateFunction() {
        String function1 = "Cutting";
        String function2 = "Assembly";
        String function3 = "QualityControl";

        int max = 3;
        Random random = new Random();
        int randomFunction = random.nextInt(max);

        if (randomFunction == 1) {
            return function1;
        } else if (randomFunction == 2) {
            return function2;
        } else {
            return function3;
        }
    }

    // TODO: refactor. currently won't work
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

// TODO: write
class GeneticAlgorithm {
    // TODO: write main algorithm loop

    static List<Individual> initializePopulation(int size) {
        return null;
    }

    static void evaluatePopulationFitness(List<Individual> population) {
        // evaluate the fitness of each individual based on problem-specific fitness function
    }

    static List<Individual> selectParents(List<Individual> population) {
        // select parents for crossover
        return null;
    }

    static List<Individual> createOffspring(List<Individual> population) {
        // perform crossover and mutation to create new population
        return null;
    }

    static Individual getBestIndividual(List<Individual> population) {
        // find and return the best individual in the population
        return null;
    }


}

//TODO: write
class Individual {
    // define representation and fitness calculation for an individual
    double getFitness() {
        // calculate and return the fitness of this individual
        return 0.0;
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
    private String name;
    private int[] position;
    private String function;

    public Station(String name, int[] position, String function) {
        this.name = name;
        this.position = position;
        this.function = function;
    }
    String getName() { return name; }
    int[] getPosition() { return position; }
    String getFunction() { return function; }

    void setName(String name) { this.name = name; }
    void setPosition(int[] position) { this.position = position; }
    void setFunction(String function) { this.function = function; }

}
