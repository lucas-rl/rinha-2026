import java.io.*;
import java.nio.file.*;

public class DatasetToBin {

    public static void main(String[] args) throws Exception {

        Path input = Path.of("api/src/dataset.txt");
        Path output = Path.of("api/src/dataset.bin");

        try (
                BufferedReader br = Files.newBufferedReader(input);
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(
                                Files.newOutputStream(output)
                        )
                )
        ) {

            String line;
            long idx = 0;

            while ((line = br.readLine()) != null) {

                String[] split = line.split("\\|");

                if (split.length != 2) {
                    throw new RuntimeException("Bad line at " + idx);
                }

                String[] values = split[0].split(",");

                if (values.length != 14) {
                    throw new RuntimeException(
                            "Invalid vector size at line " + idx +
                                    " size=" + values.length
                    );
                }

                // write 14 floats
                for (int i = 0; i < 14; i++) {

                    float v = Float.parseFloat(values[i].trim());
                    dos.writeFloat(v);
                }

                // write label (0 or 1)
                int label = Integer.parseInt(split[1].trim());
                dos.writeByte(label);

                idx++;

                if (idx % 500_000 == 0) {
                    System.out.println("converted: " + idx);
                }
            }
        }

        System.out.println("DONE");
    }
}