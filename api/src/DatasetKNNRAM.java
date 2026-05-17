import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatasetKNNRAM {

    private static final int DIM = 14;
    private static final int K = 5;

    // flat memory layout
    // vector i starts at i * 14
    private float[] vectors;

    // one label per vector
    private byte[] labels;

    private int count;

    public DatasetKNNRAM(String path) throws Exception {

        long fileSize = Files.size(Path.of(path));

        int recordSize = DIM * 4 + 1;

        this.count = (int) (fileSize / recordSize);

        this.vectors = new float[count * DIM];
        this.labels = new byte[count];

        try (
                DataInputStream in =
                        new DataInputStream(
                                new BufferedInputStream(
                                        Files.newInputStream(
                                                Path.of(path)
                                        )
                                )
                        )
        ) {

            for (int i = 0; i < count; i++) {

                int base = i * DIM;

                vectors[base]      = in.readFloat();
                vectors[base + 1]  = in.readFloat();
                vectors[base + 2]  = in.readFloat();
                vectors[base + 3]  = in.readFloat();
                vectors[base + 4]  = in.readFloat();
                vectors[base + 5]  = in.readFloat();
                vectors[base + 6]  = in.readFloat();
                vectors[base + 7]  = in.readFloat();
                vectors[base + 8]  = in.readFloat();
                vectors[base + 9]  = in.readFloat();
                vectors[base + 10] = in.readFloat();
                vectors[base + 11] = in.readFloat();
                vectors[base + 12] = in.readFloat();
                vectors[base + 13] = in.readFloat();

                labels[i] = in.readByte();
            }
        }

        System.out.println("loaded vectors = " + count);
    }

    public Response search(float[] q) {

        float[] bestDist = new float[K];
        byte[] bestLabel = new byte[K];

        for (int i = 0; i < K; i++) {
            bestDist[i] = Float.MAX_VALUE;
        }

        for (int i = 0; i < count; i++) {

            int base = i * DIM;

            float d0  = vectors[base]      - q[0];
            float d1  = vectors[base + 1]  - q[1];
            float d2  = vectors[base + 2]  - q[2];
            float d3  = vectors[base + 3]  - q[3];
            float d4  = vectors[base + 4]  - q[4];
            float d5  = vectors[base + 5]  - q[5];
            float d6  = vectors[base + 6]  - q[6];
            float d7  = vectors[base + 7]  - q[7];
            float d8  = vectors[base + 8]  - q[8];
            float d9  = vectors[base + 9]  - q[9];
            float d10 = vectors[base + 10] - q[10];
            float d11 = vectors[base + 11] - q[11];
            float d12 = vectors[base + 12] - q[12];
            float d13 = vectors[base + 13] - q[13];

            float dist =
                    d0*d0 + d1*d1 + d2*d2 + d3*d3 +
                            d4*d4 + d5*d5 + d6*d6 + d7*d7 +
                            d8*d8 + d9*d9 + d10*d10 + d11*d11 +
                            d12*d12 + d13*d13;

            byte label = labels[i];

            int pos = 4;

            // ordered insertion
            while (pos > 0 && dist < bestDist[pos - 1]) {

                bestDist[pos] = bestDist[pos - 1];
                bestLabel[pos] = bestLabel[pos - 1];

                pos--;
            }

            bestDist[pos] = dist;
            bestLabel[pos] = label;
        }

        int frauds = 0;

        if (bestLabel[0] == 1) frauds++;
        if (bestLabel[1] == 1) frauds++;
        if (bestLabel[2] == 1) frauds++;
        if (bestLabel[3] == 1) frauds++;
        if (bestLabel[4] == 1) frauds++;

        float score = frauds / 5f;

        return new Response(
                score < 0.6f,
                score
        );
    }
}