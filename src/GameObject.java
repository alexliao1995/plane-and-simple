import java.awt.Graphics2D;


public interface GameObject {
	
	public Position getPosition();
	public float getRotation();
	public void draw(Graphics2D g2d);
	
}
