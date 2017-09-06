
import java.util.*;

public class Cluster implements Comparable<Cluster> {

    // stores all the points which are part of the cluster
    private ArrayList<Vertex> content = new ArrayList<Vertex>();

    // the id of the cluster
    private int ID = 0;

    // the center points of the cluster
    private double x;
    private double y;
    private double z;

    // the calcualtion of how spread out the points are
    // in the cluster
    private double spread;

    public int tourPosition;

    private static final double CUTOFFSIZEPERCENT = .3;

    /**
     * Constructs a cluster which contains the specified point and the specified
     * id.
     *
     * @param a
     * @param id
     */
    public Cluster(Vertex a, int id) {
        ID = id;
        content.add(a);
        x = a.getX();
        y = a.getY();
        z = a.getAverageDistance();
    }

    public int compareTo(Cluster c){
        return tourPosition - c.tourPosition;
    }

    /**
     * Calculates the spread of the points in te cluster
     */
    public void calculateSpread() {
        ArrayList<Edge> minTree = BranchBound.staticFindMinTree(content);
        spread = 0;
        for (Edge e : minTree){
            spread += e.dist;
        }
    }

    /**
     * Returns the point at index i in the cluster
     *
     * @param i
     * @return
     */
    public Vertex get(int i) {
        return content.get(i);
    }

    /**
     * Adds a point to the cluster Does not recalc center or spread
     *
     * @param a
     */
    public void add(Vertex a) {
        content.add(a);

        double adjustFactor = (content.size()-1)/(double)content.size();

        x = x*adjustFactor + a.getX()/(double)content.size();
        y = y*adjustFactor + a.getY()/(double)content.size();
        z = z*adjustFactor + a.getAverageDistance()/(double)content.size();
    }

    /**
     * Removes a point from the cluster Does not recalc center or spread
     *
     * @param a
     */
    public void remove(Vertex a) {
        content.remove(a);

        double adjustFactor = (content.size()+1)/(double)content.size();

        x = x*adjustFactor + a.getX()/(double)content.size();
        y = y*adjustFactor + a.getY()/(double)content.size();
        z = z*adjustFactor + a.getAverageDistance()/(double)content.size();
    }

    /**
     * Returns the arraylist of points which the clsoter contains
     *
     * @return
     */
    public ArrayList<Vertex> getContent() {
        return content;
    }

    /**
     * gets the distance from the center of the cluster to a point
     *
     * @param a
     * @return
     */
    public double getDistance(Vertex a) {
        double xDif = x - a.getX();
        double yDif = y - a.getY();

        return Math.sqrt((Math.pow(xDif, 2) + Math.pow(yDif, 2)));
    }

    public double dist(Cluster a){
        return getDistance(a);
    }

    /**
     * Gets the distance from this cluster to another cluster uses centroid
     * distance between clusters currently
     *
     * @param a
     * @return
     */
    public double getDistance(Cluster a) {
        double xDif = x - a.x;
        double yDif = y - a.y;
        double zDif = z - a.z;

        return Math.sqrt((Math.pow(xDif, 2) + Math.pow(yDif, 2) + Math.pow(zDif, 2)));
    }

    /**
     * Returns the point in the entered cluster which is closest to any point in
     * this cluster
     *
     * @param a
     * @return
     */
    public Vertex getClosestPointIn(Cluster a) {
        double[][] dist = new double[content.size()][a.size()];
        for (int i = 0; i < content.size(); i++) {
            for (int j = 0; j < a.size(); j++) {
                dist[i][j] = content.get(i).getDistanceTo(a.get(j));
            }
        }
        int[] inds = getSmallestIn(dist);
        int jVal = inds[1];
        return a.get(jVal);
    }

    /**
     * Checks to see if the Cluster is equal to the point.
     *
     * @param a
     * @return
     */
    public boolean isEqualTo(Vertex a) {
        return a.getID() == ID;
    }

    /**
     * Returns the size of cluster
     *
     * @return
     */
    public int size() {
        return content.size();
    }

    public double getSpread(){
        return spread;
    }

    /**
     * Converts the cluster to a point represented by its centriod uses x, y
     * centriod and id. The weight of the point is determined by a function of
     * the size/spread
     *
     * @return
     */
    public Vertex toPoint() {
        calculateSpread();
        Vertex res = new Vertex((int) x, (int) y, ID, (content.size()));
        //return new Vertex((int)x, (int)y, ID, content.size()/spread);
        res.visitDist = spread;
        return res;
    }

    public int getID() {
        return ID;
    }

    public static ArrayList<Cluster> badClustering(Vertex[] vertices, int cutoff){
        double maxAvgDist = -1;
        double maxClsDist = -1;
        double minAvgDist = Double.MAX_VALUE;
        double minClsDist = Double.MAX_VALUE;

        for (Vertex v : vertices){
            v.saveAverageDistance(vertices);
            v.setClosestVertex(vertices);

            if (v.averageDistance > maxAvgDist){
                maxAvgDist = v.averageDistance;
            }
            if (v.averageDistance < minAvgDist){
                minAvgDist = v.averageDistance;
            }

            if (v.closestDist > maxClsDist){
                maxClsDist = v.closestDist;
            }
            if (v.closestDist < minClsDist){
                minClsDist = v.closestDist;
            }
        }

         ArrayList<Vertex> verts = new ArrayList<>();

        for (Vertex v : vertices){
            v.averageDistance = (v.averageDistance - minAvgDist)/(maxAvgDist - minAvgDist);
            v.closestDist = (v.closestDist - minClsDist)/(maxClsDist - minClsDist);
            verts.add(v);
        }

        Collections.sort(verts);

        double maxDist = -1;
        double minDist = Double.MAX_VALUE;
        double[][] newDistArray = new double[verts.size()][verts.size()];

        for (Vertex v : verts){
            for (Vertex u : verts){
                if (v != u){
                    double dist = v.dist(u);
                    if (dist > maxDist){
                        maxDist = dist;
                    }
                    if (dist < minDist){
                        minDist = dist;
                    }
                }
            }
        }

        for (int i = 0; i < verts.size(); i++){
            for (int j = 0; j < verts.size(); j++){
                if (i != j){
                    newDistArray[i][j] = (verts.get(i).dist(verts.get(j)) - minDist)/(maxDist-minDist);
                } else {
                    newDistArray[i][j] = Double.MAX_VALUE;
                }
            }
        }

        double[][] relativity = new double[verts.size()][verts.size()];
        for (int i = 0; i < verts.size(); i++){
            for (int j = 0; j < verts.size(); j++){
                if (i != j){
                    double a = (2*verts.size() - i -j)/(2*(double)verts.size());
                    double b = (i+j)/(double)verts.size();
                    double sqr1 = Math.pow(verts.get(i).averageDistance - verts.get(j).averageDistance, 2);
                    double sqr2 = Math.pow(verts.get(i).closestDist - verts.get(j).closestDist, 2);
                    double val = (sqr1 + sqr2)/2.0;
                    relativity[i][j] = a*newDistArray[i][j] + b * Math.sqrt(val);
                } else {
                    relativity[i][j] = Double.MAX_VALUE;
                }
            }
        }

        ArrayList<Cluster> res = new ArrayList<>();

        ArrayList<Vertex> left = new ArrayList<>(verts);

        for (int i = 0; i < cutoff; i++){
            res.add(new Cluster(left.remove(left.size()/cutoff), i));
        }

        while(!left.isEmpty()){
            Vertex v = left.remove(0);

            double minCDist = Double.MAX_VALUE;
            Cluster minClus = null;

            for (Cluster c : res){
                double avgDist = 0;
                for (Vertex u : c.content){
                    avgDist += relativity[verts.indexOf(v)][verts.indexOf(u)];
                }
                avgDist/= (double)c.size();
                if (avgDist < minCDist){
                    minCDist = avgDist;
                    minClus = c;
                }
            }

            minClus.add(v);
        }

        for (int i = res.size() - 1; i >= 0; i--) {
            // if cluster is size one
            if (res.get(i).size() == 1) {
                // remove the cluster
                Cluster smallCluster = res.remove(i);
                // calculate distances to other clusters
                double[] dist2 = new double[res.size()];
                int k = 0;
                for (Iterator<Cluster> iter = res.listIterator(); iter.hasNext(); k ++){
                    dist2[k] = iter.next().getDistance(smallCluster.get(0));
                }

                // get the smallest distance and add the city
                // to the closest cluster
                int ind = getSmallestIn(dist2);
                res.get(ind).add(smallCluster.get(0));
            }
        }

        return res;
    }

    /**
     * Clusters the Points using hearchical clustering Returns an arraylist of
     * the final clusters. Stops clustering once there is the cutoff number of
     * clusters Does not allow cluster size of 1
     *
     * Uses quick clustering method. Best case n^2, wors n^3
     *
     * @param vertices
     * @param cutoff
     * @return
     */
    public static ArrayList<Cluster> hClustering(Vertex[] vertices, int cutoff) {
        for (Vertex v : vertices){
            v.saveAverageDistance(vertices);
            v.setClosestVertex(vertices);
        }

        // creates initial single point clusters
        ArrayList<Cluster> clusterList = new ArrayList<>();
        for (int i = 0; i < vertices.length; i++) {
            clusterList.add(new Cluster(vertices[i], i));
        }

        ClusterDistanceArray distanceArray = new ClusterDistanceArray(clusterList);

        // combines clusters until there are less clusters
        // than the cutoff amount
        while (clusterList.size() > cutoff) {
            Cluster[] closest = distanceArray.getClosestClusters();
            Cluster growCluster = closest[0];
            Cluster removeCluster = closest[1];

            clusterList.remove(removeCluster);

            for (ListIterator<Vertex> iter = removeCluster.content.listIterator(); iter.hasNext();){
                growCluster.add(iter.next());
            }

            distanceArray.remove(removeCluster);

            if (growCluster.size() > vertices.length* CUTOFFSIZEPERCENT){
                distanceArray.remove(growCluster);
            } else {
                distanceArray.updateCluster(growCluster);
            }
        }


        // combines clusters of size zero with the nearest cluster
        // clusters of size zero distort spread calcs
        // searches through all remaining clusters
        for (int i = clusterList.size() - 1; i >= 0; i--) {
            // if cluster is size one
            if (clusterList.get(i).size() == 1) {
                // remove the cluster
                Cluster smallCluster = clusterList.remove(i);
                // calculate distances to other clusters
                double[] dist2 = new double[clusterList.size()];
                int k = 0;
                for (Iterator<Cluster> iter = clusterList.listIterator(); iter.hasNext(); k ++){
                    dist2[k] = iter.next().getDistance(smallCluster.get(0));
                }

                // get the smallest distance and add the city
                // to the closest cluster
                int ind = getSmallestIn(dist2);
                clusterList.get(ind).add(smallCluster.get(0));
            }
        }

        // finalizes spread and center calculation
        for (int i = 0; i < clusterList.size(); i++) {
            clusterList.get(i).calculateSpread();
        }

        // checks to see if there is only one cluster
        if (clusterList.size() == 1) {
            Cluster one = clusterList.get(0);

            //calculates the distance array for cluster
            double[] distances = new double[one.size()];
            for (int i = 0; i < one.size(); i++) {
                distances[i] = one.getDistance(one.get(i));
            }

            //gets the point thats farthest from the centroid
            Vertex far = one.get(getSmallestIn(distances));
            //removes and makes its own cluster
            one.remove(far);
            Cluster two = new Cluster(far, one.ID + 1);

            //gets point closest to new cluster
            distances = new double[one.size()];
            for (int i = 0; i < one.size(); i++) {
                distances[i] = one.getDistance(one.get(i));
            }

            //removes and adds to new
            far = one.get(getSmallestIn(distances));
            one.remove(far);
            two.add(far);

            clusterList.add(two);

            // finalizes spread and center calculation
            for (int i = 0; i < clusterList.size(); i++) {
                clusterList.get(i).calculateSpread();
            }
        }

        return clusterList;
    }

    /**
     * Returns the index of the cluster in the list with the specified id
     * @param id
     * @param list
     * @return
     */
    private static int indexOfClusterWithID(int id, ArrayList<Cluster> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getID() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the index of the smallest double in a double array
     *
     * @param dist
     * @return
     */
    public static int getSmallestIn(double[] dist) {
        int in = 0;
        for (int i = 0; i < dist.length; i++) {
            if (dist[i] < dist[in]) {
                in = i;
            }
        }
        return in;
    }

    /**
     * Gets the smallest value in the 2d array given a maximum value for each row.
     * @param dist
     * @param max
     * @return array of indexes, i value and then j value.
     */
    public static int[] getSmallestIn(double[][] dist, double[] max){
        int[] in = new int[]{0,1};
        in[0] = getSmallestIn(max);
        in[1] = Math.abs(in[0]-1);
        for (int i = 0; i < dist[0].length; i++){
            if (dist[in[0]][i] != 1000000000){
                if (i != in[0]){
                    if(dist[in[0]][i] < dist[in[0]][in[i]]){
                        in[1] = i;
                    }
                }
            }
        }
        return in;
    }

    /**
     * Returns the pair of indexes for the smallest value in the array which is
     * not equal to 0 used for distance arrays.
     * <p>
     * Does not allow i = j because it is often used for distance arrays
     *
     * @param dist
     * @return
     */
    public static int[] getSmallestIn(double[][] dist) {
        int[] in = new int[]{0, 1};
        for (int i = 0; i < dist.length; i++) {
            for (int j = 0; j < dist[0].length; j++) {
                if (dist[i][j] != 1000000000) {
                    if (i != j) {
                        if (dist[i][j] < dist[in[0]][in[1]]) {
                            in[0] = i;
                            in[1] = j;
                        }
                    }
                }
            }
        }
        return in;
    }
}
