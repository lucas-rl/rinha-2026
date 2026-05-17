import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatasetLoaderTxt {

    public static Dataset load(String path)
            throws Exception {

        Path file = Path.of(path);

        long lineCount =
                Files.lines(file).count();

        float[][] vectors =
                new float[(int) lineCount][14];

        boolean[] labels =
                new boolean[(int) lineCount];

        BufferedReader reader =
                Files.newBufferedReader(file);

        String line;

        int index = 0;

        while ((line = reader.readLine()) != null) {

            String[] parts =
                    line.split("\\|");

            String[] values =
                    parts[0].split(",");

            float[] vector =
                    new float[14];

            for (int i = 0; i < 14; i++) {

                vector[i] =
                        Float.parseFloat(values[i]);
            }

            vectors[index] = vector;

            labels[index] =
                    parts[1].equals("1");

            index++;
            if(index % 1000 == 0){
                System.out.println("counter = " + index);
            }
        }

        return new Dataset(
                vectors,
                labels
        );
    }
}