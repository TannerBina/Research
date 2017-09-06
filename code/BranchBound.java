import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Collections;
import java.lang.management.*;
import java.io.*;

public class BranchBound{

	private Tour best;

	private int numCuts;
	private int nodesVisited;

	private double startWeight;

	public static long TIMELIMIT = 100000;

	private double startT;


	public static double NUMPOSSIBLEEDGES;
	public double[][] lbDistMatrix;
	public Map<Vertex, Integer> lbMatrixMap;
	public double[][] edgesFound;
	public boolean optimalReached;

	/*
	Create new branch and boun instance
	 */
	public BranchBound(){
		numCuts = 0;
		nodesVisited = 0;
		best = null;
	}

	public double getLBDist(Vertex v, Vertex u){
		return lbDistMatrix[lbMatrixMap.get(v)][lbMatrixMap.get(u)];
	}

	public Tour branchBound(Vertex[] verts, Vertex start, Vertex end, double w){
		ArrayList<Vertex> vertexes = new ArrayList<>();
		for (Vertex v : verts){
			vertexes.add(v);
		}
		return branchBound(start,end, vertexes, w);
	}

	public Tour branchBound(Vertex start, Vertex end, ArrayList<Vertex> verts){
		return branchBound(start, end, verts, 1);
	}

	public double getLowerBound(Vertex[] vertexes, Vertex start){
		ArrayList<Vertex> verts = new ArrayList<>();
		for (Vertex v : vertexes){
			verts.add(v);
		}

		optimalReached = false;

		ArrayList<Vertex> C = new ArrayList<>();
		C.add(start);

		lbDistMatrix = new double[verts.size()][verts.size()];
		lbMatrixMap = new HashMap<>();
		for (int i = 0; i < verts.size(); i++){
			lbMatrixMap.put(verts.get(i), (Integer) i);
			for (int j = 0; j < verts.size(); j++){
				lbDistMatrix[i][j] = Double.MAX_VALUE;
			}
		}
		lbMatrixMap.put(start, verts.size());
		lbMatrixMap.put(start, verts.size()+1);

		startWeight = 1;
		Node root = new Node(C, verts, start);
		return root.lowerBound;
	}

	//gets the branch and bound tour
	public Tour branchBound(Vertex start, Vertex end, ArrayList<Vertex> verts, double addWeight){
		startWeight = addWeight;

		optimalReached = false;

		startT = getCpuTime();

		//create original constraint of only end vertex
		ArrayList<Vertex> C = new ArrayList<>();
		C.add(end);

		//make sure start or end are not in the vertexes
		verts.remove(start);
		verts.remove(end);

		//every time a child is created using one of these edges it recalcs
		//TODO add where it recalcs these when a tour is found using lb technique
		lbDistMatrix = new double[verts.size()][verts.size()];
		lbMatrixMap = new HashMap<>();
		for (int i = 0; i < verts.size(); i++){
			lbMatrixMap.put(verts.get(i), (Integer) i);
			for (int j = 0; j < verts.size(); j++){
				lbDistMatrix[i][j] = Double.MAX_VALUE;
			}
		}
		lbMatrixMap.put(start, verts.size());
		lbMatrixMap.put(end, verts.size()+1);

		edgesFound = new double[verts.size()][verts.size()];

		//create initial tour from verts with start and end.
		getInitTour(start, end, verts);

		if (getCpuTime() - startT > TIMELIMIT){
			return best;
		}

		//create root
		Node root = new Node(C, verts, start);
		//explore root
		optimalReached = root.explore();

		return best;
	}

	/*
	Set the initial tour to the best tour
	 */
	private void getInitTour(Vertex start, Vertex end, ArrayList<Vertex> verts){
		ArrayList<Vertex> order = new ArrayList<>();
		ArrayList<Vertex> remaining = new ArrayList<>(verts);
		ArrayList<Vertex> all = new ArrayList<Vertex>(verts);
		all.add(start);
		ArrayList<Edge> mst = findMinTree(all);

		double mstDist = 0;
		for (Edge e : mst){
			mstDist += e.dist;
		}

		double currentWeight = startWeight;
		order.add(start);

		while(!remaining.isEmpty()){
			double minVal = -1;
			Vertex minVert = null;
			for (Vertex v : remaining){
				double val = currentWeight * order.get(order.size()-1).getDistanceTo(v) + (currentWeight + v.weight()) * mstDist;
				if (minVert == null || val < minVal){
					minVal = val;
					minVert = v;
				}
			}

			order.add(minVert);
			remaining.remove(minVert);

			Edge max = null;
			for (Edge e : mst){
				if (e.contains(minVert)){
					ArrayList<Edge> tempMst = new ArrayList<Edge>(mst);
					tempMst.remove(e);

					for (Vertex v : all){
						v.visited = false;
					}

					if (dfs(tempMst, e.other(minVert), order) && (max == null || e.dist > max.dist)){
						max = e;
					}
				}
			}

			mstDist-= max.dist;
			mst.remove(max);
		}
		order.add(end);
		best = new Tour(order, startWeight);
		if (order.size() <= 30){
			best.SlowInsertOpt();
			best.SlowTwoOpt();
		}
		
	}

	private class Node implements Comparable<Node>{
		//the lower bound
		private double lowerBound;
		//the constraint for this node (starts at end goes backwards)
		private ArrayList<Vertex> C;
		//the remaining vertexes that arent part of the constraint
		private ArrayList<Vertex> R;
		//the starting vertex
		private Vertex start;

		private boolean complete;

		/*
		Create a node from the constraint, remainder start and matrix and finds the lower bound
		 */
		public Node(ArrayList<Vertex> C, ArrayList<Vertex> R, Vertex start){
			nodesVisited ++;
			this.C = new ArrayList<>(C);
			this.R = new ArrayList<>(R);
			this.start = start;

			complete = false;

			detLB();

			if (best != null && lowerBound > best.work()){
				complete = true;
			}
		}

		/*
		Explores the node until it is cut or all children explored.
		 */
		public boolean explore(){
			if (getCpuTime() - startT > TIMELIMIT){
				return false;
			}
			if (complete) return true;

			//if the R is empty, the tree has been fully explore.
			//Check completed tour
			if (R.isEmpty()){
				//add start to order
				ArrayList<Vertex> order = new ArrayList<>();
				order.add(start);
				//reverse constraint order and add
				for (int i = C.size()-1; i >= 0; i--){
					order.add(C.get(i));
				}
				//creates test and check
				Tour test = new Tour(order);
				
				if (best == null || test.work() < best.work()){
					best = test;
				}
				
				complete = true;

				return true;
			}

			//create children queue
			PriorityQueue<Node> children = new PriorityQueue<>();
			for (Vertex v : R){
				if (getCpuTime() - startT > TIMELIMIT){
					return false;
				}
				//get C and R by removing v from r and adding to c
				ArrayList<Vertex> newC = new ArrayList<>(C);
				ArrayList<Vertex> newR = new ArrayList<>(R);
				newC.add(v);
				newR.remove(v);

				if (newC.size() > 3 && newC.size() <= 6){
					ArrayList<Vertex> optC = getOptimalConstraint(newC, newR, start);
					if (optC.equals(newC)){
						//create the child
						Node newNode = new Node(newC, newR, start);
						children.add(newNode);	

						if (newC.size() > 2){
							Vertex u = newC.get(newC.size()-2);
							double lbDif = newNode.lowerBound - lowerBound;
							int vPos = lbMatrixMap.get(v);
							int uPos = lbMatrixMap.get(u);
							if (lbDif < lbDistMatrix[vPos][uPos] && lbDif >= 0){
								lbDistMatrix[vPos][uPos] = lbDif;
								lbDistMatrix[uPos][vPos] = lbDif;
							}
							edgesFound[vPos][uPos]++;
							edgesFound[uPos][vPos]++;
						}
					} else {
						numCuts++;
					}
				} else {
					//create the child
					Node newNode = new Node(newC, newR, start);
					children.add(newNode);
					if (newC.size() > 2){
							Vertex u = newC.get(newC.size()-2);
							double lbDif = newNode.lowerBound - lowerBound;
							int vPos = lbMatrixMap.get(v);
							int uPos = lbMatrixMap.get(u);
							if (lbDif < lbDistMatrix[vPos][uPos] && lbDif >= 0){
								lbDistMatrix[vPos][uPos] = lbDif;
								lbDistMatrix[uPos][vPos] = lbDif;
							}
							edgesFound[vPos][uPos]++;
							edgesFound[uPos][vPos]++;
						}
				}		
			}

			boolean res = true;
			//go through children, exploring those above branch and bound value
			while(!children.isEmpty()){
				if (getCpuTime() - startT > TIMELIMIT){
					return false;
				}
				//pull the best lb child
				Node node = children.remove();

				if (node.lowerBound > best.work()){
					node.complete = true;
				}
				//if lower bound is better, explore, else cut
				if (!node.complete){
					res = node.explore();
				} else {
					numCuts++;
				}
			}

			return res;
		}

		private ArrayList<Vertex> getOptimalConstraint(ArrayList<Vertex> newC, ArrayList<Vertex> newR, Vertex start){
			double weight = startWeight;
			for (Vertex v : newR){
				weight += v.weight();
			}

			ArrayList<Vertex> toVisit = new ArrayList<>(newC);
			Vertex s = toVisit.remove(toVisit.size()-1);
			Vertex e = toVisit.remove(0);
			


			Tour optimal = BruteForce.bruteForce(s, e, toVisit, weight + s.weight());

			ArrayList<Vertex> order = new ArrayList<>();
			for (int i = optimal.order().size()-1; i >= 0; i--){
				order.add(optimal.order().get(i));
			}

			return order;
		}

		private Edge findMin(ArrayList<Vertex> X, Vertex u){
			Edge min = null;
			for (Vertex v : X){
				Edge test = new Edge(v, u, v.getDistanceTo(u));
				if (min == null || test.dist < min.dist){
					min = test;
				}
			}
			return min;
		}


		/*
		Determines the lower bound of the node
		 */
		private void detLB(){
			//if there are no remainder, return as it is fully explored
			if (R.isEmpty()){
				return;
			}

			//reset lower bound
			lowerBound = 0;

			//create X and S
			ArrayList<Vertex> X = new ArrayList<>();

			//create XS
			ArrayList<Vertex> XS = new ArrayList<>(R);

			//set currentWeight to 0
			double currentWeight = startWeight;

			double visitDistLeft = 0;
			//increase current weight to weight of all vertexes in xz
			for (Vertex v : XS){
				currentWeight += v.weight();
				visitDistLeft += v.visitDist;
			}

			//add start to X and XS
			X.add(start);
			XS.add(start);

			//add constraint work to lower bound
			for (int i = C.size()-1; i > 0; i--){
				lowerBound += C.get(i).visitDist * currentWeight;
				//increase current weight
				currentWeight += C.get(i).weight();
				//add work of traveling constraint to lower bound
				lowerBound += C.get(i).dist(C.get(i-1)) * currentWeight;
			}

			//reset currentWeight to initial weight
			currentWeight = startWeight;

			//get the minimum spanning tree.
			ArrayList<Edge> minTree = findMinTree(XS);

			double mstDist = 0;
			for (Edge e : minTree){
				mstDist+= e.dist;
			}

			ArrayList<Edge> edgesUsed = new ArrayList<>();

			//NOTE this used to happen in the while loop but i think it works outside
			//Should still check.
			for (Edge e : minTree){
				ArrayList<Edge> tempMinTree = new ArrayList<>(minTree);
				Vertex vert;
				tempMinTree.remove(e);

				//mark vertexes as false
				for (Vertex v : XS){
					v.visited = false;
				}

				//check which vertex should be added to x by which one can find x without edge
				if (dfs(tempMinTree, e.one, X)){
					vert = e.two;
				} else {
					vert = e.one;
				}

				e.independentVert = vert;
			}

			//while the min tree is not empty
			while (!minTree.isEmpty()){
				Edge min = null;
				Vertex minVert = null;
				double minBound = -1;

				//find the edge with the minimum bound
				for (Edge e : minTree){
			
					//get bound calc
					double bound = currentWeight * (e.dist + e.independentVert.visitDist)+ (e.independentVert.weight()+currentWeight)*(mstDist - e.dist + visitDistLeft - e.independentVert.visitDist);
					//check if less than.
					if (min == null || bound < minBound){
						minBound = bound;
						min = e;
						minVert = e.independentVert;
					}
				}

				//add min to edges used
				edgesUsed.add(min);

				//remove min edge and update dist
				minTree.remove(min);

				//recalc mstDist
				mstDist -= min.dist;
				
				//add vert to x
				X.add(minVert);

				//increase lower bound and current weight
				lowerBound += currentWeight * min.dist + currentWeight * minVert.visitDist;
				visitDistLeft -= minVert.visitDist;
				currentWeight += minVert.weight();
			}

			//find edge going from X to the last value of the constraint. Not including start
			X.remove(start);
			double min = -1;
			Edge minEdge = null;
			for (Vertex v : X){
				double val = v.dist(C.get(C.size()-1)) * currentWeight;
				if (min == -1 || val < min){
					minEdge = new Edge(v, C.get(C.size()-1), v.dist(C.get(C.size()-1)));
					min = val;
				}
			}
			edgesUsed.add(minEdge);
			lowerBound += min;

			Tour test = isValid(edgesUsed, start, C);
			if (test != null){
				complete = true;
				
				if (best == null || test.work() < best.work()){
					best = test;
				}
			}
		}

		private Tour isValid(ArrayList<Edge> edges, Vertex start, ArrayList<Vertex> constraint){
			ArrayList<Vertex> tour = new ArrayList<>();
			Vertex current = start;
			for (Edge e : edges){
				if (e.contains(current)){
					tour.add(current);
					current = e.other(current);
				} else {
					return null;
				}
			}

			for (int i = constraint.size()-1; i >= 0; i--){
				tour.add(constraint.get(i));
			}

			return new Tour(tour);
		}

		/*
		Compare to another node based on lowerBound
		 */
		public int compareTo(Node other){
			return (int)(lowerBound - other.lowerBound);
		}

		/*
		Returns the string
		 */
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Constraint : ");
			sb.append('\n');
			sb.append("Lower Bound : ");
			sb.append(lowerBound);
			return sb.toString();
		}

	}

	private boolean dfs(ArrayList<Edge> minTree, Vertex start, ArrayList<Vertex> target){
			if (target.contains(start)) return true;

			start.visited = true;

			for (Edge e : minTree){
				if (e.contains(start)){
					Vertex u = e.other(start);
					if (!u.visited){
						if (dfs(minTree, u, target)){
							return true;
						} 
					}
				}
			}
			return false;
		}

		private ArrayList<Edge> findMinTree(ArrayList<Vertex> verts){
			ArrayList<Edge> res = new ArrayList<>();

			DisjointSets ds = new DisjointSets(lbMatrixMap.keySet().size() +1);
			ArrayList<Edge> E = getSortedEdges(verts);

			for (Edge e : E){
				if (ds.find(lbMatrixMap.get(e.one)) != ds.find(lbMatrixMap.get(e.two))){
					res.add(e);
					ds.union(lbMatrixMap.get(e.one), lbMatrixMap.get(e.two));
				}
			}
			return res;
		}

		public static ArrayList<Edge> staticFindMinTree(ArrayList<Vertex> verts){
			ArrayList<Edge> res = new ArrayList<>();
			DisjointSets ds = new DisjointSets(verts.size());
			Map<Vertex, Integer> posMap = new HashMap<>();
			for (int i = 0; i < verts.size(); i++){
				posMap.put(verts.get(i), (Integer)i);
			}
			ArrayList<Edge> E = getSortedEdges(verts);

			for (Edge e : E){
				if (ds.find(posMap.get(e.one)) != ds.find(posMap.get(e.two))){
					res.add(e);
					ds.union(posMap.get(e.one), posMap.get(e.two));
				}
			}

			return res;

		}

		private static ArrayList<Edge> getSortedEdges(ArrayList<Vertex> V){
			ArrayList<Edge> E = new ArrayList<Edge>();
			for (int i = 0; i < V.size(); i++){
				for (int j = i+1; j < V.size(); j++){
					Vertex u = V.get(i);
					Vertex v = V.get(j);
					E.add(new Edge(u, v, u.getDistanceTo(v)));
				}
			}

			Collections.sort(E);
			return E;
		}

		/*
		A disjoint set for kruskals to find minimum spanning tree
		 */
		public static class DisjointSets{
			/* put n new isolated nodes with ids 0...n-1 in a private array */
			private DisjointSets(int n) {
				parentOf = new int[n];
				rankOf = new int[n];
				for (int i=0; i<n; i++) {
					parentOf[i] = i;
				}
			}
			
			/* given one node ID, return the ID of its representative */
			public int find(int i) {
				while (parentOf[i]!=i) {
					i = parentOf[i];
				}
				return i;
			}

			/* given two node IDs, update data structure to reflect they are now connected */
			public void union(int i, int j) {
				int ri = find(i);
				int rj = find(j);
				//check if same representative
				if (ri == rj) {
					return;
				}
				//capare ranks of representatives, add lower to higher
				if (rankOf[ri] > rankOf[rj]){
					parentOf[rj] = ri;
				} else {
					parentOf[ri] = rj;

					//if rank is same, increase rank.
					if (rankOf[ri] == rankOf[rj]){
						rankOf[rj] = rankOf[ri]+1;
					}
				}
			}
			
			/* ivar to hold all the nodes */
			private int[] parentOf;
			//holds the rank of all nodes
			private int[] rankOf;
		}

		/** Get CPU time in miliseconds. */
        public static double getCpuTime( ) {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
            long val =  bean.isCurrentThreadCpuTimeSupported( ) ?
                    bean.getCurrentThreadCpuTime( ) : 0L;
            return val/1000000.0;
        }
}