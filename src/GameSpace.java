import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.*;


public class GameSpace extends JPanel {


	private String menuImageFile;
	private BufferedImage menuImage;
	private String instrImageFile;
	private BufferedImage instrImage;
	private String bgImageFile;
	private BufferedImage bgImage;
	private String goImageFile;
	private BufferedImage goImage;
	private Planes focus;
	private Runway runway;
	private TreeSet<Planes> planeSet = new TreeSet<Planes>();
	private HashMap<Integer, Boolean> keyPressed;

	public static final int COURT_WIDTH = 1000;
	public static final int COURT_HEIGHT = 600;
	public static final int SQUARE_VELOCITY = 4;
	public static final int INTERVAL = 30;
	public int counter = 1;
	public int spawnRate = 200;
	public int score = 0;
	public int mode;
	public final int MODE_MENU = 0;
	public final int MODE_INST = 1;
	public final int MODE_GAME = 2;
	public final int MODE_GAMEOVER = 3;
	public int gameOverCount = 0;

	public GameSpace() {

		setFocusable(true);
		
		
		// set up game mode
		mode = MODE_MENU;

		// setup images 
		bgImageFile = "background.png";
		instrImageFile = "instr.png";
		menuImageFile = "menu.png";
		goImageFile = "gameover.png";

		try {
			bgImage = ImageIO.read(new File(bgImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", bgImageFile);
		}
		try {
			instrImage = ImageIO.read(new File(instrImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", instrImageFile);
		}
		try {
			menuImage = ImageIO.read(new File(menuImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", menuImageFile);
		}
		try {
			goImage = ImageIO.read(new File(goImageFile));
		} catch (IOException e) {
			throw new MissingResourceException("Missing file", "Image", goImageFile);
		}

		
		// sets up initial planes
		Planes test = new Planes(INTERVAL, COURT_WIDTH, COURT_HEIGHT);
		planeSet.add(test);
		focus = test;
		focus.focused(true);
		
		runway = new Runway();
		
		// sets up game loop
		Timer timer = new Timer(INTERVAL, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tick();
			}
		});
		timer.start(); 
		
		// stores keypress events in a hashmap
		keyPressed = new HashMap<Integer, Boolean>();
		keyPressed.put(KeyEvent.VK_LEFT, false);
		keyPressed.put(KeyEvent.VK_RIGHT, false);
		keyPressed.put(KeyEvent.VK_DOWN, false);
		keyPressed.put(KeyEvent.VK_UP, false);
		keyPressed.put(KeyEvent.VK_A, false);
		keyPressed.put(KeyEvent.VK_D, false);
		keyPressed.put(KeyEvent.VK_S, false);
		keyPressed.put(KeyEvent.VK_W, false);
		keyPressed.put(KeyEvent.VK_SPACE, false);

		// mouse listener for buttons and targetting
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				
				// sets up button event for main menu
				if (mode == MODE_MENU) {
					if (x >= 540 && x <= 935 && y >= 329 && y <= 483) {
						mode = MODE_GAME;
						return;
					}
					if (x >= 540 && x <= 935 && y >= 499 && y <= 577) {
						mode = MODE_INST;
						return;
					}
				}
				
				// sets up instructions screen
				if (mode == MODE_INST) {
					if (x >= 631 && x <= 924 && y >= 463 && y <= 578) {
						mode = MODE_MENU;
						return;
					}
				}
				
				// sets up targeting function for the game
				if (mode == MODE_GAME) {
					
					Planes head = planeSet.first();
					Position pos = new Position (x,y);
					Planes target = null;
					float distance = Position.distance(pos, head.getPosition());
					
					// checks for lowest distance
					while (head != null) {
						if (head.isLanded() || head.isLanding()) {
							head = planeSet.higher(head);
							continue;
						}

						float newDist = Position.distance(pos, head.getPosition());
						if (newDist <= distance) {
							distance = newDist;
							target = head;
						}
						head = planeSet.higher(head);
					}
					
					// if distance is less than 40, target
					if (distance <= 40) {
						focus.focused(false);
						focus = target;
						focus.focused(true);
					}
				}
				
				// sets up buttons for game over screen
				if (mode == MODE_GAMEOVER) {
					if (gameOverCount >= 40) {
						if (x >= 178 && x <= 378 && y >= 330 && y <= 399) {
							mode = MODE_MENU;
							reset();
							gameOverCount = 0;
							return;
						}
						if (x >= 178 && x <= 378 && y >= 415 && y <= 490) {
							mode = MODE_GAME;
							reset();
							gameOverCount = 0;
							return;
						}
					}
				}

			}
		});


		// sets up key listener
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT :
					keyPressed.put(KeyEvent.VK_LEFT, true);
					break;
				case KeyEvent.VK_RIGHT :
					keyPressed.put(KeyEvent.VK_RIGHT, true);
					break;
				case KeyEvent.VK_DOWN :
					keyPressed.put(KeyEvent.VK_DOWN, true);
					break;
				case KeyEvent.VK_UP:
					keyPressed.put(KeyEvent.VK_UP, true);
					break;
				case KeyEvent.VK_W :
					keyPressed.put(KeyEvent.VK_W, true);
					break;
				case KeyEvent.VK_A :
					keyPressed.put(KeyEvent.VK_A, true);
					break;
				case KeyEvent.VK_S :
					keyPressed.put(KeyEvent.VK_S, true);
					break;
				case KeyEvent.VK_D:
					keyPressed.put(KeyEvent.VK_D, true);
					break;
				case KeyEvent.VK_SPACE:
					
					// sets up re-focusing for space bar
					if (keyPressed.get(KeyEvent.VK_SPACE) == false) {
						focus.resetBank();
						focus.focused(false);
						Planes next = planeSet.higher(focus);


						while (next != null) {
							if (next.isLanding() || next.isLanded()) {
								next = planeSet.higher(next);
							} else {
								break;
							}
						}

						if (next == null) {
							next = planeSet.first();
						}

						while (next != null) {
							if (next.isLanding() || next.isLanded()) {
								next = planeSet.higher(next);
							} else {
								break;
							}
						}

						focus = next;
						focus.focused(true);
					}
					keyPressed.put(KeyEvent.VK_SPACE, true);
					break;
				}
			}

			public void keyReleased(KeyEvent e) {
				// begin bank decay
				focus.resetBank();

				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT :
					keyPressed.put(KeyEvent.VK_LEFT, false);
					break;
				case KeyEvent.VK_RIGHT :
					keyPressed.put(KeyEvent.VK_RIGHT, false);
					break;
				case KeyEvent.VK_DOWN :
					keyPressed.put(KeyEvent.VK_DOWN, false);
					break;
				case KeyEvent.VK_UP:
					keyPressed.put(KeyEvent.VK_UP, false);
					break;
				case KeyEvent.VK_W :
					keyPressed.put(KeyEvent.VK_W, false);
					break;
				case KeyEvent.VK_A :
					keyPressed.put(KeyEvent.VK_A, false);
					break;
				case KeyEvent.VK_S :
					keyPressed.put(KeyEvent.VK_S, false);
					break;
				case KeyEvent.VK_D:
					keyPressed.put(KeyEvent.VK_D, false);
					break;
				case KeyEvent.VK_SPACE:
					keyPressed.put(KeyEvent.VK_SPACE, false);
					break;
				}
			}
		});

		//		this.status = status;
	}

	void tick() {
		
		// sets up animation timer for game over screen
		if (mode == MODE_GAMEOVER) {
			gameOverCount++;
		}

		if (mode == MODE_GAME) {
			
			// spawns a new plane
			if (counter % spawnRate == 0) {
				planeSet.add(new Planes(INTERVAL, COURT_WIDTH, COURT_HEIGHT));
			}

			// executes functions if buttons are pressed
			if (keyPressed.get(KeyEvent.VK_LEFT) || keyPressed.get(KeyEvent.VK_A)) {
				focus.leftRoll();
			}
			if (keyPressed.get(KeyEvent.VK_RIGHT) || keyPressed.get(KeyEvent.VK_D)) {
				focus.rightRoll();
			}
			if (keyPressed.get(KeyEvent.VK_UP) || keyPressed.get(KeyEvent.VK_W)) {
				focus.incrThrottle();
			}
			if (keyPressed.get(KeyEvent.VK_DOWN) || keyPressed.get(KeyEvent.VK_S)) {
				focus.decrThrottle();
			}
			
			// checks all the planes for collision and landing
			Planes planeIter = planeSet.first();
			while (planeIter != null) {
				
				// collision detection
				planeIter.collide(planeSet);
				
				// landing detection
				if (runway.landZone(planeIter)) {
					planeIter.focused(false);
					planeIter.land(runway.getRotation(), runway.waypoints());
					
					// passing off control of the plane, auto refocusing
					if (focus == planeIter) {
						Planes next = planeSet.higher(planeIter);

						while (next != null) {
							if (next.isLanding()) {
								next = planeSet.higher(next);
							} else {
								break;
							}
						}

						if (next == null) {
							next = planeSet.first();
						}

						while (next != null) {
							if (next.isLanding()) {
								next = planeSet.higher(next);
							} else {
								break;
							}
						}

						if (next == planeIter || next == null) {
							next = new Planes(INTERVAL, COURT_WIDTH, COURT_HEIGHT);
							planeSet.add(next);
						}

						focus = next;
						focus.focused(true);
					}
				}
				planeIter = planeSet.higher(planeIter);
			}
			
			// checks for game changing statuses
			Planes planeHead = planeSet.first();
			while (planeHead != null) {
				
				// ends game if collision is detected
				if (planeHead.collided()) {
					mode = MODE_GAMEOVER;
				}
				
				// removes plane if it has landed
				if (planeHead.isLanded()) {
					score++;
					Planes delete = planeHead;
					planeHead = planeSet.higher(planeHead);
					planeSet.remove(delete);
					continue;
				}
				
				// moves each plane
				planeHead.move();
				planeHead = planeSet.higher(planeHead);
			}
			
			// increases counter and increases spawn rate with score
			counter++;
			if (spawnRate >= 60) {
				spawnRate = 200 - (score / 10) * 10;
			}
			// update the display


		}
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g);

		if (mode == MODE_MENU) {
			g.drawImage(menuImage, 0, 0, 1000, 600, null);
		}

		if (mode == MODE_INST) {
			g.drawImage(instrImage, 0, 0, 1000, 600, null);
		}

		if (mode == MODE_GAME || mode == MODE_GAMEOVER) {
			g.drawImage(bgImage, 0, 0, 1000, 600, null);
			runway.draw(g2d);
			g.setColor(Color.white);
			g.setFont(new Font("SansSerif", 0, 30));
			g.drawString(Integer.toString(score / 100), 496, 64);
			g.drawString(Integer.toString((score / 10) % 10), 530, 64);
			g.drawString(Integer.toString(score % 10), 564, 64);
			g.setColor(Color.black);
			Iterator<Planes> planeIterator = planeSet.iterator();
			while (planeIterator.hasNext()) {
				planeIterator.next().draw(g2d);
			}
		}
		
		// sets up the game over overlay 
		if (mode == MODE_GAMEOVER) {
			
			// fades in the gameover overlay
			float opacity;
			if (gameOverCount >= 25) {
				opacity = (gameOverCount - 25) / 25f;
			} else {
				opacity = 0;
			}

			// sets up the opacity based on a time delay
			float[] scale = { 1f, 1f, 1f, opacity };
			float[] offset = new float[4];
			RescaleOp opa = new RescaleOp(scale, offset, null);
			g2d.drawImage(goImage, opa, 150, 100);

			// staggers the numbers for taunting effect
			if (gameOverCount >= 40 && score >= 100) {
				g.setFont(new Font("SansSerif", 0, 35));
				g.drawString(Integer.toString(score / 100), 550, 410);
			}

			if (gameOverCount >= 40 && score >= 10) {
				g.setFont(new Font("SansSerif", 0, 35));
				g.drawString(Integer.toString((score / 10) % 10), 570, 410);
			}
			if (gameOverCount >= 40) {
				g.setFont(new Font("SansSerif", 0, 35));
				g.drawString(Integer.toString(score % 10), 590, 410);
			}

			if (gameOverCount >= 40) {
				g.setFont(new Font("SansSerif", 0, 35));
				g.drawString(Integer.toString(score - 1), 735, 452);
			}
		}

	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(COURT_WIDTH, COURT_HEIGHT);
	}

	public void reset() {
		
		// resets the game to play again 
		planeSet.clear();
		Planes test = new Planes(INTERVAL, COURT_WIDTH, COURT_HEIGHT);
		planeSet.add(test);
		focus = test;
		test.focused(true);
		counter = 1;
		score = 0;
	}

}
