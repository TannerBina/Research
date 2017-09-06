import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Stores distance array for a list of clusters.
 * Created by Tanner on 10/13/2016.
 */
public class ClusterDistanceArray {

    private class DistanceArray{

        private Cluster[] constantArray;
        private double[] distances;
        private int minValueIndex;
        private double minValue;
        private Cluster base;
        private ArrayList<Cluster> list;

        private DistanceArray(Cluster base, ArrayList<Cluster> list){
            this.base = base;
            this.list = new ArrayList<>(list);

            constantArray = new Cluster[list.size()];
            minValue = 1000000000;
            distances = new double[list.size()];

            int i = 0;
            for (ListIterator<Cluster> iter = list.listIterator(); iter.hasNext(); i++){
                Cluster current = iter.next();
                constantArray[current.getID()] = current;

                distances[current.getID()] = base.getDistance(current);

                if (current.getID() == base.getID()){
                    distances[current.getID()] = 1000000000;
                }

                if (distances[current.getID()] < minValue){
                    minValue = distances[current.getID()];
                    minValueIndex = current.getID();
                }
            }
        }

        private void update(Cluster a){
            int index = a.getID();

            distances[index] = base.getDistance(a);

            if (base.getID() == a.getID()){
                distances[index] = 1000000000;
            }

            if (distances[index] < minValue){
                minValue = distances[index];
                minValueIndex = index;
            } else if (minValueIndex == index){
                minValueIndex = Cluster.getSmallestIn(distances);
                minValue = distances[minValueIndex];
            }
        }

        private void updateAll(){
            minValue = 1000000000;
            minValueIndex = -1;

            for (ListIterator<Cluster> iter = list.listIterator(); iter.hasNext();){
                Cluster current = iter.next();
                if (distances[current.getID()] != 1000000000) {
                    distances[current.getID()] = base.getDistance(current);
                }

                if (current.getID() == base.getID()){
                    distances[current.getID()] = 1000000000;
                }

                if (distances[current.getID()] < minValue){
                    minValue = distances[current.getID()];
                    minValueIndex = current.getID();
                }
            }
        }

        private void remove(int index){
            distances[index] = 1000000000;
            if (minValueIndex == index){
                minValueIndex = Cluster.getSmallestIn(distances);
                minValue = distances[minValueIndex];
            }
        }
    }

    private class DistanceMap{

        private DistanceArray[] distanceArray;
        private int minValueIndex;
        private double minValue;

        private DistanceMap(int maxSize){
            distanceArray = new DistanceArray[maxSize];
            minValueIndex = -1;
            minValue = 1000000000;
        }

        private void store(Cluster input, DistanceArray array){
            distanceArray[input.getID()] = array;
            if (array.minValue < minValue){
                minValue = array.minValue;
                minValueIndex = input.getID();
            }
        }

        private void remove(Cluster removal){
            int ind = removal.getID();
            distanceArray[ind] = null;
            for (int i = 0; i < distanceArray.length; i++){
                if (distanceArray[i] != null){
                    distanceArray[i].remove(ind);
                }
            }
            if (ind == minValueIndex){
                minValue = 1000000000;
                minValueIndex = -1;
                for (int i = 0; i < distanceArray.length; i++){
                    if (distanceArray[i] != null && distanceArray[i].minValue < minValue){
                        minValue = distanceArray[i].minValue;
                        minValueIndex = i;
                    }
                }
                if (minValueIndex == -1){
                    System.out.println("minValIndex out of bounds");
                    for (int i = 0; i < distanceArray.length; i++){
                        if (distanceArray[i] != null)
                        System.out.println(distanceArray[i].minValue);
                    }
                }
            }
        }

        private void update(Cluster update){
            int ind = update.getID();
            distanceArray[ind].updateAll();

            if (minValueIndex == ind){
                if (distanceArray[ind].minValue < minValue){
                    minValue = distanceArray[ind].minValue;
                    minValueIndex = ind;
                } else {
                    minValue = 1000000000;
                    minValueIndex = -1;
                }
            }
            for (int i = 0; i < distanceArray.length; i++){
                if (distanceArray[i] != null){
                    distanceArray[i].update(update);
                    if (distanceArray[i].minValue < minValue){
                        minValue = distanceArray[i].minValue;
                        minValueIndex = i;
                    }
                }
            }

        }

        private Cluster[] getClosestClusters(){
            Cluster[] result = new Cluster[2];
            DistanceArray small = distanceArray[minValueIndex];
            result[0] = small.base;
            result[1] = small.constantArray[small.minValueIndex];
            return result;
        }
    }

    private DistanceMap map;

    /**
     * A distance array for a list of clusters
     * @param list
     */
    public ClusterDistanceArray(ArrayList<Cluster> list){
        map = new DistanceMap(list.size());
        for (ListIterator<Cluster> iter = list.listIterator(); iter.hasNext();){
            Cluster current = iter.next();
            DistanceArray distanceArray = new DistanceArray(current, list);
            map.store(current, distanceArray);
        }
    }

    /**
     * Returns the two closest clusters
     * @return
     */
    public Cluster[] getClosestClusters(){
        return map.getClosestClusters();
    }

    /**
     * Updates the values for the specified cluster.
     * @param a
     */
    public void updateCluster(Cluster a){
        map.update(a);
    }

    public void remove(Cluster remove){
        map.remove(remove);
    }
}
