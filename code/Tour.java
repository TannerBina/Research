import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class Tour implements Comparable {

	// full order including start and end points;
	private Vertex[] order;

	// average distance to closest point
	private double averageClosestDistance;

	public double work;
	private double distance;
	private double startWeight = 1;

	/**
	 * Creates a tour from an array of the points visited other than the start
	 * and end and the strt and end entered seperately
	 * 
	 * @param order
	 * @param start
	 * @param end
	 */
	public Tour(Vertex[] order, Vertex start, Vertex end) {
		this.order = new Vertex[order.length + 2];
		this.order[0] = start;
		for (int i = 0; i < order.length; i++) {
			this.order[i + 1] = order[i];
		}
		this.order[order.length + 1] = end;
		// sets the total work and distance of the tour
		setTotalWork();
	}

	/**
	 * Creates a tour from the full set of points
	 * 
	 * @param order
	 */
	public Tour(Vertex[] order) {
		this.order = order;
		setTotalWork();
	}

	/**
	 * Creates a tour from the full set of points Sets the start weight to the
	 * specified int
	 * 
	 * @param order
	 */
	public Tour(Vertex[] order, double startWeight) {
		this.startWeight = startWeight + this.startWeight - 1;
		this.order = order;
		setTotalWork();
	}

	public Tour(ArrayList<Vertex> vertices) {
		order = new Vertex[vertices.size()];
		for (int i = 0; i < vertices.size(); i++) {
			order[i] = vertices.get(i);
		}
		setTotalWork();
	}

	public Tour(ArrayList<Vertex> vertices, double startWeight) {
		this.startWeight = startWeight + this.startWeight - 1;
		order = new Vertex[vertices.size()];
		for (int i = 0; i < vertices.size(); i++) {
			order[i] = vertices.get(i);
		}
		setTotalWork();
	}

	/**
	 * Returns the difference in the work of the two tours
	 */
	public int compareTo(Object a) {
		Tour other = (Tour) a;
		return (int) work - (int) other.work;
	}

	/**
	 * Returns the work
	 * 
	 * @return
	 */
	public double getWork() {
		return work;
	}

	/**
	 * Returns the distance;
	 * 
	 * @return
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * Returns the order array of towns. including start and end.
	 * 
	 * @return
	 */
	public Vertex[] getArray() {
		return order;
	}

	/**
	 * Sets the total work of the tour. n effeciency
	 */
	private void setTotalWork() {
		work = 0;
		distance = 0;
		double calculateWeight = startWeight;
		for (int i = 0; i < order.length - 1; i++) {
			double dis = order[i].getDistanceTo(order[i + 1]);
			// adds the distance to next city to tot
			distance += dis;
			distance += order[i+1].visitDist;
			// work = dis * current weight
			work += dis * calculateWeight;
			work += order[i+1].visitDist * calculateWeight;
			// if variable weight adds weight of nxt city
			// else adds 1
            calculateWeight += order[i + 1].getWeight();
		}
	}

	/**
	 * Sets the average distance between points
	 */
	private void setAverageClosestDistance() {
		for (int i = 0; i < order.length; i++) {
			order[i].setClosestVertex(order);
		}
		averageClosestDistance = 0;
		for (int i = 0; i < order.length; i++) {
			averageClosestDistance += order[i].getDistanceTo(order[i].getClosestVertex());
		}
		averageClosestDistance = averageClosestDistance / (double) order.length;
	}

	/**
	 * Calls Insert Opt until the optimization no longer increases tour
	 * effeciency by over 1%
	 * 
	 * Optimizes by inserting points into different positions in the tour.
	 * approaches the local optimal.
	 * 
	 * n^2 efficiency for each insert opt call.
	 */
	public void InsertOptimization() {
		setAverageClosestDistance();
		while (InsertOptimizationChange() > .01) {
		}
	}

	/**
	 * Optimizes by inserting points into different positions in the tour n^2
	 * efficiency.
	 * 
	 * @return returns the percentage decrease in tour.
	 */
	private double InsertOptimizationChange() {

		double originalWork = work;
		for (int i = 1; i < order.length - 1; i++) {
			if (order[i].getDistanceTo(order[i].getClosestVertex()) > averageClosestDistance) {
				// keeps track of before and after values.
				double beforeInsertWork = 0;
				double afterInsertWork = 0;

				// keeps track of distance and work in the values in
				// between the insers
				double betweenInsertWork = 0;
				double betweenInsertDistance = 0;

				// sets the before insert work to 0,
				// j starts at 1, so before is always 0
				beforeInsertWork = 0;

				// creates the array of all points after the thing to inserted,
				// i
				// size will be the length of order array - i - 1
				Vertex[] afterArray = new Vertex[order.length - i - 1];
				System.arraycopy(order, i + 1, afterArray, 0, afterArray.length);

				// sets the work to the work of a tour through the points with
				// starting weight i+2 to account for the missed
				afterInsertWork = new Tour(afterArray, i + 1 + startWeight).getWork();

				// creates the mid array, from the point at index 1 (index 0 is
				// excluded
				// because j = 1) to the point at i, exclusive
				Vertex[] betweenArray = new Vertex[i - 1];
				System.arraycopy(order, 1, betweenArray, 0, betweenArray.length);

				// gets the tour of the middle, account for a strt weight of 2,
				// because frst
				// point is skipped
				Tour betweenTour = new Tour(betweenArray, 1 + startWeight);
				betweenInsertWork = betweenTour.getWork();
				betweenInsertDistance = betweenTour.getDistance();

				// loop over points
				for (int j = 1; j < order.length - 2; j++) {

					// checks to see if i = j
					if (i != j) {

						// sets the point to be inserted,
						Vertex insertVertex = order[i];
						// remembers the point on both sides
						Vertex beforeRemovalVertex = order[i - 1];
						Vertex afterRemovalVertex = order[i + 1];

						// calculates the original new work from bef and aft
						// unchanging work
						double newWork = beforeInsertWork + afterInsertWork;

						// switches between two changes based on if i > j or < j
						if (i > j) {
							// sets the points before and after insertPointAt
							Vertex beforeInsertVertex = order[j - 1];
							Vertex afterInsertVertex = order[j];

							// adds the middle work to the new work
							newWork += betweenInsertWork;
							// adds the middle dis to the new work because
							// section slides forward
							// one position in the tour
							newWork += betweenInsertDistance;

							// adds all the work required to link the broken
							// sections back togeter
							newWork += beforeInsertVertex.getDistanceTo(insertVertex) * (j + startWeight - 1);
							newWork += insertVertex.getDistanceTo(afterInsertVertex) * (j + startWeight);
							newWork += beforeRemovalVertex.getDistanceTo(afterRemovalVertex) * (i + startWeight);

							// subtract from mid because j moves towards i
							double distance = order[j].getDistanceTo(order[j + 1]);
							betweenInsertWork -= distance * (j + startWeight);
							betweenInsertDistance -= distance;

							// add to bef because j moves away from start
							beforeInsertWork += order[j - 1].getDistanceTo(order[j]) * (j + startWeight - 1);

							// if i < j
						} else {
							// sets before and after insertPointAt points, before is at
							// insert index
							Vertex beforeInsertVertex = order[j];
							Vertex afterInsertVertex = order[j + 1];

							// adds middle work to new work
							newWork += betweenInsertWork;
							// subtracts middle dis because section slides back
							// in tour
							newWork -= betweenInsertDistance;

							// adds the linking towns to new work for tour
							newWork += beforeInsertVertex.getDistanceTo(insertVertex) * (j + startWeight - 1);
							newWork += insertVertex.getDistanceTo(afterInsertVertex) * (j + startWeight);
							newWork += beforeRemovalVertex.getDistanceTo(afterRemovalVertex) * (i + startWeight - 1);

							// add to mid because middle increases as j gets
							// farther from i
							double distance = order[j].getDistanceTo(order[j + 1]);
							betweenInsertWork += distance * (j + startWeight);
							betweenInsertDistance += distance;

							// subtract from aft because j approaches end
							afterInsertWork -= order[j + 1].getDistanceTo(order[j + 2]) * (j + 1 + startWeight);
						}

						// changes the tour and exits j resets work
						if (newWork < work) {
							double guessedWork = newWork;
							order = insertPointAt(i, j, order);
							setTotalWork();
							j = order.length;
							if (guessedWork + 1 < work) {
								System.out.println("Insert Opt not working.");
								System.out.println("Guess work: " + guessedWork);
								System.out.println("Correct work: " + work);
							}
						}

						// if i = j
					} else {
						// resets mid work and dis to account for any mistakes
						// decreases the after insert section by one city as
						// the insert is now going toward that end
						afterInsertWork -= order[j + 1].getDistanceTo(order[j + 2]) * (j + 1 + startWeight);
						betweenInsertWork = 0;
						betweenInsertDistance = 0;
					}
				}
			}
		}

		// calculates percent change in work and returns it
		double finalWork = work;
		return 1 - (finalWork / originalWork);
	}

	/**
	 * Inserts the Vertex at the first index into the array at the second index,
	 * sliding all post points over. Returns the result
	 * 
	 * @param indexOfPoint
	 *            to be inserted.
	 * @param indexOfInsertion
	 *            to be inserted at
	 * @param array
	 *            the array
	 * @return
	 */
	private Vertex[] insertPointAt(int indexOfPoint, int indexOfInsertion, Vertex[] array) {
		// sets the point that needs to be inserted
		Vertex vertexToInsert = array[indexOfPoint];
		// creates resulting aray
		Vertex[] result = new Vertex[array.length];
		// controls whether or not things must be slid forward or back
		int mod = 0;
		for (int i = 0; i < array.length; i++) {
			// if not at index needs to inserted at
			if (i != indexOfInsertion) {
				/*
				 * if current pos is equal to what needs to be inserted the mod
				 * is increased. Therefore the position is skipped. For example
				 * if what needs to inserted is at 10 then res[10] = arry[11].
				 */
				if (array[i + mod] == vertexToInsert) {
					mod++;
				}
				result[i] = array[i + mod];
			} else {
				// removes any mod that exists and sets the current to
				// what needs to be inserted. if no mod, sets
				// res[10] == res[9] from now on.
				result[i] = vertexToInsert;
				mod--;
			}
		}
		return result;
	}

	/**
	 * Switches two points around in the order and checks to see if tour work is
	 * decreased.
	 */
	public void TwoOptimization() {
		//set average distance for points
		for (int i = 0; i < order.length; i++){
			order[i].saveAverageDistance(order);
		}

		// loops through two opt while it is still improving up to 100 times.
		while (TwoOptimizationChange() > .01) {
		}
	}

	/**
	 * Performs the two opt optimization. Returns true if tours were changed.
	 * False if tours were not.
	 * 
	 * @return
	 */
	private double TwoOptimizationChange() {
        double initialWork = work;
		int change = 0;
		for (int i = 1; i < order.length - 1; i++) {
			for (int j = 1; j < order.length - 1; j++) {
				//checks to see if points are near each other
				if (order[i].getDistanceTo(order[j]) < order[i].getAverageDistance()) {
					double newWork = work;
					// declare all points
					Vertex vertexBeforeOne = order[i - 1];
					Vertex vertexOne = order[i];
					Vertex vertexAfterOne = order[i + 1];
					Vertex vertexBeforeTwo = order[j - 1];
					Vertex vertexTwo = order[j];
					Vertex vertexAfterTwo = order[j + 1];
					// subtract work
					newWork -= (vertexBeforeOne.getDistanceTo(vertexOne) * i);
					newWork -= (vertexOne.getDistanceTo(vertexAfterOne) * (i + 1));
					newWork -= (vertexBeforeTwo.getDistanceTo(vertexTwo) * j);
					newWork -= (vertexTwo.getDistanceTo(vertexAfterTwo) * (j + 1));

					if (i == j - 1) {
						vertexAfterOne = vertexOne;
						vertexBeforeTwo = vertexTwo;
					} else if (i == j + 1) {
						vertexBeforeOne = vertexOne;
						vertexAfterTwo = vertexTwo;
					}
					// add Wrok
					newWork += (vertexBeforeOne.getDistanceTo(vertexTwo) * i);
					newWork += (vertexTwo.getDistanceTo(vertexAfterOne) * (i + 1));
					newWork += (vertexBeforeTwo.getDistanceTo(vertexOne) * j);
					newWork += (vertexOne.getDistanceTo(vertexAfterTwo) * (j + 1));

					if (newWork < work) {
						swap(i, j, order);
						setTotalWork();
						change++;
					}
				}
			}
		}
		return 1 - (work / initialWork);
	}

	/**
	 * Swaps two positions in an array
	 * 
	 * @param frst
	 * @param scnd
	 * @param arry
	 */
	private void swap(int frst, int scnd, Vertex[] arry) {
		Vertex tmp = arry[frst];
		arry[frst] = arry[scnd];
		arry[scnd] = tmp;
	}

	private Vertex[] swapReturn(int frst, int scnd, Vertex[] arry){
		Vertex[] verts = new Vertex[arry.length];
		for (int i = 0; i < arry.length; i++){
			if (i == frst){
				verts[i] = arry[scnd];
			}
			else if (i == scnd){
				verts[i] = arry[frst];
			}
			else {
				verts[i] = arry[i];
			}
		}
		return verts;
	}

	/**
	 * Returns a String which prints out all tour data
	 */
	public String toString() {
		String res = new String();
		res += "Tour Work : ";
		res += work;
		res += "\n";
        //TODO uncomment
		/*res += "Tour Distance : ";
		res += distance;
		res += "\n";*/
		for (int i = 0; i < order.length; i++) {
			res += order[i].getID();
			res += ", ";
		}
		return res;
	}

	public double work(){return work;}

	public ArrayList<Vertex> order(){
		ArrayList<Vertex> ord = new ArrayList<>();
		for (Vertex v : order){
			ord.add(v);
		}
		return ord;
	}

		public void SlowInsertOpt(){
		while(SlowInsertOptChange() > .01){

		}
	}

	private double SlowInsertOptChange(){
		double initWork = work;

		for (int i = 1; i < order.length-1; i++){
			for (int j = 1; j < order.length-1; j++){
				if (i != j){
					Vertex[] testOrder = insertPointAt(i, j, order);
					Tour test = new Tour(testOrder, startWeight);
					if (test.work() < work){
						order = testOrder;
						setTotalWork();
					}
				}
			}
		}
		
		return (1 - work/ initWork);
	}

	public void SlowTwoOpt(){
		while(SlowTwoOptChange() > .01){

		}
	}

	private double SlowTwoOptChange(){
		double initWork = work;
		for (int i = 1; i < order.length - 1; i++){
			for (int j = 1; j < order.length -1; j++){
				if (i != j){
					Vertex[] testOrder = swapReturn(i, j, order);
					Tour test = new Tour(testOrder, startWeight);
					if (test.work() < work){
						order = testOrder;
						setTotalWork();
					}
				}
			}
		}
		return (1 - work / initWork);
	}

	/*
	 * Genetic Algorithm Methods should go after this. Old ones are in the old
	 * TBP methods.
	 */
}
