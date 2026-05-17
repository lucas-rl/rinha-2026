public record Payload(
        String id,
        Transaction transaction,
        Customer customer,
        Merchant merchant,
        Terminal terminal,
        LastTransaction lastTransaction

) {

    public record Transaction(

            Float amount,
            Integer installments,
            String requestedAt

    ) {}

    public record Customer(

            Float avgAmount,
            Integer txCount24h,
            String[] knownMerchants

    ) {}

    public record Merchant(

            String id,
            String mcc,
            Float avgAmount

    ) {}

    public record Terminal(

            Boolean isOnline,
            Boolean cardPresent,
            Float kmFromHome

    ) {}

    public record LastTransaction(

            String timestamp,
            Float kmFromCurrent

    ) {}

}