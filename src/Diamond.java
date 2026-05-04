import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class Diamond extends Item {
    public Diamond(double x, double y, int radius) {
        super(x, y, radius, Color.CYAN);
    }

    @Override
    public int getValue() {
        return 600;
    }

    @Override
    public double getPullSpeed() {
        return 8.0; // Diamonds are light and reel in fast
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        Polygon p = new Polygon();
        p.addPoint((int)x, (int)(y - radius));
        p.addPoint((int)(x + radius), (int)y);
        p.addPoint((int)x, (int)(y + radius));
        p.addPoint((int)(x - radius), (int)y);
        g2d.fillPolygon(p);

        g2d.setColor(Color.BLACK);
        g2d.drawPolygon(p);
    }
}
