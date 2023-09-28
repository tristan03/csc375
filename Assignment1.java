package Assignment1;

/*
    Tristan Allen
    Suny Oswego CSC375
    Assignment1

    A parallel genetic algorithm for a Facilities Layout problem
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static Assignment1.Assignment1.MAX_HEIGHT;

public class Assignment1 {
    final static int MAX_HEIGHT = 30;
    final static int MAX_WIDTH = 30;

    public static void main(String[] args) {
        FactoryFloor factoryFloor = new FactoryFloor(MAX_HEIGHT, MAX_WIDTH);

        // run genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(48, factoryFloor);
        ga.run();

        System.out.println();
    }
}

class GeneticAlgorithm {
    private List<Station> population;

    public GeneticAlgorithm(int populationSize, FactoryFloor factoryFloor) {
        population = initializePopulation(populationSize, factoryFloor);
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
        int maxGenerations = 10;
        for (int generation = 0; generation < maxGenerations; generation++) {
            evaluatePopulationFitness(population);

            List<Station> parents = selectParents(population);

            List<Station> offspring = createOffspring(parents, population.size());

            population = offspring;

            Station bestIndividual = getBestIndividual(population);

            List<Station> bestIndividualList = new ArrayList<>();
            storeBestIndividual(bestIndividual, bestIndividualList);

            if (terminationCriteriaMet(generation)) {
                System.out.println();
                break;
            }
        }
    }

    private boolean terminationCriteriaMet(int generation) {
        if (generation == 10) {
            return true;
        } // TODO: maybe monitor the fitness of the best individual (or population) over
          // TODO: several generations and if there is no significant improvement, terminate
        return false;
    }

    private void storeBestIndividual(Station bestIndividual, List<Station> bestIndividualList) {
        bestIndividualList.add(bestIndividual);
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
        // evaluate the fitness of each individual
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

        List<Station> parents = new ArrayList<>();  // list of parents

        // calculate the total fitness of the population
        double totalFitness = calculateTotalFitness(population);

        int numberOfParentsToSelect = population.size() / 2;
        // perform selection for each parent
        for (int i = 0; i < numberOfParentsToSelect; i++) {
            // generate a random number between 0 and total fitness
            double randomValue = Math.random() * totalFitness;

            // initialize variables for roulette wheel selection
            double cumulativeFitness = 0.0;
            boolean parentsSelected = false;

            // iterate through the population to select a parent
            for (Station individual : population) {
                cumulativeFitness += individual.getFitness();

                // check if the current individual is selected
                if (cumulativeFitness >= randomValue) {
                    parents.add(individual);
                    parentsSelected = true;
                    break;
                }
            }
            // if no parent is selected, select a random individual
            if (!parentsSelected) {
                int randomIndex = (int) (Math.random() * population.size());
                parents.add(population.get(randomIndex));
            }
        }

        return parents;
    }

    private static double calculateTotalFitness(List<Station> population) {
        double totalFitness = 0.0;
        
        for (Station individual : population) {
            totalFitness += individual.getFitness();
        }
        return totalFitness;
    }

    static List<Station> createOffspring(List<Station> parents, int populationSize) {
        // perform crossover and mutation to create new population

        List<Station> offspring = new ArrayList<>();

        // perform crossover to create offspring until the desired population size is reached
        while (offspring.size() < populationSize) {
            Station parent1 = parents.get((int) (Math.random() * parents.size()));
            Station parent2 = parents.get((int) (Math.random() * parents.size()));

            // apply crossover to create a child
            Station child = crossover(parent1, parent2); // TODO: write

            double mutationProbability = 0.10; // TODO: highly subject to change. fine tune to a good value.
            mutate(child, mutationProbability);  // TODO: write

            offspring.add(child);
        }

        return offspring;
    }

    static Station crossover(Station parent1, Station parent2) {
        Station child = new Station("", 0, 0, "", 0.0);

        int genomeLength = parent1.getGenome().length;

        int crossoverPoint = (int) (Math.random() * genomeLength);

        int[] childGenome = new int[genomeLength];
        for (int i = 0; i < crossoverPoint; i++) {
            childGenome[i] = parent1.getGenome()[i];
        }
        for (int i = 0; i < genomeLength; i++) {
            childGenome[i] = parent2.getGenome()[i];
        }

        child.setName(parent1.getName());
        child.setX(parent1.getX());
        child.setY(parent1.getY());
        String childFunction = (Math.random() < 0.5) ? parent1.getFunction() : parent2.getFunction();
        child.setFunction(childFunction);
        child.setFitness(calculateFitness(child));

        return child;
    }

    static void mutate(Station child, double mutationProbability) {
        int[] childGenome = child.getGenome();

        for (int i = 0; i < childGenome.length; i++) {
            if (Math.random() < mutationProbability) {
                childGenome[i] = generateRandomGeneValue();
            }
        }
    }

    static int generateRandomGeneValue() {
        Random random = new Random();
        return random.nextInt(MAX_HEIGHT);
    }

    static Station getBestIndividual(List<Station> population) {
        Station bestIndividual = population.get(0); // initialize with first individual

        for (Station individual : population) {
            if (individual.getFitness() > bestIndividual.getFitness()) { // compare fitness scores
                bestIndividual = individual;    // update best individual
            }
        }

        return bestIndividual;
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
    int[] getGenome() {
        return new int[]{x, y};
    }

    void setName(String name) { this.name = name; }
    void setX(int x) { this.x = x; }
    void setY(int y) { this.y = y; }
    void setFunction(String function) { this.function = function; }
    void setFitness(double fitness) { this.fitness = fitness; }

}