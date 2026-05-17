import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

public class PayloadVectorizer {

    // ==========================================
    // normalization constants
    // ==========================================

    private static final float MAX_AMOUNT = 10000.0f;

    private static final float MAX_INSTALLMENTS = 12.0f;

    private static final float AMOUNT_VS_AVG_RATIO = 10.0f;

    private static final float MAX_MINUTES = 1440.0f;

    private static final float MAX_KM = 1000.0f;

    private static final float MAX_TX_COUNT_24H = 20.0f;

    private static final float MAX_MERCHANT_AVG_AMOUNT =
            10000.0f;

    // ==========================================
    // main vectorization
    // ==========================================

    public static float[] vectorize(Payload payload) {

        float[] vector = new float[14];

        // ==========================================
        // 0 - amount
        // ==========================================

        vector[0] = clamp(
                payload.transaction().amount()
                        / MAX_AMOUNT
        );

        // ==========================================
        // 1 - installments
        // ==========================================

        vector[1] = clamp(
                payload.transaction().installments()
                        / MAX_INSTALLMENTS
        );

        // ==========================================
        // 2 - amount_vs_avg
        // ==========================================

        vector[2] = clamp(
                (
                        payload.transaction().amount()
                                / payload.customer().avgAmount()
                ) / AMOUNT_VS_AVG_RATIO
        );

        // ==========================================
        // 3 - hour_of_day
        // ==========================================

        ZonedDateTime requestedAt =
                ZonedDateTime.parse(
                        payload.transaction().requestedAt()
                );

        vector[3] = requestedAt.getHour() / 23.0f;

        // ==========================================
        // 4 - day_of_week
        // seg=0 ... dom=6
        // ==========================================

        DayOfWeek day =
                requestedAt.getDayOfWeek();

        int normalizedDay =
                day.getValue() - 1;

        vector[4] =
                normalizedDay / 6.0f;

        // ==========================================
        // 5 - minutes_since_last_tx
        // ==========================================

        if (payload.lastTransaction() == null) {

            vector[5] = -1;

        } else {

            Instant current =
                    Instant.parse(
                            payload.transaction().requestedAt()
                    );

            Instant last =
                    Instant.parse(
                            payload.lastTransaction().timestamp()
                    );

            long minutes =
                    Duration.between(last, current)
                            .toMinutes();

            vector[5] = clamp(
                    minutes / MAX_MINUTES
            );
        }

        // ==========================================
        // 6 - km_from_last_tx
        // ==========================================

        if (payload.lastTransaction() == null) {

            vector[6] = -1;

        } else {

            vector[6] = clamp(
                    payload.lastTransaction()
                            .kmFromCurrent()
                            / MAX_KM
            );
        }

        // ==========================================
        // 7 - km_from_home
        // ==========================================

        vector[7] = clamp(
                payload.terminal().kmFromHome()
                        / MAX_KM
        );

        // ==========================================
        // 8 - tx_count_24h
        // ==========================================

        vector[8] = clamp(
                payload.customer().txCount24h()
                        / MAX_TX_COUNT_24H
        );

        // ==========================================
        // 9 - is_online
        // ==========================================

        vector[9] =
                payload.terminal().isOnline()
                        ? 1
                        : 0;

        // ==========================================
        // 10 - card_present
        // ==========================================

        vector[10] =
                payload.terminal().cardPresent()
                        ? 1
                        : 0;

        // ==========================================
        // 11 - unknown_merchant
        // ==========================================

        boolean known = false;

        for (String merchant :
                payload.customer().knownMerchants()) {

            if (merchant.equals(
                    payload.merchant().id()
            )) {

                known = true;
                break;
            }
        }

        vector[11] =
                known ? 0 : 1;

        // ==========================================
        // 12 - mcc_risk
        // ==========================================

        vector[12] =
                getMccRisk(
                        payload.merchant().mcc()
                );

        // ==========================================
        // 13 - merchant_avg_amount
        // ==========================================

        vector[13] = clamp(
                payload.merchant().avgAmount()
                        / MAX_MERCHANT_AVG_AMOUNT
        );

        return vector;
    }

    // ==========================================
    // clamp
    // ==========================================

    private static float clamp(float value) {

        if (value < 0) {
            return 0;
        }

        if (value > 1) {
            return 1;
        }

        return value;
    }

    // ==========================================
    // fake MCC lookup
    // ==========================================

    private static float getMccRisk(String mcc) {

        return switch (mcc) {

            case "5411" -> 0.15f;

            case "5812" -> 0.30f;

            case "5912" -> 0.20f;

            case "5944" -> 0.45f;

            case "7801" -> 0.80f;

            case "7802" -> 0.75f;

            case "7995" -> 0.85f;

            case "4511" -> 0.35f;

            case "5311" -> 0.25f;

//            case "5999" -> 0.50;

            default -> 0.50f;
        };
    }
}