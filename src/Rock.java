import java.awt.Color;

public class Rock extends Item {
    public Rock(double x, double y, int radius) {
        super(x, y, radius, Color.DARK_GRAY);
    }

    @Override
    public int getValue() {
        return 15; // Rocks are worth very little
    }

    @Override
    public double getPullSpeed() {
        return 1.5; // Rocks are very heavy and slow
    }
}
