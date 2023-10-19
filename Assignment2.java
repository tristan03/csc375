package Assignment2;

/*
    Tristan Allen
    Suny Oswego CSC375 Assignment 2

    Exercise in performance measurement
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Assignment2 {

    static List<Player> playerList = new ArrayList<>();

    public static void main(String[] args) {
        playerList = createPlayers();  // get the players for the game

        run();  // start the game
    }

    static List<Player> createPlayers() {
        List<Player> playerList = new ArrayList<>();    // list to hold the players
        int numOfPlayers = 30;  // max players

        for (int i = 0; i < numOfPlayers; i++) {
            Player player = new Player("", 0, 0, 0); // initialize a player
            player.setName("Player" + (i + 1));
            player.setHealth(100);
            player.setScore(0);
            playerList.add(player);
        }
        return playerList;
    }

    static void run() {
        for (int i = 0; i < 499; i++) {
            String input = selectNames();

            String[] names = readInput(input);  // holds individual words for identification for actions

            simulate(names);

        }
        // end of game
        System.out.println("-------------------------------------------------------");
        System.out.println("Scoreboard");
        System.out.println("-------------------------------------------------------");
        for (Player player : playerList) {
            player.scoreboard();
        }
    }

    static String selectNames() {
        String input;
        Random random = new Random();

        Player player1;
        Player player2;

        int randomIdx1 = random.nextInt(playerList.size());
        int randomIdx2 = random.nextInt(playerList.size());

        if (randomIdx1 != randomIdx2) {
            player1 = playerList.get(randomIdx1);
            player2 = playerList.get(randomIdx2);
            input = player1.getName() + " " + player2.getName();
        } else {
            input = selectNames();
        }
        return input;
    }

    static void simulate(String[] names) {
        String namePlayer1 = names[0];
        String namePlayer2 = names[1];

        String action = GenerateAction(namePlayer1, namePlayer2);
        String[] words = readInput(action);

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

        assert player1 != null;
        assert player2 != null;
        if (player1.getName().equalsIgnoreCase(words[0])) {
            ;   // do nothing, names are in the correct order for calculation
        } else {    // flip the players
            Player temp = player1;
            player1 = player2;
            player2 = temp;
        }

        if (possibleAction1.equalsIgnoreCase("headshots")) {
            calculateOneShotKill(player1, player2, action);
        } else if (possibleAction1.equalsIgnoreCase("melees")) {
            calculateOneShotKill(player1, player2, action);
        } else if (isInteger(possibleAction2)) {
            assert possibleAction2 != null;
            calculateShot(player1, player2, Integer.parseInt(possibleAction2), action);
        }
    }

    // randomly decides how the two players will interact
    static String GenerateAction(String name1, String name2) {
        Random random = new Random();
        String action;

        // randomness
        int randomInt = random.nextInt(3); // generates 0 , 1, or 2
        int randomShots = random.nextInt(6); // generates 0 - 4

        String[] player = randomPlayer(name1, name2);

        if (randomInt == 0) {
            action = player[0] + " headshots " + player[1];
        } else if (randomInt == 1) {
            if (randomShots == 0) {
                action = player[0] + " shoots and misses " + player[1] + "\n";
            } else if (randomShots == 1) {
                action = player[0] + " shoots " + player[1] + " " + randomShots + " time";
            } else {
                action = player[0] + " shoots " + player[1] + " " + randomShots + " times";
            }
        } else {
            action = player[0] + " melees " + player[1];
        }

        return action;
    }

    static String[] randomPlayer(String name1, String name2) {
        Random random = new Random();
        int randomInt = random.nextInt(10); // generates 0 - 9.

        Player player1 = findPlayer(name1);
        Player player2 = findPlayer(name2);

        assert player1 != null;
        int health1 = player1.getHealth();
        assert player2 != null;
        int health2 = player2.getHealth();

        // favor the player with higher health, 70/30 odds
        if (randomInt < 6) {
            if (health1 > health2) {
                return new String[] {name1, name2};
            } else if (health2 > health1) {
                return new String[] {name2, name1};
            }
        }

        // if the random number is >= 7 or health is equal, swap randomly.
        if (random.nextInt(2) == 0) {
            return new String[] {name1, name2};
        } else {
            return new String[] {name2, name1};
        }
    }

    static Player findPlayer(String name) {
        for (Player player : playerList) {
            if (name.equalsIgnoreCase(player.getName())) {
                return player;
            }
        }
        return null;    // no player found
    }


    static void calculateShot(Player player1, Player player2, int numShots, String action) {
        int damage = 20; // every one shot is 20 damage
        int totalDamageDealt;   // total damage dealt, 20 * the number of shots landed
        int newScore;   // new score for player 1
        int killScore = 100;    // 100 points for a kill
        int killCount = player1.getKillCount();

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
        System.out.println(action);
        if (health != 0) {
            int newHealth = player2.getHealth() - totalDamageDealt;
            player2.setHealth(newHealth);
            System.out.println(player2.getName() + " survived with " + player2.getHealth() + " health \n");

            newScore = player1.getScore() + (totalDamageDealt / 2); // 10 points for every hit
        } else {
            System.out.println(player2.getName() + " was killed by " + player1.getName() + "!\n");
            player2.setHealth(0);
            player1.setKillCount(killCount + 1);

            newScore = player1.getScore() + (totalDamageDealt / 2) + killScore;
        }
        player1.setScore(newScore);
//        player1.identify();
//        player2.identify();

        if (player2.getHealth() == 0) {
            respawn(player2);
        }
        //run();
    }

    static void calculateOneShotKill(Player player1, Player player2, String action) {
        int damage = 100;

        int playerHitCurrentHealth = player2.getHealth();
        int playerCurrentScore = player1.getScore();

        int health = playerHitCurrentHealth - damage;

        int killCount = player1.getKillCount();

        if (health <= 0) {
            // handle player1 score increase & acknowledgement
            System.out.println(action);
            System.out.println(player2.getName() + " was killed by " + player1.getName() + "!\n");
            int newScore = playerCurrentScore + 100;
            player1.setScore(newScore);
            player1.setKillCount(killCount + 1);

            // handle player2 health
            player2.setHealth(0);

//            player1.identify();
//            player2.identify();

            respawn(player2);

            //run();
        }
    }

    // set players health back to 100. maintain score
    static void respawn(Player player) {
        player.setHealth(100);
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
    int killCount;

    public Player(String name, int health, int score, int killCount) {
        this.name = name;
        this.health = health;
        this.score = score;
        this.killCount = killCount;
    }

    void scoreboard() {
        System.out.println(getName() + ":" + " Health: " + getHealth() + " | Kills: " + getKillCount() + " | Score: " + getScore());
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
    void setKillCount(int killcount) {
        this.killCount = killcount;
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
    int getKillCount() { return killCount; }
}
