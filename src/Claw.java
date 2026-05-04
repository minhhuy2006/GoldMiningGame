import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;

public class Claw implements Renderable {
    private double originX, originY;
    private double angle = 0;
    private double angleDir = 1;
    private double length = 40;

    private static final double MIN_LENGTH = 40;
    private static final double MAX_LENGTH = 700;
    private static final double SWING_SPEED = 0.03;
    private static final double SHOOT_SPEED = 12.0;

    private ClawState state = ClawState.SWINGING;
    private Item grabbedItem = null;

    public Claw(double originX, double originY) {
        this.originX = originX;
        this.originY = originY;
    }

    public void reset() {
        state = ClawState.SWINGING;
        length = MIN_LENGTH;
        angle = 0;
        grabbedItem = null;
    }

    public ClawState getState() { return state; }
    public void shoot() { if (state == ClawState.SWINGING) state = ClawState.SHOOTING; }

    public void update(List<Item> items, GamePanel panel) {
        switch (state) {
            case SWINGING:
                angle += angleDir * SWING_SPEED;
                double limit = Math.PI / 2.2;
                if (angle > limit) { angle = limit; angleDir = -1; }
                else if (angle < -limit) { angle = -limit; angleDir = 1; }
                break;

            case SHOOTING:
                length += SHOOT_SPEED;
                double clawX = originX + length * Math.sin(angle);
                double clawY = originY + length * Math.cos(angle);

                // Collision Detection
                for (Item item : items) {
                    double dist = Math.hypot(clawX - item.getX(), clawY - item.getY());
                    if (dist < item.getRadius() + 10) {
                        grabbedItem = item;
                        state = ClawState.REELING;
                        break;
                    }
                }

                if (length > MAX_LENGTH || clawX < 0 || clawX > GamePanel.WIDTH || clawY > GamePanel.HEIGHT) {
                    state = ClawState.REELING;
                }
                break;

            case REELING:
                double pullSpeed = (grabbedItem == null) ? SHOOT_SPEED : grabbedItem.getPullSpeed();
                length -= pullSpeed;

                if (grabbedItem != null) {
                    grabbedItem.setX(originX + length * Math.sin(angle));
                    grabbedItem.setY(originY + length * Math.cos(angle));
                }

                if (length <= MIN_LENGTH) {
                    length = MIN_LENGTH;
                    state = ClawState.SWINGING;
                    if (grabbedItem != null) {
                        panel.addItemValue(grabbedItem);
                        items.remove(grabbedItem);
                        grabbedItem = null;
                    }
                }
                break;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        double tipX = originX + length * Math.sin(angle);
        double tipY = originY + length * Math.cos(angle);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine((int)originX, (int)originY, (int)tipX, (int)tipY);

        AffineTransform old = g2d.getTransform();
        g2d.translate(tipX, tipY);
        g2d.rotate(-angle);

        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(3));

        if (state == ClawState.SHOOTING || state == ClawState.SWINGING) {
            g2d.drawLine(0, 0, -15, 20);
            g2d.drawLine(0, 0, 15, 20);
        } else {
            g2d.drawLine(0, 0, 0, 20);
            g2d.drawLine(0, 0, -10, 15);
            g2d.drawLine(0, 0, 10, 15);
        }
        g2d.setTransform(old);
    }
}
