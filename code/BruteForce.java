import java.util.*;

public class BruteForce{
	//stores the best found tour so far.
	private static Tour best;
	private static Vertex end;
	private static double startWeight;

	public static Tour bruteForce(Vertex[] verts, Vertex start, Vertex end, int w){
		ArrayList<Vertex> used = new ArrayList<>();
		for (int i = 0; i < verts.length; i++){
			used.add(verts[i]);
		}
		return bruteForce(start, end, used, w);
	}

	/*
	Solve using brute force return the result.
	Can be used for up to 10 vertexes
	 */
	public static Tour bruteForce(Vertex start, Vertex end, ArrayList<Vertex> vertexes, double startWeight){
		BruteForce.startWeight = startWeight;
		//setttp matrix and verts and best
		ArrayList<Vertex> verts = new ArrayList<>(vertexes);
		verts.remove(start);
		verts.remove(end);
		best = null;

		//add start to order, set end
		ArrayList<Vertex> order = new ArrayList<>();
		order.add(start);
		BruteForce.end = end;

		//call recursive method
		bruteForce(new ArrayList<Vertex>(order), new ArrayList<Vertex>(verts));

		return best;
	}


	/*
	Recursive method which explore every possible tour and saves the best in best
	 */
	private static void bruteForce(ArrayList<Vertex> order, ArrayList<Vertex> toVisit){
		//base case if toVisit is empty add end and attempt
		if (toVisit.isEmpty()){
			order.add(end);
			Tour attempt = new Tour(order, startWeight);
			if (best == null || attempt.work() < best.work()){
				best = attempt;
			}
			return;
		}

		//new call for every vertex not yet visited.
		for (Vertex v : toVisit){
			ArrayList<Vertex> newToVisit = new ArrayList<>(toVisit);
			ArrayList<Vertex> newOrder = new ArrayList<>(order);
			newToVisit.remove(v);
			newOrder.add(v);
			bruteForce(newOrder, newToVisit);
		}
	}
}