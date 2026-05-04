import java.io.*;
import java.util.*;

public class SaveManager {

    public static boolean saveGame(int currentLevel, int score) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("savegame.dat"))) {
            pw.println(currentLevel);
            pw.println(score);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static int[] loadGame() {
        File f = new File("savegame.dat");
        if (!f.exists()) return null;
        try (Scanner sc = new Scanner(f)) {
            return new int[]{ sc.nextInt(), sc.nextInt() }; // [level, score]
        } catch (Exception ex) {
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
