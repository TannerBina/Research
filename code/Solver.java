/*
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
*/
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Brute force solving of a traveling salesman problem.
 * Solving time averages around 1 second for 11 cities,
 * 9 seconds for 12 cities
 * Time increases greatly after that.
 * @author Tanner
 *
 */
public class Solver {

    public static final boolean SAVETOURS = false;
	
	//the saved best tour
	private Tour best = null;

	private Vertex end;

    public ArrayList<Tour> tourList;

    

	/**
	 * Constructor for a solver,
	 * enter all pnts to be visited,
	 * start and end locations
	 * @param vertices
	 * @param start
	 * @param end
	 */
	public Solver(Vertex[] vertices, Vertex start, Vertex end){
		this.end = end;
		
		//create original arrayList of everything being left
		ArrayList<Vertex> pointsLeft = new ArrayList<>();
		for (int i = 0; i < vertices.length; i++){
			pointsLeft.add(vertices[i]);
		}

		if (SAVETOURS)
		    tourList = new ArrayList<>(factorial(vertices.length+1));
		
		//sets an initial start best tour.
		best = new Tour(vertices, start, end);
		
		//begins the recursive solve process
		solve(new ArrayList<Vertex>(), start, pointsLeft, 0, 1);

        if (SAVETOURS){
            System.out.println(tourList.size());
            //writeTours();
        }
	}

/*
	private void writeTours(){
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        Row firstRow = sheet.createRow(1);
        Row secondRow = sheet.createRow(2);

        for (int i = 0; i < tourList.size(); i++){
            DataTest.excelAdd(firstRow, i, i);
            DataTest.excelAdd(secondRow, i, tourList.get(i).getWork());
        }

        FileOutputStream os = null;
        try {
            os = new FileOutputStream("../data/AllTours.xlsx");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

	public static int factorial(int a){
        int result = 1;
        for (int i = a; i > 0; i--){
            result *= a;
        }
        return result;
    }
	
	/**
	 * Retursn the best tour generated
	 * @return
	 */
	public Tour getBest(){
		return best;
	}
	
	/**
	 * Solves for the best tour,
	 * recursively class,
	 * arrayList of visited cities,
	 * current city,
	 * cities left to be visited,
	 * wrk done so far,
	 * weight currently carried
	 * @param visited
	 * @param current
	 * @param pointsLeft
	 * @param work
	 * @param weight
	 */
	private void solve(ArrayList<Vertex> visited, Vertex current, ArrayList<Vertex> pointsLeft, double work, double weight){
		//if the current tour has done more than best stop
		if (!SAVETOURS && work > best.getWork()){
			return;
		}
		
		//copy arraylist
		ArrayList<Vertex> newVisited = new ArrayList<>(visited);
		
		//if left is empty, completes tour and checks to see if
		//the newly completed tour is better than the best
		if (pointsLeft.isEmpty()){
			//adds the last city to the tour
			double newWork = work + (current.getDistanceTo(end)*weight);
			newVisited.add(current);
			newVisited.add(end);
			
			//exits recursive loop, sets best = to new if better
			if (! SAVETOURS &&best == null || newWork < best.getWork()){
				best = new Tour(newVisited);
				return;
			} else {
                if (SAVETOURS){
                    tourList.add(new Tour(newVisited));
                }
				return;
			}
		}
		
		//adds the current to the visited
		newVisited.add(current);
		
		//recursively calls solvers to go to ever
		//city next
		for (int i = 0; i < pointsLeft.size(); i++){
			ArrayList<Vertex> newPointsLeft = new ArrayList<>(pointsLeft);
			Vertex next = newPointsLeft.remove(i);
			double newWrk = work + (current.getDistanceTo(next)*weight);
			//System.out.println("Patial " + new Tour(newVstd));
				solve(newVisited, next, newPointsLeft, newWrk, weight + next.getWeight());
		}
	}
}
