import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private final Timer timer;
    private GameState gameState = GameState.MENU;

    // Mouse hover coordinates for button highlights
    private int mouseX = 0;
    private int mouseY = 0;

    private int currentLevel = 1;
    private int score = 0;
    private float timeLeft = 90.0f;
    private final int[] targetScores = {
            600, 1500, 2500, 3800, 5200, 6800, 8500, 10500, 12800, 15500
    };

    private Claw claw;
    private List<Item> items;
    private List<Integer> highscores;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(139, 69, 19));
        setFocusable(true);
        requestFocusInWindow();

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { requestFocusInWindow(); }
        });

        claw = new Claw(WIDTH / 2.0, 80);
        items = new ArrayList<>();
        highscores = SaveManager.loadHighscores();

        // Keyboard Listener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { handleKeyPress(e); }
        });

        // Mouse Listeners
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleMouseClick(e); }
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        timer = new Timer(16, this);
        timer.start();
    }

    // --- Input Handling ---

    private void handleKeyPress(KeyEvent e) {
        if (gameState == GameState.PLAYING) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) claw.shoot();
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) gameState = GameState.PAUSED;
            if (e.getKeyCode() == KeyEvent.VK_S) {
                if (SaveManager.saveGame(currentLevel, score)) {
                    JOptionPane.showMessageDialog(this, "Game Saved!");
                }
            }
        } else if (gameState == GameState.PAUSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameState = GameState.PLAYING;
        }
    }

    private void handleMouseClick(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (gameState == GameState.MENU) {
            if (isHovering(mx, my, 300, 220, 200, 40)) { // New Game
                currentLevel = 1; score = 0; startLevel(currentLevel); gameState = GameState.PLAYING;
            }
            if (isHovering(mx, my, 300, 280, 200, 40)) { // Load Game
                int[] savedData = SaveManager.loadGame();
                if (savedData != null) {
                    currentLevel = savedData[0]; score = savedData[1]; startLevel(currentLevel); gameState = GameState.PLAYING;
                } else JOptionPane.showMessageDialog(this, "No valid save found.");
            }
            if (isHovering(mx, my, 300, 340, 200, 40)) gameState = GameState.HIGHSCORES;
            if (isHovering(mx, my, 300, 400, 200, 40)) gameState = GameState.TUTORIAL; // How to Play

            // Exit Game Button
            if (isHovering(mx, my, 300, 460, 200, 40)) {
                System.exit(0);
            }
        }
        else if (gameState == GameState.PLAYING) {
            // Check Pause button
            if (isHovering(mx, my, WIDTH - 120, 80, 100, 30)) {
                gameState = GameState.PAUSED;
            } else {
                claw.shoot(); // Click anywhere else to shoot
            }
        }
        else if (gameState == GameState.PAUSED) {
            if (isHovering(mx, my, 300, 250, 200, 40)) gameState = GameState.PLAYING; // Resume
            if (isHovering(mx, my, 300, 320, 200, 40)) gameState = GameState.MENU;    // Main Menu
        }
        else if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
            if (isHovering(mx, my, 300, 400, 200, 40)) gameState = GameState.MENU;    // Main Menu
        }
        else if (gameState == GameState.HIGHSCORES || gameState == GameState.TUTORIAL) {
            if (isHovering(mx, my, 300, 500, 200, 40)) gameState = GameState.MENU;    // Back Button
        }
    }

    // THE MISSING METHOD: Checks if mouse coordinates are inside a box
    private boolean isHovering(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    // --- Game Logic ---

    private void startLevel(int level) {
        items.clear();
        claw.reset();
        timeLeft = 90.0f;

        Random rand = new Random();
        spawnItems(5 + level, "GOLD", rand);
        spawnItems(3 + level / 2, "ROCK", rand);
        spawnItems(1 + level / 3, "DIAMOND", rand);
    }

    private void spawnItems(int count, String type, Random rand) {
        for (int i = 0; i < count; i++) {
            int x = 50 + rand.nextInt(WIDTH - 100);
            int y = 200 + rand.nextInt(HEIGHT - 250);

            switch (type) {
                case "GOLD": items.add(new Gold(x, y, 20 + rand.nextInt(25) - 5)); break;
                case "ROCK": items.add(new Rock(x, y, 35)); break;
                case "DIAMOND": items.add(new Diamond(x, y, 12)); break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            timeLeft -= 0.016f;
            claw.update(items, this);

            // Skip level instantly if target met
            if (score >= targetScores[currentLevel - 1]) {
                currentLevel++;
                if (currentLevel > 10) {
                    gameState = GameState.VICTORY;
                    updateHighscores();
                } else startLevel(currentLevel);
            }
            else if (timeLeft <= 0) {
                gameState = GameState.GAME_OVER;
                updateHighscores();
            }
        }
        repaint();
    }

    public void addItemValue(Item item) { score += item.getValue(); }

    private void updateHighscores() {
        highscores.add(score);
        highscores.sort(Collections.reverseOrder());
        if (highscores.size() > 10) highscores = new ArrayList<>(highscores.subList(0, 10));
        SaveManager.saveHighscores(highscores);
    }

    // --- Rendering ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameState == GameState.MENU) drawMenu(g2d);
        else if (gameState == GameState.PLAYING) drawGame(g2d);
        else if (gameState == GameState.PAUSED) { drawGame(g2d); drawPaused(g2d); }
        else if (gameState == GameState.GAME_OVER) drawMessageScreen(g2d, "GAME OVER", "Score: " + score, Color.RED);
        else if (gameState == GameState.VICTORY) drawMessageScreen(g2d, "YOU WIN!", "Final Score: " + score, Color.GREEN);
        else if (gameState == GameState.HIGHSCORES) drawHighscores(g2d);
        else if (gameState == GameState.TUTORIAL) drawTutorial(g2d);
    }

    private void drawButton(Graphics2D g2d, String text, int x, int y, int w, int h) {
        if (isHovering(mouseX, mouseY, x, y, w, h)) g2d.setColor(Color.LIGHT_GRAY);
        else g2d.setColor(Color.WHITE);
        g2d.fillRect(x, y, w, h);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, w, h);

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        int sw = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + (w - sw) / 2, y + 27);
    }

    private void drawMenu(Graphics2D g2d) {
        g2d.setColor(Color.BLACK); g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.YELLOW); g2d.setFont(new Font("Arial", Font.BOLD, 60));
        g2d.drawString("GOLD MINER", 200, 150);

        drawButton(g2d, "New Game", 300, 220, 200, 40);
        drawButton(g2d, "Load Game", 300, 280, 200, 40);
        drawButton(g2d, "Highscores", 300, 340, 200, 40);
        drawButton(g2d, "How to Play", 300, 400, 200, 40);
        drawButton(g2d, "Exit Game", 300, 460, 200, 40);
    }

    private void drawGame(Graphics2D g2d) {
        g2d.setColor(new Color(135, 206, 235)); g2d.fillRect(0, 0, WIDTH, 80);
        g2d.setColor(Color.DARK_GRAY); g2d.fillRect(WIDTH / 2 - 30, 40, 60, 40);

        for (Item item : items) item.draw(g2d);
        claw.draw(g2d);

        g2d.setColor(Color.BLACK); g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Level: " + currentLevel + " / 10", 20, 30);
        g2d.drawString(String.format("Time: %.1f s", timeLeft), 20, 60);
        g2d.drawString("Score: " + score, 600, 30);
        g2d.drawString("Target: " + targetScores[currentLevel - 1], 600, 60);

        // Pause Button on HUD
        drawButton(g2d, "Pause", WIDTH - 120, 80, 100, 30);
    }

    private void drawPaused(Graphics2D g2d) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        g2d.drawString("PAUSED", WIDTH/2 - 120, 150);

        drawButton(g2d, "Resume", 300, 250, 200, 40);
        drawButton(g2d, "Main Menu", 300, 320, 200, 40);
    }

    private void drawMessageScreen(Graphics2D g2d, String title, String sub, Color color) {
        g2d.setColor(Color.BLACK); g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(color); g2d.setFont(new Font("Arial", Font.BOLD, 60));
        g2d.drawString(title, WIDTH/2 - g2d.getFontMetrics().stringWidth(title)/2, 200);
        g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        g2d.drawString(sub, WIDTH/2 - g2d.getFontMetrics().stringWidth(sub)/2, 300);

        drawButton(g2d, "Main Menu", 300, 400, 200, 40);
    }

    private void drawHighscores(Graphics2D g2d) {
        g2d.setColor(Color.BLACK); g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.CYAN); g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("TOP 10 HIGHSCORES", 180, 100);

        g2d.setFont(new Font("Arial", Font.PLAIN, 24)); g2d.setColor(Color.WHITE);
        for (int i = 0; i < highscores.size(); i++) {
            g2d.drawString((i + 1) + ". " + highscores.get(i), 320, 160 + i * 32);
        }
        drawButton(g2d, "Back", 300, 500, 200, 40);
    }

    private void drawTutorial(Graphics2D g2d) {
        g2d.setColor(Color.BLACK); g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.YELLOW); g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("HOW TO PLAY", 250, 100);

        g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String[] instructions = {
                "- The claw swings automatically.",
                "- Press SPACE or CLICK your mouse to shoot the claw.",
                "- Grab items underground to earn points before time runs out.",
                "- Meet the Target Score to automatically advance to the next level.",
                "",
                "ITEM GUIDE:",
                "  * YELLOW (Gold): Normal points, normal pull speed.",
                "  * GRAY (Rock): Terrible points, VERY slow pull speed. Avoid!",
                "  * CYAN (Diamond): Massive points, fast pull speed. Grab these!",
                "",
                "Press 'S' during gameplay to Save your progress."
        };

        for (int i = 0; i < instructions.length; i++) {
            g2d.drawString(instructions[i], 100, 160 + (i * 25));
        }

        drawButton(g2d, "Back", 300, 500, 200, 40);
    }
}
