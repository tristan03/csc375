package Assignment1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Assignment1 {
    public static void main(String[] args) {
        // define dimensions of factory floor
        int height = 30;
        int width = 20;
        FactoryFloor factoryFloor = new FactoryFloor(height, width);

        // run genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(48, factoryFloor);
        ga.run();

        System.out.println();
    }
}

// TODO: write
class GeneticAlgorithm {
    private final List<Station> population;

    // TODO: write main algorithm loop
    public GeneticAlgorithm(int populationSize, FactoryFloor factoryFloor) {
        population = initializePopulation(populationSize, factoryFloor);
        //System.out.println(population);
    }

    private List<Station> initializePopulation(int size, FactoryFloor factoryFloor) {
        List<Station> population = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Station station = generateRandomPopulation(factoryFloor, i);
            population.add(station);
        }

        return population;
    }

    public void run() {
        evaluatePopulationFitness(population);
    }

    private static Station generateRandomPopulation(FactoryFloor factoryFloor, int count) {
        Random random = new Random();
        int height = factoryFloor.getHeight();
        int width = factoryFloor.getWidth();

        int randomHeight = random.nextInt(height);
        int randomWidth = random.nextInt(width);

        Station station = new Station("", 0, 0, "", 0.0);
        station.setName("Station" + count);
        station.setX(randomHeight);
        station.setY(randomWidth);
        station.setFunction(randomlyAssignFunction());
        station.setFitness(0.0); // temporarily set to 0.0, has not been calculated yet

        return new Station(station.getName(), station.getX(), station.getY(), station.getFunction(), station.getFitness());
    }

    private static String randomlyAssignFunction() {
        String function1 = "Assembly";
        String function2 = "Machinist";
        String function3 = "QualityControl";

        int max = 3;
        Random random = new Random();

        int randomNumber = random.nextInt(max);

        if (randomNumber == 1) {
            return function1;
        } else if (randomNumber == 2) {
            return function2;
        } else {
            return function3;
        }
    }

    static void evaluatePopulationFitness(List<Station> population) {
        // evaluate the fitness of each individual based on problem-specific fitness function
        for (Station station : population) {
            double fitness = calculateFitness(station);
            station.setFitness(fitness);
        }
    }

    private static double calculateFitness(Station station) {
        // TODO: implement fitness function
        double distance = calculateDistance(station);
        double functionValue = calculateFunctionValue(station);

        // fitness score
        return 1.0 / (distance + functionValue);
    }

    private static double calculateDistance(Station station) {
        double x = station.getX();
        double y = station.getY();

        // euclidean distance
        return Math.sqrt(x * x + y * y);
    }

    private static double calculateFunctionValue(Station station) {
        String function = station.getFunction();

        // TODO: probably change this. very basic for now just to move on in implementation
        return switch (function) {
            case "Assembly" -> 10.0;
            case "Machinist" -> 8.0;
            case "QualityControl" -> 7.0;
            default -> 0.0;
        };
    }

    static List<Station> selectParents(List<Station> population) {
        // select parents for crossover
        return null;
    }

    static List<Station> createOffspring(List<Station> population) {
        // perform crossover and mutation to create new population
        return null;
    }

    static Station getBestIndividual(List<Station> population) {
        // find and return the best individual in the population
        return null;
    }
}

class FactoryFloor {
    final int height;
    final int width;

    public FactoryFloor(int height, int width) {
        this.height = height;
        this.width = width;
    }

    int getHeight() { return height; }
    int getWidth() { return width; }
}

class Station {
    private String name;
    private int x;
    private int y;
    private String function;
    private double fitness;

    public Station(String name, int x, int y, String function, double fitness) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.function = function;
        this.fitness = fitness;
    }
    String getName() { return name; }
    int getX() { return x; }
    int getY() { return y; }
    String getFunction() { return function; }
    double getFitness() { return fitness; }

    void setName(String name) { this.name = name; }
    void setX(int x) { this.x = x; }
    void setY(int y) { this.y = y; }
    void setFunction(String function) { this.function = function; }
    void setFitness(double fitness) { this.fitness = fitness; }

}