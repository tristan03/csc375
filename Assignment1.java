package Assignment1;

/*
    Tristan Allen
    Suny Oswego CSC375
    Assignment1

    A parallel genetic algorithm for a Facilities Layout problem
 */

import java.util.*;

public class Assignment1 {
    public static void main(String[] args) {
        int height = 30;
        int width = 30;
        FactoryFloor factoryFloor = new FactoryFloor(height, width);

        // run genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(50, factoryFloor);
        ga.run();

        System.out.println();
    }
}

class GeneticAlgorithm {
    final static int MAX_GENERATIONS = 20;
    final static double MUTATION_PROBABILITY = 0.10;
    final static int MAX_GENE_SIZE = 30;
    private List<Station> population;
    static int stationCounter = 0;
    static Set<String> occupiedLocations = new HashSet<>();

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
        List<Station> bestIndividualList = new ArrayList<>();
        Set<String> uniqueIndividualNames = new HashSet<>();
//        System.out.println("Initial population: ");
//        evaluatePopulationFitness(population);
//        for (Station station : population) {
//            station.identify();
//        }
//        System.out.println();
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            //occupiedLocations.clear();

            evaluatePopulationFitness(population);

            List<Station> parents = selectParents(population);

            population = createOffspring(parents, population.size(), generation);

            Station bestIndividual = getBestIndividual(population);


            String bestIndName = bestIndividual.getName();

            if (uniqueIndividualNames.add(bestIndName)) {
                bestIndividualList.add(bestIndividual);
            }

            population.remove(bestIndividual);

            if (terminationCriteriaMet(generation)) {
                break;
            }

        }
        //System.out.println("Ending population: ");
        for (Station station : bestIndividualList) {
            station.identify();
        }
    }

    private boolean terminationCriteriaMet(int generation) {
        if (generation == MAX_GENERATIONS) {
            return true;
        } // TODO: maybe monitor the fitness of the best individual (or population) over
          // TODO: several generations and if there is no significant improvement, terminate
        return false;
    }

    private static String generateUniqueName() {
        return "Station" + stationCounter++;
    }

    private static Station generateRandomPopulation(FactoryFloor factoryFloor, int count) {
        Random random = new Random();
        int height = factoryFloor.getHeight();
        int width = factoryFloor.getWidth();

        Station station;
        do {
            int randomHeight = random.nextInt(height);
            int randomWidth = random.nextInt(width);

            station = new Station("", randomHeight, randomWidth, "", 0.0);
        } while (!occupiedLocations.add(station.getLocationKey()));

        station.setName("Station" + count);
        station.setFunction(randomlyAssignFunction());
        station.setFitness(0.0);

        return new Station(station.getName(), station.getX(), station.getY(), station.getFunction(), station.getFitness());
    }

    private static String randomlyAssignFunction() {
        String function1 = "Assembly";
        String function2 = "Machinist";

        Random random = new Random();

        int randomNumber = random.nextInt(3);

        if (randomNumber == 1) {
            return function1;
        } else {
            return function2;
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

    static List<Station> createOffspring(List<Station> parents, int populationSize, int generation) {
        // perform crossover and mutation to create new population

        List<Station> offspring = new ArrayList<>();

//        if (generation != 0) {
//            parents.remove(getBestIndividual(parents));
//        }

        // perform crossover to create offspring until the desired population size is reached
        while (offspring.size() < populationSize) {
            Station parent1 = parents.get((int) (Math.random() * parents.size()));
            Station parent2 = parents.get((int) (Math.random() * parents.size()));

            // apply crossover to create a child
            Station child = crossover(parent1, parent2);

            mutate(child);

            if (!occupiedLocations.contains(child.getLocationKey())) {
                offspring.add(child);
                occupiedLocations.add(child.getLocationKey());
            }
        }

        return offspring;
    }

    static Station crossover(Station parent1, Station parent2) {
        Station child = new Station("", 0, 0, "", 0.0);

        int genomeLength = parent1.getGenome().length;

        int crossoverPoint = 1 + (int) (Math.random() * genomeLength - 2);

        int[] childGenome = new int[genomeLength];
        for (int i = 0; i < crossoverPoint; i++) {
            childGenome[i] = parent1.getGenome()[i];
        }
        for (int i = crossoverPoint; i < genomeLength; i++) {
            childGenome[i] = parent2.getGenome()[i];
        }

        child.setName(generateUniqueName());
        child.setX(parent1.getX());
        child.setY(parent1.getY());
        String childFunction = (Math.random() < 0.5) ? parent1.getFunction() : parent2.getFunction();
        child.setFunction(childFunction);
        child.setFitness(calculateFitness(child));

        return child;
    }

    static void mutate(Station child) {
        int[] childGenome = child.getGenome();
        Station tempChild = new Station("", childGenome[0], childGenome[1], "", 0.0);

        for (int i = 0; i < childGenome.length; i++) {
            if (Math.random() < MUTATION_PROBABILITY) {
                childGenome[i] = generateRandomGeneValue();
            }
        }

        // Use a temporary station to check for uniqueness after mutation
        tempChild.setGenome(childGenome);

        // Ensure unique location after mutation
        while (!occupiedLocations.add(tempChild.getLocationKey())) {
            // If the location is not unique, revert the mutation and retry
            for (int i = 0; i < childGenome.length; i++) {
                if (Math.random() < MUTATION_PROBABILITY) {
                    childGenome[i] = generateRandomGeneValue();
                }
            }
            tempChild.setGenome(childGenome);
        }

        // Apply the mutation to the child
        child.setGenome(childGenome);

        // Remove the original location to avoid false positives when checking for duplicates
        occupiedLocations.remove(child.getLocationKey());
    }


    static int generateRandomGeneValue() {
        Random random = new Random();
        return random.nextInt(MAX_GENE_SIZE);
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

    void identify() {
        System.out.println("Name: " + getName() + " | Genome: " + Arrays.toString(getGenome()) + " | Function : " +
                getFunction() + " | Fitness: " + getFitness());
    }

    public String getLocationKey() {
        return String.format("(%d, %d)", getX(), getY());
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
    void setGenome(int[] genome) {
        this.x = genome[0];
        this.y = genome[1];
    }

}