import java.io.*;
import java.util.*;

public class SaveManager {

    public static boolean saveGame(int level, int score, float timeLeft, Claw claw, List<Item> items) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("savegame.dat"))) {
            pw.println(level);
            pw.println(score);
            pw.println(timeLeft);
            pw.println(claw.getAngle());
            pw.println(claw.getLength());
            pw.println(claw.getState().name());

            // NEW: Save the index of the item the claw is holding (or -1 if empty hands)
            int grabbedIndex = -1;
            if (claw.getGrabbedItem() != null) {
                grabbedIndex = items.indexOf(claw.getGrabbedItem());
            }
            pw.println(grabbedIndex);

            pw.println(items.size());
            for (Item item : items) {
                String type = "GOLD";
                if (item instanceof Rock) type = "ROCK";
                else if (item instanceof Diamond) type = "DIAMOND";
                pw.println(type + " " + item.getX() + " " + item.getY() + " " + item.getRadius());
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static class SaveData {
        public int level, score;
        public float timeLeft;
        public double clawAngle, clawLength;
        public String clawState;
        public int grabbedIndex; // NEW
        public List<Item> items = new ArrayList<>();
    }

    public static SaveData loadGame() {
        File f = new File("savegame.dat");
        if (!f.exists()) return null;
        try (Scanner sc = new Scanner(f)) {
            SaveData data = new SaveData();
            data.level = Integer.parseInt(sc.nextLine());
            data.score = Integer.parseInt(sc.nextLine());
            data.timeLeft = Float.parseFloat(sc.nextLine());
            data.clawAngle = Double.parseDouble(sc.nextLine());
            data.clawLength = Double.parseDouble(sc.nextLine());
            data.clawState = sc.nextLine();

            // NEW: Load the index of the item to attach
            data.grabbedIndex = Integer.parseInt(sc.nextLine());

            int itemCount = Integer.parseInt(sc.nextLine());
            for (int i = 0; i < itemCount; i++) {
                String[] parts = sc.nextLine().split(" ");
                String type = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                int radius = Integer.parseInt(parts[3]);

                if (type.equals("GOLD")) data.items.add(new Gold(x, y, radius));
                else if (type.equals("ROCK")) data.items.add(new Rock(x, y, radius));
                else if (type.equals("DIAMOND")) data.items.add(new Diamond(x, y, radius));
            }
            return data;
        } catch (Exception ex) {
            System.err.println("Old or corrupted save file detected.");
            return null;
        }
    }

    public static List<Integer> loadHighscores() {
        List<Integer> highscores = new ArrayList<>();
        File f = new File("highscores.txt");
        if (!f.exists()) {
            for (int i = 0; i < 10; i++) highscores.add(0);
            saveHighscores(highscores);
            return highscores;
        }
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextInt()) highscores.add(sc.nextInt());
        } catch (IOException e) {
            System.err.println("Error reading highscores.");
        }
        return highscores;
    }

    public static void saveHighscores(List<Integer> highscores) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("highscores.txt"))) {
            for (int s : highscores) pw.println(s);
        } catch (IOException e) {
            System.err.println("Error saving highscores.");
        }
    }
}
