import java.util.ArrayList;

public class Clustering{
	public static final ArrayList<Vertex> TOURORDER = new ArrayList<>();
	private Tour best;
	
	/**
	 * Solves a problem using clustering.
	 * Enter pnts, start and end
	 * @param vertices
	 * @param start
	 * @param end
	 */
	public Clustering(Vertex[] vertices, Vertex start, Vertex end){

		for (int i = 0; i < vertices.length; i++){
			vertices[i].saveAverageDistance(vertices);
			vertices[i].setClosestVertex(vertices);
		}
		//clears the final order array.
		TOURORDER.clear();
		//creates initial cluster
		Cluster initial = new Cluster(vertices[0], 0);
		for (int i = 1; i < vertices.length; i++){
			initial.add(vertices[i]);
		}
		//creates root cluster node
		//best = (new ClusterNode(initial, start, end, 1)).bestThrough;
		best = new ClusterNode(initial, start, end, 0).bestThrough;
		/*
		//uses the final order array to create a tour
		//now including start and end
		Vertex[] tour = new Vertex[TOURORDER.size()+2];
		tour[0] = start;
		tour[tour.length-1] = end;
		for (int i = 0; i < TOURORDER.size(); i++){
			tour[i+1] = TOURORDER.get(i);
		}
		//save the tour.
		best = new Tour(tour);*/
	}
	
	/**
	 * Returns the tour generated
	 * @return
	 */
	public Tour getBest(){
		return best;
	}
}
