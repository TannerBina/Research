public class Edge implements Comparable<Edge>{

	public Vertex one;
	public Vertex two;
	public double dist;

	public Vertex independentVert;

	public Edge(Vertex one, Vertex two, double dist){
		this.one = one;
		this.two = two;
		this.dist = dist;
	}

	public int compareTo(Edge other){
		return Double.compare(dist, other.dist);
	}

	public boolean equals(Edge other){
		if (one == other.one && two == other.two) return true;
		if (one == other.two && two == other.one) return true;
		return false;
	}

	public boolean contains(Vertex v){
		if (one == v || two == v) return true;
		return false;
	}

	public Vertex other(Vertex v){
		if (one == v) return two;
		if (two == v) return one;
		return null;
	}
}