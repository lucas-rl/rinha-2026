import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DatasetKNN {

    private static final int DIM = 14;
    private static final int RECORD = DIM * 4 + 1;
    private static final int K = 5;

    private MappedByteBuffer buffer;
    private int count;

    public DatasetKNN(String path) throws Exception {

        FileChannel ch = FileChannel.open(
                Path.of(path),
                StandardOpenOption.READ
        );

        this.buffer = ch.map(
                FileChannel.MapMode.READ_ONLY,
                0,
                ch.size()
        );

        this.count = (int) (ch.size() / RECORD);
    }

    public Response search(float[] q) {

        float[] bestDist = new float[K];
        byte[] bestLabel = new byte[K];

        for (int i = 0; i < K; i++) {
            bestDist[i] = Float.MAX_VALUE;
        }

        for (int i = 0; i < count; i++) {

            int base = i * RECORD;

            // INLINE distance (no array access inside loop)
            float d0 = buffer.getFloat(base)     - q[0];
            float d1 = buffer.getFloat(base+4)   - q[1];
            float d2 = buffer.getFloat(base+8)   - q[2];
            float d3 = buffer.getFloat(base+12)  - q[3];
            float d4 = buffer.getFloat(base+16)  - q[4];
            float d5 = buffer.getFloat(base+20)  - q[5];
            float d6 = buffer.getFloat(base+24)  - q[6];
            float d7 = buffer.getFloat(base+28)  - q[7];
            float d8 = buffer.getFloat(base+32)  - q[8];
            float d9 = buffer.getFloat(base+36)  - q[9];
            float d10= buffer.getFloat(base+40)  - q[10];
            float d11= buffer.getFloat(base+44)  - q[11];
            float d12= buffer.getFloat(base+48)  - q[12];
            float d13= buffer.getFloat(base+52)  - q[13];

            float dist =
                    d0*d0 + d1*d1 + d2*d2 + d3*d3 +
                            d4*d4 + d5*d5 + d6*d6 + d7*d7 +
                            d8*d8 + d9*d9 + d10*d10 + d11*d11 +
                            d12*d12 + d13*d13;

            byte label = buffer.get(base + 56);

            // manual top-K insertion (NO heap)
            int worst = 0;
            for (int k = 1; k < K; k++) {
                if (bestDist[k] > bestDist[worst]) {
                    worst = k;
                }
            }

            if (dist < bestDist[worst]) {
                bestDist[worst] = dist;
                bestLabel[worst] = label;
            }
        }

        int frauds = 0;
        for (int i = 0; i < K; i++) {
            if (bestLabel[i] == 1) frauds++;
        }

        float score = frauds / (float) K;

        return new Response(score < 0.6f, score);
    }

}