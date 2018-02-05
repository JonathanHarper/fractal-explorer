
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

//This creates the Mandelbrot set
public class FractalExplorer {
	public static void main(String[] args) {
		FEFrame frame = new FEFrame("Fractal Explorer");
		frame.init();
	}
}

// Responsible for creating the JFrame
@SuppressWarnings("serial")
class FEFrame extends JFrame {

	protected GenerateJuliaSet js;
	protected GenerateMandlebrotBurningShip mbPanel;

	// Stores the point at which the user selected
	Complex selectedPoint;

	// Prevents Julia set updating when mouse is clicked
	boolean pointSelected = false;

	// Where the user has clicked on the Mandelbrot
	protected JLabel userSelectedPoint;
	// The position in which the user is hovering over the mandelbrot
	protected JLabel userHoveredPoint;

	// Maximum number of favourites allowed
	static final int MAXIMUM_FAVOURITES = 5;
	// Holds the favourites
	private Complex[] favouriteList = new Complex[MAXIMUM_FAVOURITES];
	// Stores position of last complex point added to the favourite list
	Complex lastElementAdded = null;

	// Text fields for the selection of min and max points for the fractal
	protected static JTextField minXText, maxXText, minYText, maxYText;

	// Rectangle to be displayed
	protected Shape r;
	protected static Point startPosition, endPosition;
	//If drag has been released
	protected static boolean dragEnded = false;
	//Where the mouse was initially clicked
	protected Point startPoint;
	protected static Point start, end;
	
	//This allows for the selection of the burning ship fractal
	protected static JRadioButton burningShip;
	//Mandelbrot fractal
	protected static JRadioButton mandelbrot;
	
	protected FEFrame(String title) {
		super(title);
		mbPanel = new GenerateMandlebrotBurningShip();
		rectangleListeners();
	}

	protected FEFrame() {
		mbPanel = new GenerateMandlebrotBurningShip();
		rectangleListeners();
	}

	// Initialises the JFrame
	protected void init() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		Container pane = this.getContentPane();
		this.setLayout(new BorderLayout());

		mbPanel.init();

		pane.add(mbPanel);
		pane.add(rightPanel(), BorderLayout.EAST);

		this.setResizable(false);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	// This is the mouse listeners for creating the rectangles
	public void rectangleListeners() {
		mbPanel.addMouseListener(new MouseAdapter() {
			//When released, the rectangle has been drawn
			public void mouseReleased(MouseEvent e) {
				//Temporary variables to hold positions
				start = startPosition;
				end = endPosition;
				
				//Create a rectangle from the starting point and the point where we released
				r = makeRectangle(startPosition.x, startPosition.y, e.getX(), e.getY());
				
				//Re-initialising variables
				endPosition = null;
				startPosition = null;
				
				dragEnded = true;
				repaint();
			}			
			
			//Begins creating the triangle
			public void mousePressed(MouseEvent e) {
				startPosition = new Point(e.getX(), e.getY());
				endPosition = startPosition;
				repaint();
			}
		});

		mbPanel.addMouseMotionListener(new MouseMotionAdapter() {
			//End position constantly updated as mouse is moved to provide live updates
			public void mouseDragged(MouseEvent e) {
				startPoint = startPosition;
				endPosition = new Point(e.getX(), e.getY());
				repaint();
			}
		});
	}

	// Right hand side panel containing Julia set, controls, and information
	protected JPanel rightPanel() {
		JPanel right = new JPanel();

		js = new GenerateJuliaSet();

		mbPanel.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				pointSelected = !pointSelected;

				if (pointSelected) {
					Complex c = GenerateFractals.coordToComplex(e.getX(), e.getY(), GenerateMandlebrotBurningShip.MBBS_WIDTH, GenerateMandlebrotBurningShip.MBBS_HEIGHT);
					selectedPoint = c;

					// Stores the position in which the x and y were clicked
					double complexXClicked = (double) Math.round(c.getReal() * 10000) / 10000;
					double complexYClicked = (double) Math.round(c.getImaginary() * 10000) / 10000;

					// This shall update the Julia set
					js.setComplex(c);

					// Correctly append either + or - to imaginary part
					if (complexYClicked > 0) {
						userSelectedPoint
								.setText("User selected point: " + complexXClicked + " + " + complexYClicked + "i");
					} else {
						userSelectedPoint.setText(
								"User selected point: " + complexXClicked + " - " + Math.abs(complexYClicked) + "i");
					}
				} else {
					userSelectedPoint.setText("User selected point: ");
				}
			}

			public void mouseExited(MouseEvent e) {
				userHoveredPoint.setText("User hovered point: ");
				js.setOutOfMandel(true);
				// The Julia set will still be displayed if a point has been
				// selected
				if (!pointSelected)
					repaint();
			}
		});

		mbPanel.addMouseMotionListener(new MouseAdapter() {

			public void mouseMoved(MouseEvent e) {
				Complex c = GenerateFractals.coordToComplex(e.getX(), e.getY(), GenerateMandlebrotBurningShip.MBBS_WIDTH, GenerateMandlebrotBurningShip.MBBS_HEIGHT);
				// Mouse is within the Mandelbrot set
				js.setOutOfMandel(false);

				// Stores the position in which the x and y were clicked
				double complexXHovered = (double) Math.round(c.getReal() * 10000) / 10000;
				double complexYHovered = (double) Math.round(c.getImaginary() * 10000) / 10000;

				// This shall update the julia set
				if (!pointSelected) {
					// Updates the position of the rectangle
					startPoint = startPosition;
					endPosition = new Point(e.getX(), e.getY());

					// Updates the julia set
					js.setComplex(c);
					repaint();
				}

				// Correctly append either + or - to imaginary part
				if (complexYHovered > 0) {
					userHoveredPoint.setText("User hovered point: " + complexXHovered + " + " + complexYHovered + "i");
				} else {
					userHoveredPoint.setText(
							"User hovered point: " + complexXHovered + " - " + Math.abs(complexYHovered) + "i");
				}
			}
		});

		right.setLayout(new BorderLayout());

		right.add(controlsPanel(), BorderLayout.NORTH);
		right.add(juliaFavourites(), BorderLayout.SOUTH);
		right.add(js, BorderLayout.CENTER);

		right.setPreferredSize(new Dimension(300, 700));

		return right;
	}

	// Creates a rectangle using the mouse positions
	static Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
		Rectangle2D rectangle = new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2),
				Math.abs(y1 - y2));
		return (Float) rectangle;
	}

	// Control and information JPanel
	protected JPanel controlsPanel() {
		JPanel controls = new JPanel();

		controls.setLayout(new GridBagLayout());
		TitledBorder controlsTitle = BorderFactory.createTitledBorder("Information and Controls");
		controls.setBorder(controlsTitle);

		GridBagConstraints c = new GridBagConstraints();

		c.insets = new Insets(10, 0, 0, 0);

		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;

		controls.add(new JLabel("Iterations: "), c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.WEST;

		JTextField iterations = new JTextField(20);
		iterations.setText("" + GenerateFractals.MAX_ITERATIONS);

		controls.add(iterations, c);

		c.fill = GridBagConstraints.BOTH;

		c.gridwidth = 1;
		c.gridy = 1;
		c.gridx = 0;

		controls.add(new JLabel("X Range: "), c);

		c.gridx = 1;

		minXText = new JTextField(9);
		minXText.setText("" + GenerateFractals.minX);

		controls.add(minXText, c);

		c.gridx = 2;

		controls.add(new JLabel(" to "), c);

		maxXText = new JTextField(9);
		maxXText.setText("" + GenerateFractals.maxX);

		c.gridx = 3;
		c.fill = GridBagConstraints.WEST;

		controls.add(maxXText, c);

		c.gridy = 2;
		c.gridx = 0;

		c.fill = GridBagConstraints.BOTH;

		controls.add(new JLabel("Y Range: "), c);

		c.gridx = 1;

		minYText = new JTextField(9);
		minYText.setText("" + GenerateFractals.minY);

		controls.add(minYText, c);

		c.gridx = 2;

		controls.add(new JLabel(" to "), c);

		c.fill = GridBagConstraints.WEST;
		c.gridx = 3;
		maxYText = new JTextField(9);
		maxYText.setText("" + GenerateFractals.maxY);

		controls.add(maxYText, c);
		
		c.gridy = 3;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;

		userSelectedPoint = new JLabel("User selected point: ");

		controls.add(userSelectedPoint, c);

		c.gridy = 4;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;

		userHoveredPoint = new JLabel("User hovered point: ");

		controls.add(userHoveredPoint, c);

		c.insets = new Insets(5, 0, 0, 0);

		// When clicked, mandlebrot will render with selected choices
		JButton button = new JButton("Render");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					GenerateFractals.minX = Double.parseDouble(minXText.getText());
					GenerateFractals.maxX = Double.parseDouble(maxXText.getText());
					GenerateFractals.minY = Double.parseDouble(minYText.getText());
					GenerateFractals.maxY = Double.parseDouble(maxYText.getText());

					GenerateFractals.MAX_ITERATIONS = Integer.parseInt(iterations.getText());
				} catch (NumberFormatException err) {
					System.err.println(err);
				}
				repaint();
			}
		});

		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 5;

		controls.add(button, c);

		//Stores the radio buttons
		JPanel radioPanel = new JPanel();
			
		mandelbrot = new JRadioButton("Mandelbrot");
		burningShip = new JRadioButton("Burning Ship");
		
		ButtonGroup bg = new ButtonGroup();
		
		bg.add(mandelbrot);
		bg.add(burningShip);
		
		radioPanel.add(burningShip);
		radioPanel.add(mandelbrot);
				
		c.gridy = 6;
		
		controls.add(radioPanel, c);
	
		return controls;
	}

	// Panel to store favourites of Julia sets
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected JPanel juliaFavourites() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		TitledBorder favouritesTitle = BorderFactory.createTitledBorder("Favourites");
		panel.setBorder(favouritesTitle);

		JButton add = new JButton("Add Favourite");
		JButton delete = new JButton("Delete Favourite");

		// Adds buttons to the favourites list
		JPanel buttons = new JPanel();

		buttons.add(add);
		buttons.add(delete);

		// Allows for modification of favourites during execution
		DefaultListModel modifyList = new DefaultListModel();

		JList favouriteList = new JList(modifyList);
		// Allows for only single selection
		favouriteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// If a point has been selected add the item
				if (pointSelected) {
					try {
						addFavourite(modifyList);
					} catch (Exception e) {
						System.err.println(e);
					}
				}
			}
		});

		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					deleteFavourite(modifyList, favouriteList);
				} catch (Exception e) {
					System.err.println(e);
				}
			}
		});

		panel.add(favouriteList);
		panel.add(buttons, BorderLayout.SOUTH);

		panel.setPreferredSize(new Dimension(300, 150));

		return panel;
	}

	// Deletes chosen favourite point from list
	@SuppressWarnings("rawtypes")
	protected void deleteFavourite(DefaultListModel dlm, JList list) throws Exception {
		// If item has been selected
		if (list.getSelectedIndex() != -1) {
			dlm.remove(list.getSelectedIndex());
		} else {
			throw new IllegalArgumentException("Please select a favourite point to delete");
		}
	}

	// Adds favourite point to favourite list
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addFavourite(DefaultListModel dlm) throws Exception {
		// Maximum number of elements
		if (dlm.size() < MAXIMUM_FAVOURITES) {

			// Identifies if element is being added more than once
			boolean elementAddedLast = false;

			if (dlm.getSize() > 0) {
				// Ensures that same point not selected more than once
				if (selectedPoint.modulusSquared() == lastElementAdded.modulusSquared()) {
					elementAddedLast = true;
				}
			}

			// If a favourite point has been selected
			if (pointSelected) {
				// Point not already added
				if (!elementAddedLast) {
					lastElementAdded = selectedPoint;
					elementAddedLast = false;
					// Stores the position in which the x and y were clicked
					double x = (double) Math.round(selectedPoint.getReal() * 10000) / 10000;
					double y = (double) Math.round(selectedPoint.getImaginary() * 10000) / 10000;

					if (y > 0) {
						dlm.addElement("Point:   " + x + " + " + y + "i");
					} else {
						dlm.addElement("Point:   " + x + " - " + Math.abs(y) + "i");
					}
					favouriteList[dlm.getSize() - 1] = selectedPoint;
				} else {
					throw new IllegalArgumentException("Duplicate favourites not allowed.");
				}
			}
		} else {
			throw new IndexOutOfBoundsException("Maximum number of 5 favourites.");
		}
	}
}

