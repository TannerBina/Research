import java.lang.*;

public class Vertex implements Comparable<Vertex>{
	
	private int x;
	private int y;
	public int id;
	private double weight = 1;

    //used in branchSolve for WMST
    public int bbWeight = 0;
	
	//used for saving a position with genetic algorithms
	private double averagePosition;
	
	//used for clutering
	public double averageDistance;
	
	//used for determining outlier status
	public Vertex closestVertex;

	public double closestDist;

	public double visitDist = 0;

	public boolean visited = false;

	/*
	Used for new clustering that we are trying
	 */
	public int compareTo(Vertex v){
		double one = closestDist + averageDistance;
		double two = v.closestDist + v.averageDistance;

		return Double.compare(one, two);
	}
	

    public Vertex(Vertex p){
        this.x = p.x;
        this.y = p.y;
        this.id = p.id;
        visitDist = 0;
    }

	/**
	 * Constructs a point with specified x, y, and id
	 * Uses default weight of 1.
	 * @param x
	 * @param y
	 * @param i
	 */
	public Vertex(int x, int y, int i) {
		this.x = x;
		this.y = y;
		this.id = i;
		visitDist = 0;
	}
	
	/**
	 * Creates a point with specified x, y, id, and weight
	 * @param x
	 * @param y
	 * @param id
	 * @param wgt
	 */
	public Vertex(int x, int y, int id, double wgt){
		this.x = x;
		this.y = y;
		this.id = id;
		this.weight = wgt;
		visitDist = 0;
	}
	
	/**
	 * Sets the closest point to this point in the point set
	 * @param vertices
	 */
	public void setClosestVertex(Vertex[] vertices){
		double[] dist = new double[vertices.length];
		
		//create distance array
		for (int i = 0; i < vertices.length; i++){
			dist[i] = getDistanceTo(vertices[i]);
			
			//if distance is 0 set arbitrarily high
			if (dist[i] == 0){
				dist[i] = 1000000000;
			}
		}
		
		//sets closest point to smallest value point
		closestVertex = vertices[Cluster.getSmallestIn(dist)];
		closestDist = dist(closestVertex);
	}
	
	/**
	 * Gets the closest point
	 * @return
	 */
	public Vertex getClosestVertex(){
		return closestVertex;
	}

	/**
	 * Gets the x value.
	 * @return
	 */
	public double getX() {
		return x;
	}

	/**
	 * Gets the y value.
	 * @return
	 */
	public double getY() {
		return y;
	}

	/**
	 * Gets the id of the point
	 * @return
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Gets average distance to other cities
	 * @return
	 */
	public double getAverageDistance(){
		return averageDistance;
	}

	/**
	 * Gets the average position in tours for ga.
	 * @return
	 */
	public double getAveragePosition(){
		return averagePosition;
	}

	public double dist(Vertex a){
		return getDistanceTo(a);
	}
	
	/**
	 * Gets the distance to another point
	 * @param a
	 * @return
	 */
	public double getDistanceTo(Vertex a){
		 return Math.sqrt(Math.pow(x - a.x, 2) + Math.pow(y - a.y, 2));
	}
	
	/**
	 * Saves the average distance from the points
	 * in the entered point array.
	 * @param pnts
	 */
	public void saveAverageDistance(Vertex[] pnts){
		double[] xs = new double[pnts.length];
		double[] ys = new double[pnts.length];
		for (int i = 0; i < pnts.length; i++){
			xs[i] = pnts[i].getX();
			ys[i] = pnts[i].getY();
		}
		Vertex averageVertex = new Vertex((int) getAverage(xs), (int) getAverage(ys), 0);
		averageDistance = getDistanceTo(averageVertex);
	}
	
	/**
	 * Returns the average of a double array.
	 * @param array
	 * @return
	 */
	private double getAverage(double[] array){
		double sum = 0;
        for (int i = 0; i < array.length; i++){
            sum += array[i];
        }
        return sum/(double)array.length;
	}

	public double getWeight() {
		return weight;
	}

	public double weight(){return weight;}


	//perhaps incorperate these
    private int shapeX;
    private int shapeY;

    public int getShapeX(){
        return shapeX;
    }

    public int getShapeY(){
        return shapeY;
    }

    public void setShapeX(int x){
        shapeX = x;
    }

    public void setShapeY(int y){
        shapeY = y;
    }
}
