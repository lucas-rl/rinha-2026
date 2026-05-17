import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Processor {
    static Dataset dataset;

    public static void load(String path){
        try {
            dataset = DatasetLoader.load(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Processor() throws Exception {
    }

    public static Response process(float[] transaction){
        PriorityQueue<Neighbor> heap =
                new PriorityQueue<>(
                        Comparator.comparingDouble(
                                Neighbor::distance
                        ).reversed()
                );

        for (int i = 0;
             i < dataset.vectors().length;
             i++) {

            double distance =
                    distanceSquared(
                            transaction,
                            dataset.vectors()[i]
                    );

            Neighbor neighbor =
                    new Neighbor(
                            distance,
                            dataset.labels()[i]
                    );

            if (heap.size() < 5) {

                heap.add(neighbor);

            } else if (
                    distance < heap.peek().distance()
            ) {

                heap.poll();

                heap.add(neighbor);
            }
        }
        int frauds = 0;
        for (Iterator<Neighbor> it = heap.iterator(); it.hasNext(); ) {
            Neighbor n = it.next();

            if(n.fraud()){
                frauds++;
            }
        }
        float score = frauds / 5f ;
        return new Response(score<0.6, score);

    }

    private static float distanceSquared(
            float[] a,
            float[] b
    ) {

        float sum = 0;

        for (int i = 0; i < 14; i++) {

            float diff = a[i] - b[i];

            sum += diff * diff;
        }

        return sum;
    }

}
