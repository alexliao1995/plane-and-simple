import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;

import javax.imageio.ImageIO;


public class Runway implements GameObject {

	// Runway properties
	private Position pos;
	private float orientation;
	private int length;
	private int width;

	// Runway "gate" properties
	private float orientTolerance;
	private float distanceOut;
	private int mouth;
	private int back; 

	// Waypoints to guide planes to land
	private Position[] landPoints;
	private String runwayImageFile;
	private BufferedImage runwayImage;


	public Runway() {

		// sets up runway parameters
		pos = new Position(600, 250);
		orientation = 50; 
		orientTolerance = 30;
		length = 300;
		width = 40;
		distanceOut = 20;
		mouth = 50;
		back = 30;

		// calculate waypoints dynamically based on parameters
		landPoints = new Position[3];
		landPoints[0] = new Position(pos.x + width / 2 * (float) Math.sin(orientation / 180 * Math.PI) + length / 8 * (float) Math.sin(orientation / 180 * Math.PI), pos.y - length / 8 * (float) Math.cos(orientation / 180 * Math.PI) + width / 2 * (float) Math.sin(orientation / 180 * Math.PI));
		landPoints[1] = new Position(pos.x + width / 2 * (float) Math.sin(orientation / 180 * Math.PI) + 2 * length / 8 * (float) Math.sin(orientation / 180 * Math.PI), pos.y -  2 * length / 8 * (float) Math.cos(orientation / 180 * Math.PI) + width / 2 * (float) Math.sin(orientation / 180 * Math.PI));
		landPoints[2] = new Position(pos.x + width / 2 * (float) Math.sin(orientation / 180 * Math.PI) + 3 * length / 8 * (float) Math.sin(orientation / 180 * Math.PI), pos.y - 3 * length / 8 * (float) Math.cos(orientation / 180 * Math.PI) + width / 2 * (float) Math.sin(orientation / 180 * Math.PI));

		// sets up image file
		runwayImageFile = "runway.png";

		try {
			runwayImage = ImageIO.read(new File(runwayImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", runwayImageFile);
		}


	}

	@Override
	public Position getPosition() {
		Position copyPos = new Position(pos.x, pos.y);
		return copyPos;
	}

	@Override
	public float getRotation() {
		// TODO Auto-generated method stub
		return orientation;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate((int) pos.x, (int) pos.y);
		g.rotate((orientation - 90) / 180 * Math.PI);
		g.setColor(Color.black);
		g.drawImage(runwayImage, 0, (int) (-width/2 + width / 2 * 
				(float) Math.sin(orientation / 180 * Math.PI)), 
				length, width, null);
		g.rotate(-(orientation -90) / 180 * Math.PI);
		g.translate((int) -pos.x, (int) -pos.y);
	}

	public boolean landZone (Planes checkPlane) {

		// checks in the plane is in the landing zone
		// the zone is calculated based on linear parametric bounds that are
		// calculated dynamics based on runway parameters (no hard coding!)
		// the zone of landing is roughly a trapezium with the narrow end at
		// the beginning of the runway
		Position pos = checkPlane.getPosition();

		// these are the parallel lines of the trapezium
		float planeParallel = pos.y - pos.x * 
				(float) Math.tan(orientation / 180 * Math.PI);
		float parallel1 = this.pos.y - this.pos.x * 
				(float) Math.tan(orientation / 180 * Math.PI);
		float parallel2 = (this.pos.y + 
				(float) Math.sin(orientation / 180 * Math.PI) * distanceOut) - 
				(this.pos.x - (float) Math.cos(orientation / 180 * Math.PI) * 
						distanceOut) * (float) Math.tan(orientation / 180 * 
								Math.PI);

		// these are the non-parallel lines of the trapezium
		float augOrt = (float) Math.atan(((mouth - back) / 2) / 
				(double) distanceOut);
		float planeAngled1 = pos.y + pos.x / (float) Math.tan(orientation /
				180 * Math.PI + augOrt) ;
		float angled1 = (this.pos.y - back / 2  * 
				(float) Math.sin(orientation)) + (this.pos.x - back / 2 * 
						(float) Math.cos(orientation)) / 
						(float) Math.tan(orientation / 180 * Math.PI + augOrt);
		float planeAngled2 = pos.y + pos.x / (float) Math.tan(orientation / 
				180 * Math.PI - augOrt);
		float angled2 = (this.pos.y + back / 2  * 
				(float) Math.sin(orientation)) + (this.pos.x + back / 2 * 
						(float) Math.cos(orientation)) / 
						(float) Math.tan(orientation / 180 * Math.PI 
								- augOrt) + 20;

		// checks if in bounds
		if (planeParallel >= parallel1 && planeParallel <= parallel2 && 
				planeAngled1 >= angled1 && planeAngled2 <= angled2) {
			// allows for a tolerance of orientation
			if (Math.abs(checkPlane.getRotation() - 
					this.orientation) <= orientTolerance) {
				return true;
			}
		}
		return false;

	}

	public Position[] waypoints() {
		return landPoints.clone();
	}
}
