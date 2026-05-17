import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {

//    public static DatasetKNN datasetKNN;
    public static DatasetKNNRAM datasetKNN;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting to load dataset");
//        Processor.load("/app/dataset.bin");
//        datasetKNN = new DatasetKNN("/app/dataset.bin");
        datasetKNN = new DatasetKNNRAM("/app/dataset.bin");
        System.out.println("Done loading dataset");
        String portEnv =
                System.getenv("PORT");

        int port =
                portEnv == null
                        ? 8080
                        : Integer.parseInt(portEnv);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new MyHandler());

        server.setExecutor(null);

        server.start();

        System.out.println("Server running");
    }

    static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long start = System.nanoTime();

            String response = "Hello World";

            if("GET".equals(exchange.getRequestMethod()) && "/ready".equals(exchange.getRequestURI().getPath())){
                response = "ok";
            }
            if("POST".equals(exchange.getRequestMethod()) && "/fraud-score".equals(exchange.getRequestURI().getPath())){

                String body = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

                Payload payload = PayloadParser.parse(body);
                float[] transaction = PayloadVectorizer.vectorize(payload);
//                Response res = Processor.process(transaction);
                Response res = datasetKNN.search(transaction);
                response = "{\"approved\":" + res.approved() + ", \"fraud_score\":" +  res.score() + "}";

            }


            exchange.sendResponseHeaders(200, response.length());

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            long end = System.nanoTime();

            long durationNs = end - start;

            System.out.println(
                    "Request took: " +
                            durationNs / 1_000_000.0 +
                            " ms"
            );
        }
    }

    public static String vectorToString(double[] vector) {

        StringBuilder sb = new StringBuilder();

        sb.append("[");

        for (int i = 0; i < vector.length; i++) {

            sb.append(vector[i]);

            if (i < vector.length - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");

        return sb.toString();
    }
}