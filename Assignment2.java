package Assignment2;

/*
    Tristan Allen
    Suny Oswego CSC375 Assignment 2

    Exercise in performance measurement
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Assignment2 {

    static List<Player> playerList = new ArrayList<>();

    public static void main(String[] args) {
        playerList = createPlayers();  // get the players for the fame

        run();  // start the game
    }

    static List<Player> createPlayers() {
        List<Player> playerList = new ArrayList<>();    // list to hold the players
        int numOfPlayers = 50;  // max players

        for (int i = 0; i < numOfPlayers; i++) {
            Player player = new Player("", 0, 0); // initialize a player
            player.setName("Player" + (i + 1));
            player.setHealth(100);
            player.setScore(0);
            playerList.add(player);
        }
        return playerList;
    }

    static void run() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("exit")) {
            System.exit(0);
        }

        String[] words = readInput(input);  // holds individual words for identification for actions

        simulate(words);

    }

    static void simulate(String[] words) {
        System.out.println();
        String namePlayer1 = words[0];
        String namePlayer2 = words[2];

        String possibleAction1 = words[1];  // for something like "player1 headshots player2"
        String possibleAction2 = null;
        if (words.length > 3) {
            possibleAction2 = words[3];  // for something like "player1 shoots player2 3 times"
        }

        Player player1 = null;
        Player player2 = null;

        // find current player info
        for (Player player : playerList) {
            if (player.getName().equalsIgnoreCase(namePlayer1)) {
                player1 = player;
            } else if (player.getName().equalsIgnoreCase(namePlayer2)) {
                player2 = player;
            }
        }

        if (possibleAction1.equalsIgnoreCase("headshots")) {
            assert player1 != null;
            assert player2 != null;
            calculateOneShotKill(player1, player2);
        } else if (possibleAction1.equalsIgnoreCase("melees")) {
            assert player1 != null;
            assert player2 != null;
            calculateOneShotKill(player1, player2);
        } else if (isInteger(possibleAction2)) {
            assert player2 != null;
            assert possibleAction2 != null;
            calculateShot(player1, player2, Integer.parseInt(possibleAction2));
        }
    }

    static void calculateShot(Player player1, Player player2, int numShots) {
        int damage = 20; // every one shot is 20 damage
        int totalDamageDealt;   // total damage dealt, 20 * the number of shots landed
        int newScore;   // new score for player 1
        int killScore = 100;    // 100 points for a kill

        if (numShots > 5) {
            totalDamageDealt = 100;
        } else {
            totalDamageDealt = damage * numShots;
        }

        int health = player2.getHealth() - totalDamageDealt; // player2 new health

        if (health < 0) {
            health = 0;
        }

        // deduct health from player2
        if (health != 0) {
            int newHealth = player2.getHealth() - totalDamageDealt;
            player2.setHealth(newHealth);

            newScore = player1.getScore() + (totalDamageDealt / 2); // 10 points for every hit
            player1.setScore(newScore);
        } else {
            System.out.println(player2.getName() + " was killed by " + player1.getName() + "!\n");
            player2.setHealth(0);

            newScore = player1.getScore() + (totalDamageDealt / 2) + killScore;
            player1.setScore(newScore);
        }
        player1.identify();
        player2.identify();

        if (player2.getHealth() == 0) {
            respawn(player2);
        }

        run();
    }

    static void calculateOneShotKill(Player player1, Player player2) {
        int damage = 100;

        int playerHitCurrentHealth = player2.getHealth();
        int playerCurrentScore = player1.getScore();

        int health = playerHitCurrentHealth - damage;

        if (health <= 0) {
            // handle player1 score increase & acknowledgement
            System.out.println(player2.getName() + " was killed by " + player1.getName() + "!\n");
            int newScore = playerCurrentScore + 100;
            player1.setScore(newScore);

            // handle player2 health
            player2.setHealth(0);

            player1.identify();
            player2.identify();

            respawn(player2);

            run();
        }
    }

    // set players health back to 100. maintain score
    static void respawn(Player player) {
        player.setHealth(100);
        System.out.println(player.getName() + " has respawned! \n");
    }

    // check if a string represents an integer
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static String[] readInput(String input) {
        return input.split("\\s+"); // split string by whitespace
    }
}

class Player {
    String name;
    int health;
    int score;

    public Player(String name, int health, int score) {
        this.name = name;
        this.health = health;
        this.score = score;
    }

    void identify() {
        System.out.println(getName() + " has " + getHealth() + " health remaining.\n" +
                            getName() + " has a score of " + getScore() + ".\n");
    }

    void setName(String name) {
        this.name = name;
    }
    void setHealth(int health) {
        this.health = health;
    }
    void setScore(int score) {
        this.score = score;
    }

    String getName() {
        return name;
    }
    int getHealth() {
        return health;
    }
    int getScore() {
        return score;
    }
}
