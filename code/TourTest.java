import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

/**
 * Testing Class
 * 
 * @author Tanner
 *
 */
public class TourTest {

	public static final boolean SCNR = true;

	public static void main(String[] args) throws FileNotFoundException {

		int numPnts = 100;
		int numTrs = 50;
		int range = 1000;

		double totWrkPreO = 0;
		double totWrkPostO = 0;

		double totTimePreO = 0;
		double totTimePostO = 0;
		long firstT = System.currentTimeMillis();

		Random r = new Random();
		Scanner s = new Scanner(new File("../pointSets/test.txt"));

		for (int i = 0; i < numTrs; i++) {
			System.out.println(i);
			long startT = System.currentTimeMillis();
			
			Vertex strt = null;

			Vertex[] vertices = new Vertex[numPnts];

			if (!SCNR) {
				strt = new Vertex(r.nextInt(range), r.nextInt(range), 0);
				for (int j = 0; j < numPnts; j++) {
					vertices[j] = new Vertex(r.nextInt(range), r.nextInt(range), j + 1);
				}
			} else {
				strt = new Vertex(s.nextInt(), s.nextInt(), 0);
				for (int j = 0; j < numPnts; j++){
					vertices[j] = new Vertex(s.nextInt(), s.nextInt(), j+1);
				}
			}
			Tour crnt = new Clustering(vertices, strt, strt).getBest();
			totTimePreO += (System.currentTimeMillis() - startT);
			totWrkPreO += crnt.getWork();
			crnt.InsertOptimization();
			crnt.TwoOptimization();
			totTimePostO += (System.currentTimeMillis() - startT);
			totWrkPostO += crnt.getWork();
		}
		s.close();
		
		double avgTimePreO = totTimePreO / numTrs;
		double avgTimePostO = totTimePostO / numTrs;
		double avgWrkPreO = totWrkPreO / numTrs;
		double avgWrkPostO = totWrkPostO / numTrs;

		long fullTime = System.currentTimeMillis() - firstT;

		System.out.println("Ran " + numTrs + " tours of " + numPnts + " points over range of " + range + ".");
		System.out.println("Took a total of " + fullTime / 1000 + " seconds.");
		System.out.println("AverageTimePreO: " + avgTimePreO);
		System.out.println("AverageTimePostO: " + avgTimePostO);
		System.out.println("AverageWorkPreO: " + avgWrkPreO);
		System.out.println("AverageWorkPostO: " + avgWrkPostO);
	}
}
