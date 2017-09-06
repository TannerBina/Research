/*
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
*/
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.lang.management.*;

/**
 * Runs comparisons and algorithm on all data.
 * Created by Tanner on 11/10/2016.
 */


public class DataTest {

    //public static XSSFWorkbook workbook;
    //public static XSSFSheet sheet;

    public static final String[] setNames = new String[]{
           "wi29",
            "dj38",
            "qa194",
            "uy734",
            "zi929",
            "lu980",
            "rw1621",
            "mu1979",
            "nu3496",
            "ca4663",
            "tz6117",
            "eg7146",
            "ym7663",
            "pm8079",
            "ei8246",
            "ar9152",
            "ja9847",
            "gr9882"
    };

    public static void main(String[] args) throws IOException {
        //workbook = new XSSFWorkbook();
        //sheet = workbook.createSheet("BBResults");
        //makeHeader();

        int rowCount = 2;
        for (int i = 0; i < setNames.length; i++, rowCount++){
            inputData(setNames[i], rowCount);
        }


        //FileOutputStream os = new FileOutputStream("../data/SetResultsMultipleStartsBB.xlsx");
        //workbook.write(os);
    }

    public static final int NUMSTARTVALS = 20;

    private static void inputData(String fileName, int rowNumber) throws FileNotFoundException {
        //System.out.println("Starting " + fileName);

        Scanner s = new Scanner(new File("../pointSets/" + fileName + ".txt"));
        int size = s.nextInt();

        Vertex[] vertices = new Vertex[size];
        for (int i = 0; i < size; i++){
            vertices[i] = new Vertex((int)s.nextDouble(), (int)s.nextDouble(), i);
        }

        ArrayList<Vertex> tourOrder = loadTour("../pointSets/tspSolutions/" + fileName + ".txt", vertices);

        double timeInit = 0;
        double time = 0;
        double work = 0;
        double lkWork = 0;
        double oLkWork = 0;
        double greedyWork = 0;
        double oGreedyWork = 0;
        double greedyTime = 0;

        double[] timeArray = new double[NUMSTARTVALS];
        double[] workArray = new double[NUMSTARTVALS];
        double[] lkWorkArray = new double[NUMSTARTVALS];
        double[] oLkWorkArray = new double[NUMSTARTVALS];
        double[] greedyWorkArray = new double[NUMSTARTVALS];
        double[] oGreedyWorkArray = new double[NUMSTARTVALS];
        double[] greedyTimeArray = new double[NUMSTARTVALS];
        double[] lbArray = new double[NUMSTARTVALS];

        //analyzePointSet(vertices);

        
        System.out.print("Run ");

        
        for (int i = 0; i < NUMSTARTVALS; i++){
            System.out.print(i + ", ");
            int startVal = new Random().nextInt(vertices.length);

            startVal = i * (vertices.length-1)/20;

            Vertex start = vertices[startVal];
            Tour tsp = getTourStartingAt(start ,tourOrder);
            Vertex[] toVisit = new Vertex[vertices.length-1];
            int mod = 0;
            for (int j = 0; j < vertices.length; j++){
                if (j == startVal){
                    mod--;
                } else {
                    toVisit[j + mod] = vertices[j];
                }
            }

            //BranchBound bb = new BranchBound();
            //lbArray[i] = bb.getLowerBound(toVisit, start);
            
            timeInit = getCpuTime();
            
            
            Tour clustering = new Clustering(toVisit, start, start).getBest();
            clustering.InsertOptimization();
            clustering.TwoOptimization();
            timeArray[i] += (getCpuTime() - timeInit);
            workArray[i] += clustering.getWork();

            timeInit = getCpuTime();

            Tour greedy = getGreedyTour(start, toVisit);
            greedyWorkArray[i] += greedy.getWork();
            greedyTimeArray[i] += getCpuTime() - timeInit;
            greedy.InsertOptimization();
            greedy.TwoOptimization(); 
            oGreedyWorkArray[i] += greedy.getWork();
            
            lkWorkArray[i] += tsp.getWork();
            tsp.InsertOptimization();
            tsp.TwoOptimization();
            oLkWorkArray[i] += tsp.getWork();
        }
        //System.out.println();

        //System.out.println(getMean(lbArray));
        
        System.out.println();
        System.out.println();

        time = getMean(timeArray);
        work = getMean(workArray);
        lkWork = getMean(lkWorkArray);
        oLkWork = getMean(oLkWorkArray);
        greedyWork = getMean(greedyWorkArray);
        oGreedyWork = getMean(oGreedyWorkArray);
        greedyTime = getMean(greedyTimeArray);

        System.out.println("Clustering Time : " + time);
        System.out.println("Clustering Work : " + work);
        
        System.out.println();
        System.out.println("LK Time : NA");
        System.out.println("LK Work : " + lkWork);
        System.out.println("Optimized LK Work : " + oLkWork);
        System.out.println();
        System.out.println("Greedy Time : " + greedyTime);
        System.out.println("Greedy Work : " + greedyWork);
        System.out.println("Optimized Greedy Work : " + oGreedyWork);
        

        System.out.println("Finished " + fileName);
        System.out.println();
    }

    private static void analyzePointSet(Vertex[] vertexes){
        ArrayList<Vertex> verts = new ArrayList<>();
        for (Vertex v : vertexes){
            verts.add(v);
        }

        Cluster c = new Cluster(verts.get(0), 1);
        for (int i = 1; i < verts.size(); i++){
            c.add(verts.get(i));
        }

        double closestSum = 0;

        double minDist = Double.MAX_VALUE;
        double maxDist = -1;

        double distAvg = 0;

        for (Vertex v : verts){
            for (Vertex u : verts){
                if (v != u){
                    double dist = v.dist(u);
                    if (dist < minDist) minDist = dist;
                    if (dist > maxDist) maxDist = dist;
                    distAvg += dist;
                }
            }
        }

        distAvg/= (verts.size() * verts.size());

        double centDist = 0;
        for (Vertex v : verts){
            v.setClosestVertex(vertexes);
            closestSum += v.closestDist;

            centDist += c.getDistance(v);
        }
        c.calculateSpread();

        closestSum/=verts.size();
        centDist/=verts.size();

        System.out.println("MST : " + c.getSpread());
        System.out.println("CPA : " + closestSum + ", ");
        System.out.println("CDA : " + centDist + ", ");
        System.out.println("DR : " + (maxDist - minDist) + ", ");

        double closestPointVariance = 0;
        double centDistVariance = 0;
        double distVariance = 0;
        for (Vertex v : verts){
            closestPointVariance += Math.pow(v.closestDist - closestSum, 2);
            centDistVariance += Math.pow(c.getDistance(v) - centDist, 2);
            for (Vertex u : verts){
                distVariance += Math.pow(v.dist(u) - distAvg, 2);
            }
        }

        distVariance /= (verts.size() * verts.size());
        closestPointVariance /= verts.size();
        centDistVariance /= verts.size();

        System.out.println("CPV : " + closestPointVariance  + ", ");
        System.out.println("CDV : " + centDistVariance);  

        System.out.println("Dist Avg : " + distAvg);
        System.out.println("Dist Var : " + distVariance);
        System.out.println();
    }

    public static Tour getGreedyTour(Vertex start, Vertex[] verts){
        ArrayList<Vertex> left = new ArrayList<>();
        for (Vertex v : verts){
            left.add(v);
        }
        Vertex current = start;
        ArrayList<Vertex> order = new ArrayList<>();
        order.add(start);
        while(!left.isEmpty()){
            double minDist = Double.MAX_VALUE;
            Vertex min = null;
            for (Vertex v : left){
                if (v.dist(current) < minDist){
                    minDist = v.dist(current);
                    min = v;
                }
            }

            order.add(min);
            left.remove(min);
            current = min;
        }
        
        ArrayList<Vertex> reverseOrder = new ArrayList<>();
        for (Vertex v : order){
            reverseOrder.add(0, v);
        }
        reverseOrder.add(0, start);

        Tour reverse = new Tour(reverseOrder);

        return reverse;
    }

    private static Tour getTourStartingAt(Vertex p, ArrayList<Vertex> list){
        while(list.get(0) != p){
            Vertex rem = list.remove(0);
            list.add(rem);
        }
        Vertex[] tour = new Vertex[list.size()+1];
        for (int i = 0; i < list.size(); i++){
            tour[i] = list.get(i);
        }
        tour[tour.length-1] = list.get(0);
        Tour forward = new Tour(tour);

        Vertex[] tour2 = new Vertex[list.size()+1];

        int count = 0;
        for (int i = tour.length -1; i >= 0; i--, count++){
            tour2[count] = tour[i];
        }

        Tour backward = new Tour(tour2);

        if (forward.getWork() < backward.getWork()){
            return forward;
        }
        return backward;
    }

    public static double calcRange(Vertex[] vertexSet){
        double minX = vertexSet[0].getX();
        double maxX = vertexSet[0].getX();
        double minY = vertexSet[0].getY();
        double maxY = vertexSet[0].getY();
        for (int i = 0; i < vertexSet.length; i++){
            if (vertexSet[i].getX() < minX){
                minX = vertexSet[i].getX();
            }
            if (vertexSet[i].getX() > maxX){
                maxX = vertexSet[i].getX();
            }
            if (vertexSet[i].getY() < minY){
                minY = vertexSet[i].getY();
            }
            if (vertexSet[i].getY() > maxY){
                maxY = vertexSet[i].getY();
            }
        }
        double tot = (maxX-minX) + (maxY-minY);
        return (tot/2.0);
    }

    public static double getMean(double[] doubles){
        double sum = 0;
        for (double d : doubles){
            sum += d;
        }
        return (sum/(double)doubles.length);
    }

    public static double getMean(Vertex[] pointset){
        double sumX = 0;
        double sumY = 0;
        for (Vertex p : pointset){
            sumX += p.getX();
            sumY += p.getY();
        }
        return ((sumX/(double)pointset.length) + (sumY/(double)pointset.length))/2.0;
    }

    public static double getVariance(Vertex[] vertexSet){
        double mean = getMean(vertexSet);
        double temp = 0;
        for (Vertex p: vertexSet){
            temp+= Math.pow(((p.getX() + p.getY())/2.0-mean),2);
        }
        return temp/(double) vertexSet.length;
    }
    public static double getStdDev(Vertex[] vertexSet){
        return Math.sqrt(getVariance(vertexSet));
    }

    private static ArrayList<Vertex> loadTour(String name, Vertex[] vertices) throws FileNotFoundException {
        Scanner s = new Scanner(new File(name));
        ArrayList<Vertex> result = new ArrayList<>(vertices.length+1);
        result.add(vertices[s.nextInt()]);
        s.nextInt();
        s.nextDouble();
        for (int i = 0; i < vertices.length-1; i++){
            result.add(vertices[s.nextInt()]);
            s.nextInt();
            s.nextDouble();
        }
        return result;
    }

    /** Get CPU time in seconds. */
        public static double getCpuTime( ) {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
            long val =  bean.isCurrentThreadCpuTimeSupported( ) ?
                    bean.getCurrentThreadCpuTime( ) : 0L;
            return val/1000000000.0;
        }
}
/*
    private static void makeHeader(){
        Row headerRow = sheet.createRow(1);
        excelAdd(headerRow, SETNAME, "Set Name");
        excelAdd(headerRow, SETSIZE, "Set Size");
        excelAdd(headerRow, RANGE, "Set Range");
        excelAdd(headerRow, SETDEVIATION, "Set Deviation");
        excelAdd(headerRow, lkWork, "TSP Best Work");
        excelAdd(headerRow, TSPOPTIMIZEDWORK, "TSP O Work");
        excelAdd(headerRow, PREOWORK, "Pre O Work");
        excelAdd(headerRow, POSTOWORK, "Post O Work");
        excelAdd(headerRow, PREOEFFECIENCY, "Pre O Efficiency");
        excelAdd(headerRow, POSTOEFFECIENCY, "Post O Efficiency");
        excelAdd(headerRow, PREOTOOEFFECIENCY, "Pre O to O Efficiency");
        excelAdd(headerRow, POSTOTOOEFFECIENCY, "Post O to O Efficiency");
        excelAdd(headerRow, PREOTIME, "Pre O Time");
        excelAdd(headerRow, POSTOTIME, "Post O Time");
    }

    public static void excelAdd(Row row, int colNumber, Object data){
        Cell cell = row.createCell(colNumber);
        if (data instanceof String){
            cell.setCellValue((String) data);
        } else if (data instanceof  Integer){
            cell.setCellValue((Integer) data);
        } else if (data instanceof  Double){
            cell.setCellValue((Double)data);
        }
    }
}
*/