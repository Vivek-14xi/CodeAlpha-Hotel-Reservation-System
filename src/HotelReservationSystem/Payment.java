package HotelReservationSystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {

    public enum PaymentMode {
        CASH, CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING
    }
    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }

    private final String        paymentId;
    private final String        reservationId;
    private final double        amount;
    private final PaymentMode   paymentMode;
    private       PaymentStatus status;
    private final String        timestamp;
    private       String        transactionRef;

    private static int payCounter = 5000;


    public Payment(String reservationId, double amount, PaymentMode paymentMode) {
        this.paymentId      = "PAY" + (++payCounter);
        this.reservationId  = reservationId;
        this.amount         = amount;
        this.paymentMode    = paymentMode;
        this.status         = PaymentStatus.PENDING;
        this.timestamp      = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        this.transactionRef = "N/A";
    }
    public Payment(String paymentId, String reservationId, double amount, PaymentMode paymentMode, PaymentStatus status, String timestamp, String transactionRef) {
        this.paymentId      = paymentId;
        this.reservationId  = reservationId;
        this.amount         = amount;
        this.paymentMode    = paymentMode;
        this.status         = status;
        this.timestamp      = timestamp;
        this.transactionRef = transactionRef;
    }

    public String getPaymentId(){
        return paymentId;
    }
    public String getReservationId(){
        return reservationId;
    }
    public double getAmount(){
        return amount;
    }
    public PaymentMode  getPaymentMode(){
        return paymentMode;
    }
    public PaymentStatus getStatus(){
        return status;
    }
    public String getTimestamp(){
        return timestamp;
    }
    public String getTransactionRef(){
        return transactionRef;
    }

    public void setStatus(PaymentStatus status)         {
        this.status = status;
    }
    public void setTransactionRef(String transactionRef){
        this.transactionRef = transactionRef;
    }

    public boolean processPayment() {
        System.out.println("\n  [PAYMENT GATEWAY] Processing payment...");
        simulateNetworkDelay();

        boolean success;
        if (paymentMode == PaymentMode.CASH) {
            success = true;
        } else {
            // 90% success probability
            success = (Math.random() < 0.90);
        }

        if (success) {
            this.status         = PaymentStatus.SUCCESS;
            this.transactionRef = generateTransactionRef();
            System.out.println("  [PAYMENT GATEWAY] Payment SUCCESSFUL.");
            System.out.println("  Transaction Ref : " + transactionRef);
        } else {
            this.status = PaymentStatus.FAILED;
            System.out.println("  [PAYMENT GATEWAY] Payment FAILED. Please retry.");
        }
        return success;
    }
    public void processRefund() {
        this.status = PaymentStatus.REFUNDED;
        System.out.println("\n  [PAYMENT GATEWAY] Refund initiated for Rs. " +
                String.format("%.2f", amount));
        if (paymentMode == PaymentMode.CASH) {
            System.out.println("  Cash refund will be provided at the front desk.");
        } else {
            System.out.println("  Refund of Rs. " + String.format("%.2f", amount) +
                    " will be credited within 3-5 business days.");
        }
    }

    private String generateTransactionRef() {
        long ref = (long)(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return paymentMode.name().charAt(0) + String.valueOf(ref);
    }
    private void simulateNetworkDelay() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {
            System.out.println("Server Busy");
        }
    }
    public String getReceipt() {
        return String.format(
                "\n  -------- PAYMENT RECEIPT --------"  +
                        "\n  Payment ID      : %s"               +
                        "\n  Reservation ID  : %s"               +
                        "\n  Amount          : Rs. %.2f"         +
                        "\n  Mode            : %s"               +
                        "\n  Status          : %s"               +
                        "\n  Date & Time     : %s"               +
                        "\n  Transaction Ref : %s"               +
                        "\n  ---------------------------------",
                paymentId, reservationId, amount,
                paymentMode, status, timestamp, transactionRef
        );
    }
    public String toCSV() {
        return paymentId + "," + reservationId + "," + amount + "," +
                paymentMode.name() + "," + status.name() + "," +
                timestamp + "," + transactionRef;
    }

    @Override
    public String toString() {
        return "Payment[" + paymentId + ", Rs." + amount + ", " + status + "]";
    }
}