import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final String[] BACKENDS = {

            "http://api1:8081",
            "http://api2:8082"

    };

    private static final AtomicInteger COUNTER =
            new AtomicInteger(0);

    public static void main(String[] args)
            throws Exception {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(9999),
                        0
                );

        server.createContext(
                "/",
                new ProxyHandler()
        );

        server.setExecutor(
                Executors.newVirtualThreadPerTaskExecutor()
        );

        server.start();

        System.out.println(
                "Load balancer running on 9999"
        );
    }

    static class ProxyHandler
            implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) {

            try {

                String backend =
                        nextBackend();

                String targetUrl =
                        backend
                                + exchange.getRequestURI();

                URL url =
                        new URL(targetUrl);

                HttpURLConnection connection =
                        (HttpURLConnection)
                                url.openConnection();

                String method =
                        exchange.getRequestMethod();

                connection.setRequestMethod(method);

                boolean hasBody =
                        method.equals("POST")
                                || method.equals("PUT")
                                || method.equals("PATCH");

                if(hasBody){
                    connection.setDoOutput(true);

                    // copy request body

                    try (
                            InputStream clientBody =
                                    exchange.getRequestBody();

                            OutputStream backendBody =
                                    connection.getOutputStream()
                    ) {

                        clientBody.transferTo(
                                backendBody
                        );
                    }
                }

                int responseCode =
                        connection.getResponseCode();

                byte[] responseBytes =
                        connection
                                .getInputStream()
                                .readAllBytes();

                exchange.sendResponseHeaders(
                        responseCode,
                        responseBytes.length
                );

                try (
                        OutputStream os =
                                exchange
                                        .getResponseBody()
                ) {

                    os.write(responseBytes);
                }

            } catch (Exception e) {

                e.printStackTrace();

                try {

                    exchange.sendResponseHeaders(
                            500,
                            0
                    );

                    exchange.close();

                } catch (Exception ignored) {
                }
            }
        }

        private String nextBackend() {

            int index =
                    COUNTER.getAndIncrement();

            return BACKENDS[
                    index % BACKENDS.length
                    ];
        }
    }
}