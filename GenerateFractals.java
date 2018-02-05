import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

// Responsible for displaying the image
@SuppressWarnings("serial")
public abstract class GenerateFractals extends JPanel {

	// Default values for the boundaries
	final static double DEFAULT_MAXX = 2.0;
	final static double DEFAULT_MINX = -2.0;
	final static double DEFAULT_MAXY = 1.6;
	final static double DEFAULT_MINY = -1.6;

	// x, y coordinate ranges of displayed area
	protected static double maxX = DEFAULT_MAXX;
	protected static double minX = DEFAULT_MINX;
	protected static double maxY = DEFAULT_MAXY;
	protected static double minY = DEFAULT_MINY;

	// Number of times testing for convergence
	protected static int MAX_ITERATIONS = 100;

	// Generate the Mandlebrot set
	protected void computeMandel(int width, int height) {
		// Go through every pixel
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Visit each point
				checkPoint(coordToComplex(x, y, width, height), x, y);
			}
		}
	}

	// Converts the coordinates to its complex number equivalent
	protected static Complex coordToComplex(int x, int y, int width, int height) {
		// Mapping points to complex number
		double a = minX + x * (maxX - minX) / width;
		double b = minY + y * (maxY - minY) / height;

		return new Complex(a, b);
	}

	// Goes through each point
	protected abstract void checkPoint(Complex c, int x, int y);

	// Colours the point according to if it belongs in the Mandlebrot set
	protected int colourPoint(int iterations, Complex c) {
		int colour;

		// Sets the colour of the pixel
		if (iterations == MAX_ITERATIONS) {
			colour = Color.BLACK.getRGB();
		} else {
			// Smooths the colouring
			float smoothColour = (float) (iterations + 5 - Math.log(Math.log(c.modulus())) / Math.log(2))
					/ MAX_ITERATIONS;

			colour = Color.HSBtoRGB((float) (0.9 * smoothColour), 0.9f, 0.9f);
		}
		return colour;
	}
}

@SuppressWarnings("serial")
// This is used to generate the Mandlebrot set and the Burning Ship fractal
class GenerateMandlebrotBurningShip extends GenerateFractals {

	private BufferedImage bufferedImage;

	// Constant screen dimensions
	final static int MBBS_WIDTH = 900;
	final static int MBBS_HEIGHT = 700;

	protected GenerateMandlebrotBurningShip() {
		bufferedImage = new BufferedImage(MBBS_WIDTH, MBBS_HEIGHT, BufferedImage.TYPE_INT_RGB);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		computeMandel(MBBS_WIDTH, MBBS_HEIGHT);
		g.drawImage(bufferedImage, 0, 0, this);

		// Draw the rectangle if the mouse is being dragged
		if (FEFrame.startPosition != null && FEFrame.endPosition != null) {
			g2.setPaint(Color.LIGHT_GRAY);
			Shape r = FEFrame.makeRectangle(FEFrame.startPosition.x, FEFrame.startPosition.y, FEFrame.endPosition.x,
					FEFrame.endPosition.y);
			g2.draw(r);
		}

		// Checks if dragging has just ended and the user hasn't just clicked
		// once (no drag)
		if (FEFrame.dragEnded && (FEFrame.end.x - FEFrame.start.x != 0 || FEFrame.end.y - FEFrame.start.y != 0)) {

			FEFrame.dragEnded = false;
			
			// Set maximum and minimum points of the rectangle
			Complex min = null;
			Complex max = null;

			// This checks the direction in which the rectangle was
			// drawn
			// and selects points accordingly
			if (FEFrame.end.y - FEFrame.start.y > 0) {
				if (FEFrame.end.x - FEFrame.start.x > 0) {
					// End is bottom right of start
					min = coordToComplex(FEFrame.start.x, FEFrame.start.y, MBBS_WIDTH, MBBS_HEIGHT);
					max = coordToComplex(FEFrame.end.x, FEFrame.end.y, MBBS_WIDTH, MBBS_HEIGHT);
				} else {
					// End is bottom left of start
					min = coordToComplex(FEFrame.end.x, FEFrame.start.y, MBBS_WIDTH, MBBS_HEIGHT);
					max = coordToComplex(FEFrame.start.x, FEFrame.end.y, MBBS_WIDTH, MBBS_HEIGHT);
				}
			} else {
				if (FEFrame.end.x - FEFrame.start.x > 0) {
					// End is top right of start
					min = coordToComplex(FEFrame.start.x, FEFrame.end.y, MBBS_WIDTH, MBBS_HEIGHT);
					max = coordToComplex(FEFrame.end.x, FEFrame.start.y, MBBS_WIDTH, MBBS_HEIGHT);
				} else {
					// End is top left of start
					min = coordToComplex(FEFrame.end.x, FEFrame.end.y, MBBS_WIDTH, MBBS_HEIGHT);
					max = coordToComplex(FEFrame.start.x, FEFrame.start.y, MBBS_WIDTH, MBBS_HEIGHT);
				}
			}
			// //Assigning new bounds
			minX = min.getReal();
			maxX = max.getReal();
			minY = min.getImaginary();
			maxY = max.getImaginary();

			// Updating the text fields to match the new bounds
			FEFrame.minXText.setText("" + (double) Math.round(minX * 10000) / 10000);
			FEFrame.maxXText.setText("" + (double) Math.round(maxX * 10000) / 10000);
			FEFrame.minYText.setText("" + (double) Math.round(minY * 10000) / 10000);
			FEFrame.maxYText.setText("" + (double) Math.round(maxY * 10000) / 10000);

			repaint();
		}
	}

	protected void init() {
		this.setPreferredSize(new Dimension(MBBS_WIDTH, MBBS_HEIGHT));
	}

	// Returns the number of iterations that each point takes
	protected void checkPoint(Complex c, int x, int y) {
		int iterations = 0;
		Complex c2 = null;
		
		// If normal mandlebrot is selected
		if (!FEFrame.burningShip.isSelected()) {
			c2 = c;

			// Check if it goes to infinity
			while (iterations < MAX_ITERATIONS && c2.modulusSquared() <= 4) {
				// (zn) = (zn-1)^2 + c
				c2 = c2.square().add(c);
				iterations++;
			}
		} else {
			double zreal = 0;
			double zimaginary = 0;

			while (iterations < MAX_ITERATIONS && zreal * zreal + zimaginary * zimaginary < 4) {
				double zrealUpdated = zreal * zreal - zimaginary * zimaginary + c.getReal();
				double zimaginaryUpdated = 2 * Math.abs(zreal) * Math.abs(zimaginary) + c.getImaginary();

				zreal = zrealUpdated;
				zimaginary = zimaginaryUpdated;

				iterations += 1;
			}
			c2 = new Complex(zreal, zimaginary);
		}
		
		// Colour point
		bufferedImage.setRGB(x, y, colourPoint(iterations, c2));
	}
}

@SuppressWarnings("serial")
// Generates the Julia set
class GenerateJuliaSet extends GenerateFractals {

	// This stores whether the mouse is in the mandlebrot or not
	private boolean outOfMandel = true;

	private Complex z;
	
	// x, y coordinate ranges of displayed area
	protected static double maxX, minX, maxY, minY;

	// Constant screen dimensions
	final static int JULIA_WIDTH = 300;
	final static int JULIA_HEIGHT = 300;

	private BufferedImage juliaSetImg;

	protected GenerateJuliaSet(Complex z) {
		this.z = z;

		juliaSetImg = new BufferedImage(JULIA_WIDTH, JULIA_HEIGHT, BufferedImage.TYPE_INT_RGB);

		maxX = DEFAULT_MAXX;
		minX = DEFAULT_MINX;
		minY = DEFAULT_MINY;
		maxY = DEFAULT_MAXY;
	}

	protected GenerateJuliaSet() {

		juliaSetImg = new BufferedImage(JULIA_WIDTH, JULIA_HEIGHT, BufferedImage.TYPE_INT_RGB);

		maxX = DEFAULT_MAXX;
		minX = DEFAULT_MINX;
		minY = DEFAULT_MINY;
		maxY = DEFAULT_MAXY;
	}

	protected void setOutOfMandel(boolean b) {
		outOfMandel = b;
	}

	protected void init() {
		this.setPreferredSize(new Dimension(JULIA_HEIGHT, JULIA_WIDTH));
	}

	// Updates the complex number which is used
	protected void setComplex(Complex z) {
		this.z = z;
	}
	
	// Generate the Mandlebrot set
	protected void computeMandel() {
		// Go through every pixel
		for (int y = 0; y < JULIA_HEIGHT; y++) {
			for (int x = 0; x < JULIA_WIDTH; x++) {
				
				//Visit each point
				checkPoint(coordToComplex(x, y), x, y);
			}
		}
	}
	
	//Converts the coordinates to its complex number equivalent
	protected static Complex coordToComplex(int x, int y) {
		// Mapping points to complex number
		double a = minX + x * (maxX - minX) / JULIA_WIDTH;
		double b = minY + y * (maxY - minY) / JULIA_HEIGHT;

		return new Complex(a, b);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// If in Mandel display Julia set in relation to position; otherwise
		// show black
		if (!outOfMandel) {
			computeMandel();
			g.drawImage(juliaSetImg, 0, 0, this);
		} else {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, JULIA_WIDTH, JULIA_HEIGHT);
		}
	}

	// Returns the number of iterations that each point takes
	protected void checkPoint(Complex c, int x, int y) {
		int iterations;
		boolean k = false;
		for (iterations = 0; iterations < JULIA_HEIGHT + JULIA_WIDTH; iterations++) {

			c = c.square().add(z);

			if (c.modulusSquared() > 4) {
				k = true;
				break;
			}
		}
		if (!k) {
			iterations = JULIA_HEIGHT + JULIA_WIDTH - 1;
		}
		// Colour point
		juliaSetImg.setRGB(x, y, colourPoint(iterations, c));
	}
}