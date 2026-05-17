import java.util.ArrayList;
import java.util.List;

public class PayloadParser {

    public static Payload parse(String json) {

        json = removeSpaces(json);

        String id =
                extractString(json, "id");

        // =========================
        // transaction
        // =========================

        String transactionJson =
                extractObject(json, "transaction");

        Payload.Transaction transaction =
                new Payload.Transaction(
                        extractFloat(transactionJson, "amount"),
                        extractInt(transactionJson, "installments"),
                        extractString(transactionJson, "requested_at")
                );

        // =========================
        // customer
        // =========================

        String customerJson =
                extractObject(json, "customer");

        Payload.Customer customer =
                new Payload.Customer(
                        extractFloat(customerJson, "avg_amount"),
                        extractInt(customerJson, "tx_count_24h"),
                        extractStringArray(
                                customerJson,
                                "known_merchants"
                        )
                );

        // =========================
        // merchant
        // =========================

        String merchantJson =
                extractObject(json, "merchant");

        Payload.Merchant merchant =
                new Payload.Merchant(
                        extractString(merchantJson, "id"),
                        extractString(merchantJson, "mcc"),
                        extractFloat(merchantJson, "avg_amount")
                );

        // =========================
        // terminal
        // =========================

        String terminalJson =
                extractObject(json, "terminal");

        Payload.Terminal terminal =
                new Payload.Terminal(
                        extractBoolean(terminalJson, "is_online"),
                        extractBoolean(terminalJson, "card_present"),
                        extractFloat(terminalJson, "km_from_home")
                );

        // =========================
        // last_transaction
        // =========================

        String lastTxJson =
                extractObject(json, "last_transaction");

        Payload.LastTransaction lastTransaction = null;

        if (lastTxJson != null) {

            lastTransaction =
                    new Payload.LastTransaction(
                            extractString(
                                    lastTxJson,
                                    "timestamp"
                            ),
                            extractFloat(
                                    lastTxJson,
                                    "km_from_current"
                            )
                    );
        }

        // =========================
        // final payload
        // =========================

        return new Payload(
                id,
                transaction,
                customer,
                merchant,
                terminal,
                lastTransaction
        );
    }

    // ======================================================
    // Helpers
    // ======================================================

    private static String removeSpaces(String json) {

        return json
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "");
    }

    private static String extractString(
            String json,
            String key
    ) {

        String search =
                "\"" + key + "\":";

        int start = json.indexOf(search);

        if (start == -1) {
            return null;
        }

        start += search.length();

        if (json.startsWith("null", start)) {
            return null;
        }

        start++;

        int end = json.indexOf("\"", start);

        return json.substring(start, end);
    }

    private static Integer extractInt(
            String json,
            String key
    ) {

        String value =
                extractPrimitive(json, key);

        if (value == null) {
            return null;
        }

        return Integer.parseInt(value);
    }

    private static Float extractFloat(
            String json,
            String key
    ) {

        String value =
                extractPrimitive(json, key);

        if (value == null) {
            return null;
        }

        return Float.parseFloat(value);
    }

    private static Boolean extractBoolean(
            String json,
            String key
    ) {

        String value =
                extractPrimitive(json, key);

        if (value == null) {
            return null;
        }

        return Boolean.parseBoolean(value);
    }

    private static String extractPrimitive(
            String json,
            String key
    ) {

        String search =
                "\"" + key + "\":";

        int start = json.indexOf(search);

        if (start == -1) {
            return null;
        }

        start += search.length();

        if (json.startsWith("null", start)) {
            return null;
        }

        int end = start;

        while (end < json.length()
                && json.charAt(end) != ','
                && json.charAt(end) != '}') {

            end++;
        }

        return json.substring(start, end);
    }

    private static String extractObject(
            String json,
            String key
    ) {

        String nullSearch =
                "\"" + key + "\":null";

        if (json.contains(nullSearch)) {
            return null;
        }

        String search =
                "\"" + key + "\":{";

        int start = json.indexOf(search);

        if (start == -1) {
            return null;
        }

        start += search.length() - 1;

        int braces = 0;
        int end = start;

        while (end < json.length()) {

            if (json.charAt(end) == '{') {
                braces++;
            }

            if (json.charAt(end) == '}') {
                braces--;
            }

            if (braces == 0) {
                break;
            }

            end++;
        }

        return json.substring(start, end + 1);
    }

    private static String[] extractStringArray(
            String json,
            String key
    ) {

        String search =
                "\"" + key + "\":[";

        int start = json.indexOf(search);

        if (start == -1) {
            return null;
        }

        start += search.length();

        int end = json.indexOf("]", start);

        String content =
                json.substring(start, end);

        if (content.isEmpty()) {
            return new String[0];
        }

        String[] raw =
                content.split(",");

        List<String> values =
                new ArrayList<>();

        for (String item : raw) {

            values.add(
                    item.replace("\"", "")
            );
        }

        return values.toArray(new String[0]);
    }
}