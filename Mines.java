package Assignment1;

/*
    Tristan Allen
    Suny Oswego CSC375
    Assignment1

    A parallel genetic algorithm for a Facilities Layout problem
 */


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Assignment1 {
    public static void main(String[] args) {
        int populationSize = 50;
        GeneticAlgorithm ga = new GeneticAlgorithm(populationSize);
        ga.run();
    }
}

class GeneticAlgorithm {
    final static int MAX_GENERATIONS = 100;
    final static double MUTATION_PROBABILITY = 0.10;
    final static int MAX_GENE_SIZE = 30;
    static int stationCounter = 0;
    private static List<Station> population;
    static Set<String> occupiedLocations = new HashSet<>();

    static Map<Pair<Station, Station>, Double> stationPairMap;  // map to hold a pair of Stations -> affinity value
    public GeneticAlgorithm(int populationSize) {
        population = initializePopulation(populationSize);
    }
    private static final ExecutorService executorService;
    private static final Lock lock = new ReentrantLock();

    static {
        int numThreads = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(numThreads);
    }

    private static List<Station> initializePopulation(int size) {
        List<Station> population = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Station station = generateRandomPopulation(i);
            population.add(station);
        }

        return population;
    }

    private static Station generateRandomPopulation(int count) {
        Random random = new Random();
        int height = 30;
        int width = 30;

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

    public void run() {
        List<Station> bestIndividualList = new ArrayList<>();
        Set<String> uniqueIndividualNames = new HashSet<>();

        JFrame frame = new JFrame("Factory Floor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FactoryFloor factoryFloor = new FactoryFloor(100, 100, population);

        frame.getContentPane().add(factoryFloor);
        frame.setSize(500, 500);
        frame.setVisible(true);

        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            evaluatePopulationFitness(population);

            stationPairMap = evaluatePairPopulationAffinity(population);

            List<Station> parents = selectParents(population);

            population = createOffspring(parents, population.size());

            Station bestIndividual = getBestIndividual(population);

            String bestIndName = bestIndividual.getName();

            if (uniqueIndividualNames.add(bestIndName)) {
                bestIndividualList.add(bestIndividual);
            }

            bestIndividualList.sort(Comparator.comparingDouble(Station::getFitness));

            population.set(0, bestIndividual);

            if (generation % 20 == 0) {
                factoryFloor.displayFloor(stationPairMap, 2000);    // periodically update graph (every 20 generations)
            }

            if (generation == MAX_GENERATIONS - 1) {
                factoryFloor.displayFloor(stationPairMap, 1);    // display final population
                break;
            }
        }

    }

    static void evaluatePopulationFitness(List<Station> population) {
        for (Station station : population) {
            double fitness = calculateFitness(station);
            station.setFitness(fitness);
        }
    }

    private static double calculateFitness(Station station) {
        double distance = calculateDistance(station);
        double functionValue = calculateFunctionValue(station);

        // fitness score
        return 1.0 / (distance + functionValue);
    }

    private static double calculateDistance(Station station) {
        double x = station.getX();
        double y = station.getY();

        return Math.sqrt(x * x + y * y);
    }

    private static double calculateFunctionValue(Station station) {
        String function = station.getFunction();

        return switch (function) {
            case "Assembly", "Machinist" -> 1.0;
            default -> 0.0;
        };
    }


    // find best pair for each station (affinity of placing stations A and B near each other based on function and distance)
    private static Map<Pair<Station, Station>, Double> evaluatePairPopulationAffinity(List<Station> population) {
        Map<Pair<Station, Station>, Double> affinityMap = new HashMap<>();  // map to return affinity scores for each pair
        Pair<Station, Station> currentBestPair = null;
        double currentBestAffinity = 0.0;
        double affinity;

        for (int i = 0; i < population.size(); i++) {   // compare station A to every station B
            Station stationA = population.get(i);

            for (int j = 0; j < population.size(); j++) {
                if (i != j) {
                    Station stationB = population.get(j);

                    affinity = calculatePairAffinity(stationA, stationB);

                    // find the best pair
                    if (currentBestPair == null) {
                        currentBestPair = new Pair<>(stationA, stationB);
                        currentBestAffinity = affinity;
                    } else if (affinity > currentBestAffinity){
                        currentBestAffinity = affinity;
                        currentBestPair = new Pair<>(stationA, stationB);
                    }
                }
            }
            // add best pair to the affinityMap
            assert currentBestPair != null;
            affinityMap.put(new Pair<>(currentBestPair.getFirst(), currentBestPair.getSecond()), currentBestAffinity);
            currentBestPair = null;
        }

        return affinityMap;
    }


    private static double calculatePairAffinity(Station station1, Station station2) {
        // calculate affinity based on distance and function
        double distanceAffinity = calculatePairDistanceAffinity(station1, station2);
        double functionAffinity = calculateFunctionAffinity(station1, station2);

        return distanceAffinity * functionAffinity;
    }

    // calculate affinity based on distance
    private static double calculatePairDistanceAffinity(Station stationA, Station stationB) {
        double xa = stationA.getX();
        double ya = stationA.getY();

        double xb = stationB.getX();
        double yb = stationB.getY();

        double distance = Math.sqrt((xa - xb) * (xa - xb) + (ya - yb) * (ya - yb));

        return 1.0 / (distance + 1);
    }

    // calculate affinity based on function
    private static double calculateFunctionAffinity(Station stationA, Station stationB) {
        String functionA = stationA.getFunction();
        String functionB = stationB.getFunction();

        if (functionA.equals(functionB)) {
            return 1.0;        // weight higher if they have the same function
        } else {
            return 0.5;
        }
    }

    static List<Station> selectParents(List<Station> population) {
        List<Station> parents = new ArrayList<>();

        double totalFitness = calculateTotalFitness(population);    // total fitness across the whole population

        for (int i = 0; i < population.size(); i++) {
            double randomValue = Math.random() * totalFitness;

            double cumulativeFitness = 0.0;
            boolean parentsSelected = false;

            for (Station station : population) {
                cumulativeFitness += station.getFitness();

                if (cumulativeFitness >= randomValue) {
                    parents.add(station);
                    parentsSelected = true;
                    break;
                }
            }

            // if no parent is selected, select a random station
            if (!parentsSelected) {
                int randomIndex = (int) (Math.random() * population.size());
                parents.add(population.get(randomIndex));
            }
        }

        return parents;
    }

    // calculate total fitness across the whole population
    private static double calculateTotalFitness(List<Station> population) {
        double totalFitness = 0.0;

        for (Station individual : population) {
            totalFitness += individual.getFitness();
        }
        return totalFitness;
    }

    static List<Station> createOffspring(List<Station> parents, int populationSize) {
        List<Station> offspring = new ArrayList<>();

        while(offspring.size() < populationSize) {
            Station parent1 = parents.get((int) (Math.random() * parents.size()));
            Station parent2 = parents.get((int) (Math.random() * parents.size()));

            Station child = crossover(parent1, parent2);
            mutate(child);

            // don't add duplicate locations
            if (!occupiedLocations.contains(child.getLocationKey())) {
                offspring.add(child);
                occupiedLocations.add(child.getLocationKey());
            }
        }
        return offspring;
    }

    // crossover method running in parallel
    static Station crossover(Station parent1, Station parent2) {
        Station child = new Station("", 0, 0, "", 0.0);

        int genomeLength = parent1.getGenome().length;

        int crossoverPoint = 1 + (int) (Math.random() * genomeLength - 2);

        // running in parallel...
        Callable<Station> crossoverTask = () -> {
            int[] childGenome = new int[genomeLength];
            for (int i = 0; i < crossoverPoint; i++) {
                childGenome[i] = parent1.getGenome()[i];
            }
            for (int i = crossoverPoint; i < genomeLength; i++) {
                childGenome[i] = parent2.getGenome()[i];
            }

            lock.lock();
            try {
                child.setName(generateUniqueName());    // unique name to avoid duplicate names
                child.setX(childGenome[0]);
                child.setY(childGenome[1]);
                child.setFunction(crossoverFunction(parent1, parent2)); // randomly choose one of the parents functions
                child.setFitness(calculateFitness(child));
            } finally {
                lock.unlock();
            }
            return child;
        };

        try {
            // submit the task to the executor service
            Future<Station> future = executorService.submit(crossoverTask);

            // wait for the task to complete
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        return child;
    }

    // randomly choose one of the parents functions
    static String crossoverFunction(Station parent1, Station parent2) {
        Random random = new Random();
        int randomChoice = random.nextInt(2);

        return (randomChoice == 0) ? parent1.getFunction() : parent2.getFunction();
    }


    // mutate method running in parallel
    static void mutate(Station child) {
        // running in parallel...
        Callable<Void> mutateTask = () -> {
            int[] childGenome = child.getGenome();
            Station tempChild = new Station("", childGenome[0], childGenome[1], "", 0.0);

            for (int i = 0; i < childGenome.length; i++) {
                if (Math.random() < MUTATION_PROBABILITY) {
                    childGenome[i] = generateRandomGeneValue();
                    lock.lock();
                    try {
                        child.setFunction(randomlyAssignFunction()); // randomly assign function if mutation occurs so one function doesn't dominate the other
                    } finally {
                        lock.unlock();
                    }
                }
            }

            lock.lock();
            try {
                // use a temporary station to check for uniqueness after mutation
                tempChild.setGenome(childGenome);

                int maxRetries = 100;

                // ensure unique location after mutation
                while (!occupiedLocations.add(tempChild.getLocationKey())) {
                    // if the location is not unique, revert the mutation and retry
                    for (int i = 0; i < childGenome.length; i++) {
                        if (Math.random() < MUTATION_PROBABILITY) {
                            childGenome[i] = generateRandomGeneValue();
                        }
                    }

                    if (maxRetries-- == 0) {
                        break;
                    }

                    tempChild.setGenome(childGenome);
                }

                // apply the mutation to the child
                child.setGenome(childGenome);

                // remove the original location to avoid false positives when checking for duplicates
                occupiedLocations.remove(child.getLocationKey());
            } finally {
                lock.unlock();
            }

            return null;
        };

        try {
            // submit the task to the executor service
            Future<Void> future = executorService.submit(mutateTask);

            // wait for the task to complete
            future.get();
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }

    private static String generateUniqueName() {
        return "Station" + stationCounter++;
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

class RepaintTask extends TimerTask {
    private final FactoryFloor factoryFloor;

    public RepaintTask(FactoryFloor factoryFloor) {
        this.factoryFloor = factoryFloor;
    }


    @Override
    public void run() {
        SwingUtilities.invokeLater(factoryFloor::repaint);
    }
}

class FactoryFloor extends JPanel {
    final int height;
    final int width;

    List<Station> population;

    Map<Pair<Station, Station>, Double> stationPairMap;

    public FactoryFloor(int height, int width, List<Station> population) {
        this.height = height;
        this.width = width;
        this.population = population;
    }

    public void displayFloor(Map<Pair<Station, Station>, Double> stationPairMap, int repaintDelay) {
        this.stationPairMap = stationPairMap;

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new RepaintTask(this), 0, repaintDelay);

        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (stationPairMap != null) {
            List<Pair<Station, Station>> pairList = new ArrayList<>(stationPairMap.keySet());

            for (Pair<Station, Station> pair : pairList) {
                Station first = pair.getFirst();
                Station second = pair.getSecond();

                int firstX = first.getX() * 15; // scale to fit canvas
                int firstY = first.getY() * 15; // scale to fit canvas

                int secondX = second.getX() * 15; // scale to fit canvas
                int secondY = second.getY() * 15; // scale to fit canvas

                // set colors for each function, and plot
                if (first.getFunction().equals("Machinist")) {
                    g.setColor(Color.BLUE);
                    g.fillRect(firstX, firstY, 10, 10);
                    if (second.getFunction().equals("Machinist")) {
                        g.setColor(Color.BLUE);
                        g.fillRect(firstX, firstY, 10, 10);
                    } else if (second.getFunction().equals("Assembly")) {
                        g.setColor(Color.GREEN);
                        g.fillOval(secondX, secondY, 10, 10);
                    }
                } else {
                    g.setColor(Color.GREEN);
                    g.fillOval(firstX, firstY, 10, 10);
                }
            }
        }
    }
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

class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}