import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public class KMeansCentroids {

    static final int DIM = 14;
    static final int RECORD = DIM * 4 + 1;

    static final int K = 50_000;
    static final int ITERATIONS = 5;

    public static void main(String[] args) throws Exception {

        String inputPath = "dataset.bin";
        String outputPath = "centroids.bin";

        FileChannel ch = FileChannel.open(
                Path.of(inputPath),
                StandardOpenOption.READ
        );

        MappedByteBuffer data =
                ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());

        int n = (int) (ch.size() / RECORD);

        System.out.println("dataset size = " + n);

        float[][] centroids = new float[K][DIM];
        int[] fraudCount = new int[K];
        int[] legitCount = new int[K];

        Random rnd = new Random(42);

        // STEP 1: initialize random centroids
        for (int k = 0; k < K; k++) {

            int idx = rnd.nextInt(n);
            int base = idx * RECORD;

            for (int d = 0; d < DIM; d++) {
                centroids[k][d] = data.getFloat(base + d * 4);
            }
        }

        // STEP 2: K-means iterations
        for (int it = 0; it < ITERATIONS; it++) {

            System.out.println("iteration " + it);

            float[][] sum = new float[K][DIM];
            int[] count = new int[K];

            // reset label stats each iteration
            int[] fraudTmp = new int[K];
            int[] legitTmp = new int[K];

            for (int i = 0; i < n; i++) {

                int base = i * RECORD;

                byte label = data.get(base + DIM * 4);

                int best = 0;
                float bestDist = Float.MAX_VALUE;

                for (int k = 0; k < K; k++) {

                    float dist = 0f;

                    for (int d = 0; d < DIM; d++) {

                        float v = data.getFloat(base + d * 4);
                        float diff = v - centroids[k][d];
                        dist += diff * diff;
                    }

                    if (dist < bestDist) {
                        bestDist = dist;
                        best = k;
                    }
                }

                // accumulate vector sum
                for (int d = 0; d < DIM; d++) {
                    sum[best][d] += data.getFloat(base + d * 4);
                }

                count[best]++;

                // accumulate label stats
                if (label == 1) fraudTmp[best]++;
                else legitTmp[best]++;
            }

            // recompute centroids
            for (int k = 0; k < K; k++) {

                if (count[k] == 0) continue;

                for (int d = 0; d < DIM; d++) {
                    centroids[k][d] = sum[k][d] / count[k];
                }
            }

            fraudCount = fraudTmp;
            legitCount = legitTmp;
        }

        // STEP 3: write output file
        try (DataOutputStream out =
                     new DataOutputStream(
                             new BufferedOutputStream(
                                     java.nio.file.Files.newOutputStream(
                                             Path.of(outputPath)
                                     )
                             )
                     )) {

            for (int k = 0; k < K; k++) {

                for (int d = 0; d < DIM; d++) {
                    out.writeFloat(centroids[k][d]);
                }

                byte label =
                        fraudCount[k] > legitCount[k]
                                ? (byte) 1
                                : (byte) 0;

                out.writeByte(label);
            }
        }

        System.out.println("DONE -> centroids.bin created");
    }
}