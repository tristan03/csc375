package Assignment1;

/*
    Tristan Allen
    Suny Oswego CSC375
    Assignment1

    A parallel genetic algorithm for a Facilities Layout problem
 */

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.*;


public class Assignment1 {
    public static void main(String[] args) {
        int height = 30;
        int width = 30;
        FactoryFloor factoryFloor = new FactoryFloor(height, width);

        int populationSize = 50;

        // run genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(populationSize, factoryFloor);
        ga.run();
    }
}

class GeneticAlgorithm {
    final static int MAX_GENERATIONS = 500;
    final static double MUTATION_PROBABILITY = 0.50;
    final static int MAX_GENE_SIZE = 30;
    private List<Station> population;
    static int stationCounter = 0;
    static Set<String> occupiedLocations = new HashSet<>();
    private static final ExecutorService executorService;
    private static final Lock lock = new ReentrantLock();

    static {
        int numThreads = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(numThreads);
    }

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

        JFrame frame = new JFrame("Factory Floor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FactoryFloor factoryFloor = new FactoryFloor(100, 100, population);

        frame.getContentPane().add(factoryFloor);
        frame.setSize(700, 700);
        frame.setVisible(true);

//        Timer timer = new Timer(true);
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                SwingUtilities.invokeLater(factoryFloor::repaint);
//            }
//        }, 0, 100);

        try {
            for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
                evaluatePopulationFitness(population);  // running in parallel

                List<Station> parents = selectParents(population);  // running in parallel

                population = createOffspring(parents, population.size(), generation);   // methods inside running in parallel

                Station bestIndividual = getBestIndividual(population);

                String bestIndName = bestIndividual.getName();

                if (uniqueIndividualNames.add(bestIndName)) {
                    bestIndividualList.add(bestIndividual);
                }

                bestIndividualList.sort(Comparator.comparingDouble(Station::getFitness));

                population.set(0, bestIndividual);

                if (generation % 20 == 0) {
                    factoryFloor.displayFloor(population, 2000);    // periodically update graph (every 2 seconds)
                }

                if (generation == MAX_GENERATIONS - 1) {
//                    timer.cancel();
                    renameStations(population);
                    factoryFloor.displayFloor(population, 1);    // display last population
                    break;
                }
            }
        } finally {
            executorService.shutdown();
        }
    }


    private static String generateUniqueName() {
        return "Station" + stationCounter++;
    }

    private void renameStations(List<Station> population) {
        for (int i = 0; i < population.size(); i++) {
            population.get(i).setName("Station" + i);
        }
    }

    private static Station generateRandomPopulation(FactoryFloor factoryFloor, int count) {
        Random random = new Random();
        int height = factoryFloor.getFloorHeight();
        int width = factoryFloor.getFloorWidth();

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
        try {
            List<Callable<Void>> tasks = new ArrayList<>();

            for (Station station : population) {
                tasks.add(() -> {
                    double fitness = calculateFitness(station);

                    lock.lock();
                    try {
                        station.setFitness(fitness);
                    } finally {
                        lock.unlock();
                    }
                    return null;
                });
            }

            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

        return Math.sqrt(x * x + y * y);
    }

    private static double calculateFunctionValue(Station station) {
        String function = station.getFunction();

        // TODO: probably change this. very basic for now just to move on in implementation
        return switch (function) {
            case "Assembly" -> 0.8;
            case "Machinist" -> 1.0;
            default -> 0.0;
        };
    }

    static List<Station> selectParents(List<Station> population) {
        // select parents for crossover

        List<Station> parents = new ArrayList<>();  // list of parents

        // calculate the total fitness of the population
        double totalFitness = calculateTotalFitness(population);

        //int numberOfParentsToSelect = population.size() / 2;

        // create a list of tasks for parallel execution
        List<Callable<Station>> tasks = new ArrayList<>();

        for (int i = 0; i < population.size() / 2; i++) {
            tasks.add(() -> {
                double randomValue = Math.random() * totalFitness;
                double cumulativeFitness = 0.0;

                for (Station individual : population) {
                    cumulativeFitness += individual.getFitness();

                    if (cumulativeFitness >= randomValue) {
                        return individual;
                    }
                }

                // if no parent is selected, select a random individual
                int randomIndex = (int) (Math.random() * population.size());
                return population.get(randomIndex);
            });
        }

        try {
            // invokeAll will execute tasks in parallel and return a list of Futures
            List<Future<Station>> results = executorService.invokeAll(tasks);

            // extract results from the completed Futures
            for (Future<Station> result : results) {
                parents.add(result.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
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

        // perform crossover to create offspring until the desired population size is reached
        while (offspring.size() < populationSize) {
            Station parent1 = parents.get((int) (Math.random() * parents.size()));
            Station parent2 = parents.get((int) (Math.random() * parents.size()));

            Station child = crossover(parent1, parent2);

            mutate(child, generation);

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
                child.setName(generateUniqueName());
                child.setX(childGenome[0]);
                child.setY(childGenome[1]);
                //String childFunction = (Math.random() < 0.5) ? parent1.getFunction() : parent2.getFunction();
                child.setFunction(randomlyAssignFunction());
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

    static void mutate(Station child, int generation) {
        Callable<Void> mutateTask = () -> {
            int[] childGenome = child.getGenome();
            Station tempChild = new Station("", childGenome[0], childGenome[1], "", 0.0);

            for (int i = 0; i < childGenome.length; i++) {
                if (Math.random() < MUTATION_PROBABILITY) {
                    childGenome[i] = generateRandomGeneValue();
                }
            }

            lock.lock();
            try {
                int temp = generation;
                // Use a temporary station to check for uniqueness after mutation
                tempChild.setGenome(childGenome);

                int maxRetries = 100;

                // ensure unique location after mutation
                while (!occupiedLocations.add(tempChild.getLocationKey())) {
                    // If the location is not unique, revert the mutation and retry
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
            ex.printStackTrace();  // Handle exceptions appropriately
        }
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

    private Timer timer;

    List<Station> population;

    public FactoryFloor(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public FactoryFloor(int height, int width, List<Station> population) {
        this.height = height;
        this.width = width;
        this.population = population;
    }

    public void displayFloor(List<Station> updatedPopulation, int repaintDelay) {
        this.population = updatedPopulation;

//        if (timer != null) {
//            timer.cancel();
//        }

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new RepaintTask(this), 0, repaintDelay);

        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw stations
        for (Station station : population) {
            int x = station.getX() * 20;
            int y = station.getY() * 20;

            if (station.getFunction().equals("Machinist")) {
                g.setColor(Color.BLUE);
                g.fillRect(x, y, 10, 10);
            } else {
                g.setColor(Color.GREEN);
                g.fillOval(x, y, 10, 10);
            }
            g.setColor(Color.BLACK);
            g.drawString(station.getName(), x, y);
        }
    }
    int getFloorHeight() {
        return height;
    }
    int getFloorWidth() {
        return width;
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