import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.*;

/**
 * 
 */

/**
 * @author alexliao
 *
 */
public class Game implements Runnable {

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		final JFrame environ = new JFrame("Plane n' Simple");
		environ.setResizable(false);
		final GameSpace court = new GameSpace();
		environ.add(court, BorderLayout.CENTER);
		
		environ.setLocation(1000, 1000);
		
		environ.pack();
		environ.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		environ.setVisible(true);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Game());
	}

}
