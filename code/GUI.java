import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class GUI extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 317535027810558393L;

	// dictates whether point numbers are shown
	public static boolean SHOWPOINTNUMBERS = false;

	public static final Map MAP = new Map();
	private Vertex[] vertices;

	private static Tour SAVEDTOUR;

	/**
	 * Creates the GUI window.
	 */
	public GUI() {
		super("Tennis Ball Problem");
		settup();
	}

	JMenuBar mBar;

	// file methods
	JMenu file;
	JMenuItem load;

	// solve methods
	JMenu solve;
	JMenuItem loadTour;
	JMenuItem cluster;
    JMenuItem bruteForce;

	// options items
	JMenu options;
	JMenuItem saveTour;
	JMenuItem showNumbers;
    JMenuItem printDistances;

	public static JTextArea output;

	public static JProgressBar progressBar;

	/**
	 * Sets up all GUI elements
	 */
	private void settup() {
		setSize(1000, 1000);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		mBar = new JMenuBar();

		file = new JMenu("File");
		mBar.add(file);
		load = new JMenuItem("Load");
		initialize(load, file);

		solve = new JMenu("Solve");
		mBar.add(solve);
		loadTour = new JMenuItem("Load Tour");
		initialize(loadTour, solve);
		cluster = new JMenuItem("Clustering Solve");
		initialize(cluster, solve);
        bruteForce = new JMenuItem("Brute Force Solve");
        initialize(bruteForce, solve);

		options = new JMenu("Options");
		mBar.add(options);
		saveTour = new JMenuItem("Save Current Tour");
		initialize(saveTour, options);
		showNumbers = new JMenuItem("Show Vertex Numbers");
		initialize(showNumbers, options);
        printDistances = new JMenuItem("Print Distance Array");
        initialize(printDistances, options);

		output = new JTextArea("Output Field\n\n", 10, 0);
		output.setEditable(false);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		JPanel both = new JPanel();
		both.add(mBar);
		both.add(progressBar);

		setVisible(true);

		add(BorderLayout.NORTH, both);
		add(BorderLayout.CENTER, MAP);
		add(BorderLayout.SOUTH, output);
	}

	/**
	 * Initializes a menuItem to a menu.
	 * 
	 * @param item
	 * @param menu
	 */
	private void initialize(JMenuItem item, JMenu menu) {
		item.addActionListener(this);
		menu.add(item);
	}

	/**
	 * Loads a tour into the map and finds work. Tour Format city, num, num
	 */
	private void loadTour() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("."));
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try (Scanner s = new Scanner(file)) {
				Vertex[] order = new Vertex[vertices.length + 1];
				for (int i = 0; i < vertices.length + 1; i++) {
					order[i] = vertices[s.nextInt()];
					s.nextInt();
					s.nextDouble();
				}
				Tour loaded = new Tour(order);
				MAP.sTr(loaded);
				output.append("Tour Loaded\n");
				output.append("Work: " + loaded.getWork() + "\n");
				if (GUI.SAVEDTOUR != null) {
					output.append("Compared To Best: " + (loaded.getWork() / GUI.SAVEDTOUR.getWork()) + "\n");
				}
				output.append("Distance: " + loaded.getDistance() + "\n");
			} catch (FileNotFoundException e) {
			}
		}
	}

	/**
	 * Loads a set of vertices into the map. Vertex format. numCities x y x y
	 */
	private void load() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("../pointSets"));
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try (Scanner s = new Scanner(file)) {
				vertices = new Vertex[s.nextInt()];
				for (int i = 0; i < vertices.length; i++) {
					int x = (int) s.nextDouble();
					int y = (int) s.nextDouble();
					vertices[i] = new Vertex(x, y, i);
				}
				MAP.setVertices(vertices);
			} catch (FileNotFoundException e) {
			}
		}
	}

	public static long startTime;
	public static long estimatedTime;

	public static boolean running = false;
	/**
	 * Handles all action events
	 */
	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		output.setText("Output Field\n\n");
		if (s.equalsIgnoreCase(load.getActionCommand())) {
			load();
		} else if (s.equalsIgnoreCase(loadTour.getActionCommand())) {
			loadTour();
		} else if (s.equalsIgnoreCase(saveTour.getActionCommand())) {
			// sets the saved tour to the current
			SAVEDTOUR = MAP.getTour();
		} else if (s.equalsIgnoreCase(cluster.getActionCommand())) {
			// estimates the time based on equation. Usable for sets 500-5000
			// found through data analysis.
			estimatedTime = (long) (.0004*Math.pow(vertices.length, 2) -.1515* vertices.length+221.15);
			GUI.output.append("Estimated Time: " + estimatedTime + " milliseconds. \n");
			startTime = System.currentTimeMillis();
			output.paintImmediately(0, 0, 1000, 1000);
			progressBar.setMaximum((int) estimatedTime);

			Vertex start = vertices[0];
			Vertex[] all = new Vertex[vertices.length - 1];
			for (int i = 0; i < all.length; i++) {
				all[i] = vertices[i + 1];
			}
			long startT = System.currentTimeMillis();
			Runnable r = new Runnable() {
				public void run() {
					Tour best = new Clustering(all, start, start).getBest();
					MAP.sTr(best);
					output.append("Pre Opt Work: " + best.getWork() + "\n");
					output.append("Pre Opt Time: " + (System.currentTimeMillis() - startT) + "\n");
					best.InsertOptimization();
					best.TwoOptimization();
					output.append("Final Work: " + best.getWork() + "\n");
					output.append("Final Time: " + (System.currentTimeMillis() - startT));
					MAP.addClusters(Cluster.hClustering(all, ClusterNode.CUTOFF));
					running = false;
				}
			};
			
			running = true;
			new Thread(r).start();
			while(running){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				progressBar.setValue((int)(System.currentTimeMillis()-startT));
				progressBar.paintImmediately(0, 0, 200, 100);
			}
			progressBar.setValue(progressBar.getMaximum());
			
		} else if (s.equalsIgnoreCase(showNumbers.getActionCommand())) {
			if (SHOWPOINTNUMBERS) {
				SHOWPOINTNUMBERS = false;
			} else {
                SHOWPOINTNUMBERS = true;
            }
        } else if (s.equalsIgnoreCase(bruteForce.getActionCommand())){
            estimatedTime = (long) (.0004*Math.pow(vertices.length, 2) -.1515* vertices.length+221.15);
            GUI.output.append("Estimated Time: " + estimatedTime + " milliseconds. \n");
            startTime = System.currentTimeMillis();
            output.paintImmediately(0, 0, 1000, 1000);
            progressBar.setMaximum((int) estimatedTime);

            Vertex start = vertices[0];
            Vertex[] all = new Vertex[vertices.length - 1];
            for (int i = 0; i < all.length; i++) {
                all[i] = vertices[i + 1];
            }
            long startT = System.currentTimeMillis();
            Runnable r = new Runnable() {
                public void run() {
                    Tour best = new Solver(all, start, start).getBest();
                    MAP.sTr(best);
                    output.append("Pre Opt Work: " + best.getWork() + "\n");
                    output.append("Pre Opt Time: " + (System.currentTimeMillis() - startT) + "\n");
                    //best.InsertOptimization();
                    //best.TwoOptimization();
                    output.append("Final Work: " + best.getWork() + "\n");
                    output.append("Final Time: " + (System.currentTimeMillis() - startT));
                    running = false;
                }
            };

            running = true;
            new Thread(r).start();
            while(running){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                progressBar.setValue((int)(System.currentTimeMillis()-startT));
                progressBar.paintImmediately(0, 0, 200, 100);
            }
            progressBar.setValue(progressBar.getMaximum());
        } else if (s.equalsIgnoreCase(printDistances.getActionCommand())){
            printDistanceArray();
        }
	}

	private void printDistanceArray(){
        for (int i = 0; i < vertices.length; i++){
            for (int j = 0; j < vertices.length; j++){
                System.out.print((int) vertices[i].getDistanceTo(vertices[j]) + ", ");
            }
            System.out.println();
        }
    }

	public static void main(String[] args) {
		new GUI();
	}
}
