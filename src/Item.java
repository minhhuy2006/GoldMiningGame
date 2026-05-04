import java.awt.Color;
import java.awt.Graphics2D;

public abstract class Item implements Renderable {
    protected double x, y;
    protected int radius;
    protected Color color;

    public Item(double x, double y, int radius, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public int getRadius() { return radius; }

    // Abstract methods to be defined by child classes
    public abstract int getValue();
    public abstract double getPullSpeed();

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval((int)x - radius, (int)y - radius, radius * 2, radius * 2);
        g2d.setColor(Color.BLACK);
        g2d.drawOval((int)x - radius, (int)y - radius, radius * 2, radius * 2);
    }
}
