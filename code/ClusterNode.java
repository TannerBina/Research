import java.util.ArrayList;
import java.util.Collections;

public class ClusterNode{
	
	public Tour bestThrough;

	private Cluster cluster;
	
	private ArrayList<ClusterNode> children = new ArrayList<>();
	
	//The Points (or Clusters) which are visited prior and post
	//visiting all the cities or children in the node
	private Vertex before;
	private Vertex after;

	public Tour bOrder;
	
	public static final int CUTOFF = 20;
	public static final int LEAFCUTOFF = 20;

	public static final int TIMELIMIT = 3000;

	/*
	public ClusterNode(Cluster cluster, Vertex before, Vertex after, int pos){
		this.cluster = cluster;
		this.before = before;
		this.after = after;

		BranchBound.TIMELIMIT = BBTIMELIMIT;
		if (cluster.getContent().size() < BBCUT){
			BranchBound bb = new BranchBound();
			Tour opt = bb.branchBound(before, after, cluster.getContent(), pos);
			if (bb.optimalReached){
				bestThrough = opt;
				return;
			}
		}

		//get the vertices from the cluster into an array
		Vertex[] vertices = new Vertex[this.cluster.size()];
		for (int i = 0; i < this.cluster.size(); i++){
			vertices[i] = this.cluster.get(i);
		}

		ArrayList<Cluster> childrenCluster = Cluster.hClustering(vertices, BBCUT);

		//turn the clusters into vertices
		ArrayList<Vertex> childrenVerts = new ArrayList<>();
		for (Cluster c : childrenCluster){
			childrenVerts.add(c.toPoint());
		}

		BranchBound bb = new BranchBound();
		BranchBound.TIMELIMIT = BBTIMELIMIT;
		Tour optimal = bb.branchBound(before, after, childrenVerts, pos);
		while(!bb.optimalReached){
			for (int k = 0; k < childrenCluster.size() * BBDEC; k++){
				Cluster minOne = childrenCluster.get(0);
				Cluster minTwo = childrenCluster.get(1);
				double minDist = minOne.dist(minTwo);
				for (int i = 0; i < childrenCluster.size(); i++){
					for (int j = i+1; j < childrenCluster.size(); j++){
						double test = childrenCluster.get(i).dist(childrenCluster.get(j));
						if (test < minDist){
							minDist = test;
							minOne = childrenCluster.get(i);
							minTwo = childrenCluster.get(j);
						}
					}
				}

				childrenVerts.remove(minOne.toPoint());
				childrenVerts.remove(minTwo.toPoint());

				childrenCluster.remove(minOne);
				childrenCluster.remove(minTwo);

				for (Vertex v : minTwo.getContent()){
					minOne.add(v);
				}

				minOne.findCenter();
				minOne.calculateSpread();

				childrenCluster.add(minOne);
				childrenVerts.add(minOne.toPoint());
			}
			bb = new BranchBound();
			optimal = bb.branchBound(before, after, childrenVerts, pos);
		}

		for (int i = 1; i < optimal.order().size()-1; i++){
			find(childrenCluster, optimal.order().get(i)).tourPosition = i;
		}

		Collections.sort(childrenCluster);
		
		children = new ArrayList<>();

		int currentPos = pos;
		for (int i = 0; i < childrenCluster.size(); i++){
			if (i == 0){
				children.add(new ClusterNode(childrenCluster.get(i),
					this.before, 
					childrenCluster.get(i).getClosestPointIn(childrenCluster.get(i+1)),
					currentPos));
			} else if (i == childrenCluster.size()-1){
				children.add(new ClusterNode(childrenCluster.get(i),
					childrenCluster.get(i).getClosestPointIn(childrenCluster.get(i-1)),
					this.after,
					currentPos));
			} else {
				children.add(new ClusterNode(childrenCluster.get(i),
					childrenCluster.get(i).getClosestPointIn(childrenCluster.get(i-1)),
					childrenCluster.get(i).getClosestPointIn(childrenCluster.get(i+1)),
					currentPos));
			}
			currentPos += childrenCluster.get(i).size();
		}

		ArrayList<Vertex> order = new ArrayList<>();
		order.add(before);
		for (ClusterNode c : children){
			for (int i = 1; i < c.bestThrough.order().size()-1; i++){
				order.add(c.bestThrough.order().get(i));
			}
		}
		order.add(after);

		bestThrough = new Tour(order, pos);
		bestThrough.InsertOptimization();
		bestThrough.TwoOptimization();
	}

	public Cluster find(ArrayList<Cluster> clus, Vertex v){
		for (Cluster c : clus){
			if (c.isEqualTo(v)){
				return c;
			}
		}
		return null;
	}*/

	public ClusterNode(Cluster cluster, Vertex before, Vertex after, int pos){
		BranchBound.TIMELIMIT = TIMELIMIT;

		this.cluster = cluster;
		this.before = before;
		this.after = after;
		
		//if cluster is too big to be brute forced
		if (this.cluster.size() > LEAFCUTOFF){
			//get the vertices from the cluster into an array
			Vertex[] vertices = new Vertex[this.cluster.size()];
			for (int i = 0; i < this.cluster.size(); i++){
				vertices[i] = this.cluster.get(i);
			}
			
			//cluster the vertices and return the arraylist of clusters
			ArrayList<Cluster> childrenClusters = Cluster.hClustering(vertices, CUTOFF);
			
			//turn the clusters into vertices
			Vertex[] childrenVertices = new Vertex[childrenClusters.size()];
			for (int i = 0; i < childrenVertices.length; i++){
				childrenVertices[i] = childrenClusters.get(i).toPoint();
			}
			
			//solve the best tour starting from before, ending
			//at after and going through all the vertices
			BranchBound bb = new BranchBound();
			Tour bestTour = bb.branchBound(childrenVertices, this.before, this.after, pos+1);
			
			//an array which stores all vertices from the tour but
			//the start and end in order.
			Vertex[] tourVertices = new Vertex[bestTour.getArray().length-2];
			for (int i = 0; i < tourVertices.length; i++){
				tourVertices[i] = bestTour.getArray()[i+1];
			}
			
			//gets the matching clusters to each vertices,
			//(orders the clusters matching the best tour
			Cluster[] order = new Cluster[childrenClusters.size()];
			for (int i = 0; i < order.length; i++){
				order[i] = findCluster(tourVertices[i], childrenClusters);
			}
			
			//creates a child node for each cluster which
			//was generated. The before vertices are the closest
			//point in the previous cluster to the current one
			//the after is closest in next cluster to current
			int currentPos = pos;
			for (int i = 0; i < order.length; i++){
				//if i == 0, before is the original before point
				if (i == 0){
					children.add(new ClusterNode(order[i], this.before,
							order[i].getClosestPointIn(order[i+1]), currentPos));
				
				//if i = ord.lenght-1, after is original after point
				} else if (i == order.length-1){
					children.add(new ClusterNode(order[i],
							order[i].getClosestPointIn(order[i-1]),
                            this.after, currentPos));
				} else {
					children.add(new ClusterNode(order[i],
							order[i].getClosestPointIn(order[i-1]),
							order[i].getClosestPointIn(order[i+1]), currentPos));
				}
				currentPos += order[i].size();
			}

			ArrayList<Vertex> bestOrder = new ArrayList<>();
			bestOrder.add(before);
			for (ClusterNode c : children){
				for (int i = 1; i < c.bestThrough.order().size()-1; i++){
					bestOrder.add(c.bestThrough.order().get(i));
				}
			}
			bestOrder.add(after);
			bestThrough = new Tour(bestOrder, pos);

			bestThrough.InsertOptimization();
			bestThrough.TwoOptimization();
		//if cluster is small enough to brute force
		} else {
			//get vertices in cluster
			Vertex[] vertices = new Vertex[this.cluster.size()];
			for (int i = 0; i < this.cluster.size(); i++){
				vertices[i] = this.cluster.get(i);
			}
			//find the best tour
			BranchBound bb = new BranchBound();
			bestThrough = bb.branchBound(vertices, this.before, this.after, pos + 1);
		}
	}
	
	/**
	 * Finds the Cluster in the arrayList which is 
	 * represented by the point, a
	 * @param a
	 * @param cluster
	 * @return
	 */
	public Cluster findCluster(Vertex a, ArrayList<Cluster> cluster){
		for (int i = 0; i < cluster.size(); i++){
			if (cluster.get(i).isEqualTo(a)){
				return cluster.get(i);
			}
		}
		return null;
	}
}
