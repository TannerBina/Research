import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JComponent;

public class Map extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8385937352113946031L;
	// holds all vertices
	private Vertex[] vertices;
	// holds the current tour
	private Tour tour;
	// holds clusters to paint
	private ArrayList<Cluster> clusters = new ArrayList<>();

	/**
	 * Sets the current tour to the entered tour.
	 * 
	 * @param loaded
	 */
	public void sTr(Tour loaded) {
		tour = loaded;
		repaint();
	}

	/**
	 * Adds a list of clusters to display on the map.
	 * 
	 * @param clusters
	 */
	public void addClusters(ArrayList<Cluster> clusters) {
		this.clusters.clear();
		for (int i = 0; i < clusters.size(); i++) {
			this.clusters.add(clusters.get(i));
		}
		repaint();
	}

	/**
	 * Sets the vertices displayed to the entered vertices. The tour is set to null
	 * by default.
	 * 
	 * @param vertices
	 */
	public void setVertices(Vertex[] vertices) {
		this.vertices = vertices;
		tour = null;
		repaint();
	}

	/**
	 * Returns the currently displayed tour.
	 * 
	 * @return
	 */
	public Tour getTour() {
		return tour;
	}

	/**
	 * Paints the grid and everything. Ported from old version.
	 */
	public void paintComponent(Graphics g) {
		
		int width = getSize().width;
		int height = getSize().height;

		g.setColor(Color.LIGHT_GRAY);

		int size = 20;
		int numSections = (width - 40) / size;
		for (int i = 1; i <= numSections / 2; i++) {
			g.drawLine(width / 2 - i * size, 20, width / 2 - i * size, height - 20);
			g.drawLine(width / 2 + i * size, 20, width / 2 + i * size, height - 20);
		}
		numSections = (height - 40) / size;
		for (int i = 1; i <= numSections / 2; i++) {
			g.drawLine(20, height / 2 - i * size, width - 20, height / 2 - i * size);
			g.drawLine(20, height / 2 + i * size, width - 20, height / 2 + i * size);
		}

		g.setColor(Color.BLACK);

		g.drawLine(20, 20, width - 20, 20);
		g.drawLine(20, height - 20, width - 20, height - 20);
		g.drawLine(20, 20, 20, height - 20);
		g.drawLine(width - 20, 20, width - 20, height - 20);
		g.drawLine(width / 2, 20, width / 2, height - 20);
		g.drawLine(20, height / 2, width - 20, height / 2);

		if (vertices != null) {
			double scale = 1;
			double maxX = 0;
			double maxY = 0;
			double minX = 1000000000;
			double minY = 1000000000;

			for (int i = 0; i < vertices.length; i++) {
				if (vertices[i].getX() > maxX) {
					maxX = vertices[i].getX();
				}
				if (vertices[i].getX() < minX) {
					minX = vertices[i].getX();
				}
				if (vertices[i].getY() > maxY) {
					maxY = vertices[i].getY();
				}
				if (vertices[i].getY() < minY) {
					minY = vertices[i].getY();
				}
			}
			double xRange = maxX - minX;
			double yRange = maxY - minY;
			if (width / xRange > height / yRange) {
				scale = ((height - 50)) / yRange;
			} else {
				scale = ((width - 50)) / xRange;
			}

			int townSize = 4;

			for (int i = 0; i < vertices.length; i++) {
				int x = (int) ((vertices[i].getX() - minX) * scale) + 25;
				int y = (int) ((vertices[i].getY() - minY) * scale) + 25;
				g.setColor(Color.BLACK);
				g.drawOval(x - townSize / 2, y - townSize / 2, townSize, townSize);
				if (GUI.SHOWPOINTNUMBERS) {
					g.drawString(((Integer) vertices[i].getID()).toString(), x - townSize / 2, y - townSize / 2);
				}
				if (tour != null && tour.getArray()[0] == vertices[i]) {
					g.setColor(Color.CYAN);
					g.fillOval(x - townSize, y - townSize, townSize*2, townSize*2);
					g.setColor(Color.BLACK);
					g.drawOval(x - townSize, y - townSize, townSize*2, townSize*2);
					
				} else {
					g.setColor(Color.BLUE);
					g.fillOval(x - townSize / 2, y - townSize / 2, townSize, townSize);
				}
			}
			if (tour != null) {
				g.setColor(Color.RED);
				Vertex[] order = tour.getArray();
				for (int i = 0; i < order.length - 1; i++) {
					int x1 = (int) ((order[i].getX() - minX) * scale) + 25;
					int x2 = (int) ((order[i + 1].getX() - minX) * scale) + 25;
					int y1 = (int) ((order[i].getY() - minY) * scale) + 25;
					int y2 = (int) ((order[i + 1].getY() - minY) * scale) + 25;
					if (i == 0) {
						g.setColor(Color.ORANGE);
						townSize = 10;
						g.fillOval(x2 - townSize / 2, y2 - townSize / 2, townSize, townSize);
						g.setColor(Color.BLACK);
						g.drawOval(x2 - townSize/2, y2 - townSize/2, townSize, townSize);
						g.setColor(Color.RED);
					}
					Graphics2D g2 = (Graphics2D) g;
					g2.setStroke(new BasicStroke(2));
					if (i < vertices.length * .1) {
						g2.setColor(Color.BLUE);
					} else {
						g2.setColor(Color.DARK_GRAY);
					}
					g2.drawLine(x1, y1, x2, y2);
				}
			}
			// draws clusters if there are any added
			float opacity = 0.3f;
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			for (int i = 0; i < clusters.size(); i++) {
				ArrayList<Vertex> contents = new ArrayList<>(clusters.get(i).getContent());
				int cMinX = 1000000000;
				int cMaxX = 0;
				int cMinY = 1000000000;
				int cMaxY = 0;
				int cXDif = 0;
				int cYDif = 0;
				// System.out.println("Cluster " + i);
				for (int j = 0; j < contents.size(); j++) {
					// System.out.print("Town " + contents.get(j).identifier +
					// ", ");
					double x = (contents.get(j).getX() - (int) minX) * scale;
					double y = (contents.get(j).getY() - (int) minY) * scale;
					if (x < cMinX) {
						cMinX = (int) x;
					}
					if (x > cMaxX) {
						cMaxX = (int) x;
					}
					if (y < cMinY) {
						cMinY = (int) y;
					}
					if (y > cMaxY) {
						cMaxY = (int) y;
					}
				}
				// System.out.println();
				cXDif = cMaxX - cMinX;
				cYDif = cMaxY - cMinY;
				switch (i % 6) {
				case 0:
					g.setColor(Color.GRAY);
					break;
				case 1:
					g.setColor(Color.RED);
					break;
				case 2:
					g.setColor(Color.MAGENTA);
					break;
				case 3:
					g.setColor(Color.ORANGE);
					break;
				case 4:
					g.setColor(Color.PINK);
					break;
				case 5:
					g.setColor(Color.YELLOW);
					break;
				default:
					g.setColor(Color.BLACK);
					break;
				}
				g.fillRect(cMinX + 20, cMinY + 20, cXDif + 10, cYDif + 10);
			}
		}
	}
}
