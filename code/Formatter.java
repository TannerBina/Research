import java.io.*;
import java.util.Scanner;

/**
 * Formats a Tsp point set
 * Created by Tanner on 10/27/2016.
 */
public class Formatter {


    public static void main(String[] args) throws IOException {
        File file = new File("../pointSets/" + args[0] + ".txt");
        Scanner s= new Scanner(file);

        int setSize = s.nextInt();
        double[] xs = new double[setSize];
        double[] ys = new double[setSize];

        for (int i = 0; i < setSize; i++){
            s.next();
            xs[i] = s.nextDouble();
            ys[i] = s.nextDouble();
        }
        s.close();

        PrintWriter fw = new PrintWriter("../pointSets/" + args[0] + ".txt");
        fw.println(setSize);
        for (int i = 0; i < setSize; i++){
            fw.print(xs[i]);
            fw.print(" ");
            fw.println(ys[i]);
        }

        fw.close();
    }
}
