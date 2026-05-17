import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatasetLoader {

    public static Dataset load(String path)
            throws Exception {

        Path file = Path.of(path);

        long fileSize = Files.size(file);

        int recordSize = (14 * 4) + 1; // 57 bytes per row

        int count = (int) (fileSize / recordSize);

        float[][] vectors =
                new float[count][14];

        boolean[] labels =
                new boolean[count];

        try (DataInputStream dis =
                     new DataInputStream(
                             new BufferedInputStream(
                                     Files.newInputStream(file)
                             )
                     )) {

            for (int i = 0; i < count; i++) {

                float[] vector = new float[14];

                for (int j = 0; j < 14; j++) {
                    vector[j] = dis.readFloat();
                }

                vectors[i] = vector;

                labels[i] = dis.readByte() == 1;

//                for (int j = 0; j < 14; j++) {
//                    System.out.print(vectors[i][j] + " ");
//                }
//                System.out.println();

                if (i % 100000 == 0) {
                    System.out.println("loaded = " + i);
                }
                if (i % 2999999 == 0) {
                    System.out.println("loaded = " + i);
                }
            }
        }

        return new Dataset(vectors, labels);
    }
}