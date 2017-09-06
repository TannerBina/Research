import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class TourCreate {
	public static void main(String[] args) {
		int numTrsGen = 1;
		int pntsPerTr = 1000;
		int range = 1000;
		try {
			PrintWriter pw = new PrintWriter(new File("../pointSets/test1000.txt"));
			for (int i = 0; i < numTrsGen; i++) {
				//System.out.println(i);
				Random r= new Random();
				pw.println(pntsPerTr);
				for (int j = 0; j < pntsPerTr; j++){
					pw.print(r.nextInt(range));
					pw.print(" ");
					pw.println(r.nextInt(range));
					//pw.print(" ");
					//pw.println(j);
				}
				pw.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
