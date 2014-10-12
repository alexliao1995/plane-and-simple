import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.TreeSet;

import javax.imageio.ImageIO;


public class Planes implements GameObject, Comparable<Planes> {

	// Game variables
	private static int numPlanes = 0;
	private final int PLANE_ID;
	private final int COURT_WIDTH;
	private final int COURT_HEIGHT;


	// Image files
	private String planeImageFile;
	private BufferedImage planeImage;
	private String planeLandImageFile;
	private BufferedImage planeLandImage;
	private String markerImageFile;
	private BufferedImage markerImage;
	private String warnImageFile;
	private BufferedImage warnImage;


	// Airplane properties
	private final float ENGINE_COEF = 20f;
	private final float AERO_COEF = 0.5f;
	private final float MASS = 1;
	private float squareSize;
	private boolean focused;
	private Position pos;

	// Velocity-related variables
	private float velocity;
	private float throttle;
	private final float THRO_INCR = 0.01f;


	// Roll-related variables
	private float bankAngle;
	private float orientation;
	private boolean bankDecay;
	private final float BANK_INCREMENT = 1;
	private final float BANK_LIMIT = 40;
	private final float BANK_DECAY_CONSTANT = 0.85f;

	// Game Edge Detection 
	private boolean reorientForce;
	private int reorientState;
	private float reorientTarget;
	private final int REORIENT_ENTRY = -2;
	private final int REORIENT_NORMAL = -1;
	private final int REORIENT_LEFT = 1;
	private final int REORIENT_RIGHT = 2;
	private final int REORIENT_TOP = 3;
	private final int REORIENT_BOTTOM = 4;
	private final int REORIENT_LEFT_TOP = 5;
	private final int REORIENT_LEFT_BOTTOM = 6;
	private final int REORIENT_RIGHT_TOP = 7;
	private final int REORIENT_RIGHT_BOTTOM = 8;

	// Modifiers
	private boolean warning;
	private boolean collide;
	private ArrayList<String> msg;
	private boolean landing;
	private float landingOrt;
	private Position[] waypoints;
	private float target;
	private int landingState;
	private boolean landed;

	// Timestep Scaling
	private final float TIMESTEP_SCALE;

	public Planes(int timeStep, int COURT_WIDTH, int COURT_HEIGHT) {

		// Random spawning from the sides, suppresses edge detection
		int whereToSpawn = (int) (Math.random() * 8);

		switch (whereToSpawn) {
		case 0: pos = new Position (-30, (float) (Math.random() * 
				COURT_HEIGHT / 4 + COURT_HEIGHT /8 * 5));
		orientation = (float) (Math.random() - 0.5) * 80 + 90;
		break;
		case 1: pos = new Position (-30, (float) (Math.random() * 
				COURT_HEIGHT / 4 + COURT_HEIGHT /8));
		orientation = (float) (Math.random() - 0.5) * 80 + 90;
		break;
		case 2: pos = new Position ((float) (Math.random() * 
				COURT_WIDTH / 4 + COURT_WIDTH /8), -30);
		orientation = (float) (Math.random() - 0.5) * 80 + 180;
		break;
		case 3: pos = new Position ((float) (Math.random() * 
				COURT_WIDTH / 4 + COURT_WIDTH /8 * 5), -30);
		orientation = (float) (Math.random() - 0.5) * 80 + 180;
		break;
		case 4: pos = new Position (COURT_WIDTH + 30, (float) (Math.random() * 
				COURT_HEIGHT / 4 + COURT_HEIGHT /8));
		orientation = (float) (Math.random() - 0.5) * 80 + 270;
		break;
		case 5: pos = new Position (COURT_WIDTH + 30, (float) (Math.random() * 
				COURT_HEIGHT / 4 + COURT_HEIGHT /8 * 5));
		orientation = (float) (Math.random() - 0.5) * 80 + 270;
		break;
		case 6: pos = new Position ((float) (Math.random() * COURT_WIDTH / 4 + 
				COURT_WIDTH /8 * 5), COURT_HEIGHT + 30);
		orientation = (float) (Math.random() - 0.5) * 80;
		break;
		case 7: pos = new Position ((float) (Math.random() * COURT_WIDTH / 4 + 
				COURT_WIDTH /8 ), COURT_HEIGHT + 30);
		orientation = (float) (Math.random() - 0.5) * 80;
		break;
		}


		// setting up parameters
		velocity = 6;
		throttle = 0.5f;
		bankDecay = false;
		PLANE_ID = numPlanes; 
		numPlanes++;
		TIMESTEP_SCALE = 0.001f * timeStep;
		this.COURT_WIDTH = COURT_WIDTH;
		this.COURT_HEIGHT = COURT_HEIGHT;
		reorientState = REORIENT_ENTRY;
		reorientForce = true;
		warning = false;
		collide = false;
		msg = new ArrayList<String>();
		landing = false;
		landed = false;
		squareSize = 30;
		focused = false;

		// setting up image files
		planeImageFile = "plane-100red.png";
		planeLandImageFile = "plane-100.png";
		markerImageFile = "marker.png";
		warnImageFile = "warn.png";

		try {
			planeImage = ImageIO.read(new File(planeImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", 
					planeImageFile);
		}

		try {
			planeLandImage = ImageIO.read(new File(planeLandImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", 
					planeLandImageFile);
		}

		try {
			markerImage = ImageIO.read(new File(markerImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", 
					markerImageFile);
		}
		try {
			warnImage = ImageIO.read(new File(warnImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", 
					warnImageFile);
		}
	}


	@Override
	public Position getPosition() {
		return new Position(pos.x, pos.y);
	}

	@Override
	public void draw(Graphics2D g) {

		// translates graphics
		g.translate((int) pos.x, (int) pos.y);
		g.rotate((orientation - 90) / 180 * Math.PI);

		// show focus
		if (focused) {
			g.drawImage(markerImage, -25, -25, 50, 50, null);
		}

		// show collision
		if (collide) { 
			g.setColor(Color.red);
			g.fillOval(-15, -15, 30, 30);
			g.setColor(Color.black);
		}

		// show warning circle for proximity
		if (warning) {

			g.drawImage(warnImage, -38, -38, 75, 75, null);

		}

		// changes the plane image and size to indicate landing
		if (landing) {
			g.drawImage(planeLandImage, -15, -15, (int) squareSize, 
					(int) squareSize, null);
		} else{
			g.drawImage(planeImage, -15, -15, (int) squareSize, 
					(int) squareSize, null);
		}

		g.rotate(-(orientation -90) / 180 * Math.PI);
		g.translate((int) -pos.x, (int) -pos.y);

	}

	// detects for collisions and proximity
	public void collide(TreeSet<Planes> planeSet) {

		// disables in the plane is landing
		if (landing) {
			return;
		}

		Iterator<Planes> planeSetIter = planeSet.iterator();

		warning = false;
		collide = false;

		while (planeSetIter.hasNext()) {
			Planes nextPlane = planeSetIter.next();
			if (nextPlane == this) {
				continue;
			}
			float distance = Position.distance(this.pos, 
					nextPlane.getPosition());

			if (distance < 25 && !nextPlane.isLanding()) {
				collide = true;
			} else if (distance < 75 && !nextPlane.isLanding()) {
				warning = true;
			}
		}
	}

	// calculates the acceleration physics
	private float accelerate() {
		if (this.velocity <= 10 && this.velocity >= 2) {
			if (((this.velocity - 2f) / 8f) <  this.throttle) {
				return 4f * TIMESTEP_SCALE;
			}
			if (((this.velocity - 2f) / 8f) >  this.throttle) {
				return -4f * TIMESTEP_SCALE;
			}
		}
		if (this.velocity >= 1.5 && this.velocity <= 10) {
			return 4f * TIMESTEP_SCALE;
		}
		if (this.velocity >= 10) {
			return -4f * TIMESTEP_SCALE;
		}
		return 0f;
	}


	// Real-er physics
	//	private float accelerate(float timeStep) {
	//		float force = (throttle * ENGINE_COEF) - (AERO_COEF * 
	// 		(float) Math.pow(velocity,2));
	//		float acceleration = force / MASS;
	//		return acceleration * TIMESTEP_SCALE;
	//	}


	// calculates turning of the aero plane
	private void roll() {

		// orientation change is buffered by bank angle for realism
		orientation += bankAngle * 10 * TIMESTEP_SCALE;

		// bank angle decays realistically to stimulate the plane rolling
		// back to upright
		if (bankDecay) {
			bankAngle = bankAngle * BANK_DECAY_CONSTANT;
			if (Math.abs(bankAngle) < 1) {
				bankAngle = 0;
				bankDecay = false;
			}
		}
	}

	// suppresses normal edge detection during entry into the game space
	private void entry() {
		if ((pos.x >= 25 && pos.y >= 25 && pos.x <= COURT_WIDTH - 25 &&
				pos.y <= COURT_HEIGHT - 25)) {
			reorientState = REORIENT_NORMAL;
			reorientForce = false;
		}
	}

	// corrects the angle of the plane according to the calculated angles
	private void correct() {
		orientation += (reorientTarget - orientation) / 2 * 10 * TIMESTEP_SCALE;

		if (Math.abs(reorientTarget - orientation) < 1) {
			reorientState = REORIENT_NORMAL;
			reorientForce = false;
		}
	}

	// detects when the plane is at the edge and implements corrective action
	private void edgeDetect() {

		// corrects if the plane is out of bounds but not forced in action
		if ((pos.x < 20 || pos.y < 20 || pos.x > COURT_WIDTH - 20 || 
				pos.y > COURT_HEIGHT - 20) && reorientForce == false) {

			// first four are the corner cases for the corners
			// forces plane to reorient in a singular orientation
			if (pos.x < 20 && pos.y < 20) {
				orientation = ((orientation % 360) + 360) % 360;
				reorientTarget = 135;
				reorientState = REORIENT_LEFT_TOP;
				reorientForce = true;

			} else if (pos.x < 20 && pos.y > COURT_HEIGHT - 20) {
				orientation = ((orientation % 360) + 360) % 360;
				reorientTarget = 45;
				reorientState = REORIENT_LEFT_BOTTOM;
				reorientForce = true;

			} else if (pos.x > COURT_WIDTH - 20 && pos.y < 20) {
				orientation = ((orientation % 360) + 360) % 360;
				reorientTarget = 225;
				reorientState = REORIENT_RIGHT_TOP;
				reorientForce = true;

			} else if (pos.x > COURT_WIDTH - 20 && pos.y > COURT_HEIGHT - 20) {
				orientation = ((orientation % 360) + 360) % 360;
				reorientTarget = 315;
				reorientState = REORIENT_RIGHT_BOTTOM;
				reorientForce = true;

			} else {

				// rest of the cases when plane exceed edge on any side
				if (pos.x < 20 && reorientState != REORIENT_LEFT) {
					orientation = ((orientation % 360) + 360) % 360;
					if (orientation >= 270) {
						if (orientation <= 350) {
							reorientTarget = (360 - orientation) * 2 + 
									orientation;
						} else {
							reorientTarget = orientation + 20;
						}
					} else {
						if (orientation >= 190) {
							reorientTarget = orientation - 
									(orientation - 180) * 2;
						} else {
							reorientTarget = orientation - 20;
						}
					}
					reorientState = REORIENT_LEFT;
				} else if (pos.x > COURT_WIDTH - 20 && reorientState != 
						REORIENT_RIGHT) {
					orientation = ((orientation % 360) + 360) % 360;
					if (orientation >= 90) {
						if (orientation <= 170) {
							reorientTarget = (180 - orientation) * 2 + 
									orientation;
						} else {
							reorientTarget = orientation + 20;
						}
					} else {
						if (orientation >= 10) {
							reorientTarget = orientation - orientation * 2;
						} else {
							reorientTarget = orientation - 20;
						}
					}
					reorientState = REORIENT_RIGHT;
				} else if (pos.y < 20 && reorientState != REORIENT_TOP) {
					orientation = ((orientation % 360) + 360) % 360;
					if (orientation >= 270) {
						if (orientation >= 280) {
							reorientTarget = orientation - 
									(orientation - 270) * 2;
						} else {
							reorientTarget = orientation - 20;
						}
					} else {
						if (orientation <= 80) {
							reorientTarget = (90 - orientation) * 2 + 
									orientation;
						} else {
							reorientTarget = orientation + 20;
						}
					}
					reorientState = REORIENT_TOP;
				} else if (pos.y > COURT_HEIGHT - 20 && reorientState != 
						REORIENT_BOTTOM){
					orientation = ((orientation % 360) + 360) % 360;
					if (orientation <= 180) {
						if (orientation >= 100) {
							reorientTarget = orientation - 
									(orientation - 90) * 2;
						} else {
							reorientTarget = orientation - 20;
						}
					} else {
						if (orientation <= 260) {
							reorientTarget = (270 - orientation) * 2 + 
									orientation;
						} else {
							reorientTarget = orientation + 20;
						}
					}
					reorientState = REORIENT_BOTTOM;
				}
			}
		}
	}

	private void correctLand() {

		// if velocity has decayed sufficiently, count as landed
		if (velocity <= 0.7) {
			landed = true;
		}

		// calculates the markings necessary to see if the plane has crossed
		// the waypoints
		float planeParallel = pos.y - pos.x * (float) Math.tan(landingOrt / 
				180 * Math.PI);
		float parallel = waypoints[landingState].y - waypoints[landingState].x 
				* (float) Math.tan(landingOrt / 180 * Math.PI);

		// checks if the plane has crossed the waypoints
		// orients the plane to correct its orientation to match center line
		if (planeParallel <= parallel) {
			if(landingState < 2) {
				landingState++;
				target = Position.orientation(waypoints[landingState], pos);
			} else {
				target = reorientTarget;
			}
		}

		// animate turning
		orientation = ((orientation % 360) + 360) % 360;
		orientation += (target - orientation) / 5 * 10 * TIMESTEP_SCALE;

		// decay velocity
		velocity = velocity * (0.995f - (landingState * 0.005f));

		// decay size of plane to simulate altitude
		if (squareSize >= 20) {
			squareSize = squareSize * 0.995f;
		}

		// advance position
		pos.x += (float) Math.sin(orientation / 180 * Math.PI) * velocity *
				10 * TIMESTEP_SCALE;
		pos.y += (- (float) Math.cos(orientation / 180 * Math.PI)) * velocity *
				10 * TIMESTEP_SCALE;
	}

	// commands the plane to land, provides waypoints
	public void land(float orientation, Position[] waypoints) {
		landing = true;
		reorientTarget = orientation;
		this.waypoints = waypoints;
		this.landingState = 0;
		target = Position.orientation(this.waypoints[0], pos);

	}

	// function for movement
	public void move() {

		// uses land movement function when landing
		if (landing) {
			correctLand();
			return;
		}

		// detects edge collision
		this.edgeDetect();

		// executes different orientation functions depending on the scenario
		if (reorientState == REORIENT_NORMAL) {
			roll();
		} else if (reorientState == REORIENT_ENTRY) {
			entry();
		} else {
			correct();
		}

		// does acceleration, velocity, position calculations
		float acceleration = accelerate();
		velocity += acceleration;
		pos.x += (float) Math.sin(orientation / 180 * Math.PI) * velocity * 10 * TIMESTEP_SCALE;
		pos.y += (- (float) Math.cos(orientation / 180 * Math.PI)) * velocity * 10 * TIMESTEP_SCALE;
	}

	// methods to increase and decrease throttle
	public void incrThrottle() {
		if (throttle + THRO_INCR <= 1) {
			throttle += THRO_INCR;
		}
	}

	public void decrThrottle() {
		if (throttle - THRO_INCR >= 0) {
			throttle -= THRO_INCR;
		}
	}

	// provides a "sticky" roll function 
	public void leftRoll() {
		bankDecay = false;
		if (bankAngle - BANK_INCREMENT >= -BANK_LIMIT) {
			bankAngle -= BANK_INCREMENT;
		}
	}

	// provides a "sticky" roll function 
	public void rightRoll() {
		bankDecay = false;
		if (bankAngle + BANK_INCREMENT <= BANK_LIMIT) {
			bankAngle += BANK_INCREMENT;
		}
	}

	// decays the bank angle
	public void resetBank() {
		bankDecay = true;
	}

	@Override
	public float getRotation() {
		orientation = ((orientation % 360) + 360) % 360;
		return orientation;

	}

	// for serializing
	public int getID() {
		return this.PLANE_ID;
	}

	// implements the comparable interface for use in TreeSet
	@Override
	public int compareTo(Planes arg0) {
		if (arg0.getID() < this.PLANE_ID) {
			return -1;
		}
		else if (arg0.getID() == this.PLANE_ID) {
			return 0;
		}
		else {
			return 1;
		}
	}

	// returns booleans needed to check state of plane
	public boolean isLanding() {
		return landing;
	}

	public boolean isLanded() {
		return landed;
	}

	public void focused(boolean val) {
		focused = val;
	}

	public boolean collided() {
		return collide;
	}

}
