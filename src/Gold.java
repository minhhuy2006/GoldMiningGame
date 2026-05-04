import java.awt.Color;

public class Gold extends Item {
    public Gold(double x, double y, int radius) {
        super(x, y, radius, Color.YELLOW);
    }

    @Override
    public int getValue() {
        if (radius < 20) return 50;   // Small
        if (radius < 30) return 150;  // Medium
        return 300;                   // Large
    }

    @Override
    public double getPullSpeed() {
        if (radius > 30) return 3.0; // Heavy
        return 5.0;                  // Normal
    }
}
