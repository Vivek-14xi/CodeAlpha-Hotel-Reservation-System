package HotelReservationSystem;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

public class Payment {
    public enum PaymentMode{
        CASH,CREDIT_CARD ,DEBIT_CARD,UPI,NET_BANKING;
    }

    public enum PaymentStatus{
        PENDING,SUCCESS,FAILED,REFUNDED
    }

    private final String paymentId;
    private final String reservationId;
    private final double amount;
    private final PaymentMode paymentMode ;
    private PaymentStatus status;
    private final String timestamp;
    private String transcationRef;

    private static int payCounter=5000;

    public Payment(String reservationId,double amount,PaymentMode paymentMode) {
        this.paymentId      = "PAY" + (++payCounter);
        this.reservationId  = reservationId;
        this.amount         = amount;
        this.paymentMode    = paymentMode;
        this.status         = PaymentStatus.PENDING;
        this.timestamp      = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        this.transcationRef = "N/A";
    }
    public Payment(String paymentId,String reservationId,double amount, PaymentMode paymentMode,PaymentStatus paymentStatus ,String timestamp,String transcationRef){
        this.paymentId=paymentId;
        this.reservationId=reservationId;
        this.amount=amount;
        this.paymentMode=paymentMode;
        this.status=status;
        this.timestamp=timestamp;
        this.transcationRef=transcationRef;
    }

    public String getPaymentId() {
        return paymentId;
    }
    public String getReservationId() {
        return reservationId;
    }
    public double getAmount() {
        return amount;
    }
    public PaymentMode getPaymentMode() {
        return paymentMode;
    }
    public PaymentStatus getStatus() {
        return status;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getTranscationRef() {
        return transcationRef;
    }

    public void setStatus(PaymentStatus status){
    this.status=status;
    }
    public void setTranscationRef(String transcationRef){
        this.transcationRef=transcationRef;
    }

    public boolean processPayment(){
        System.out.println("\n [PAYMENT GATEWAY] Processing payment....");
        simulateNetworkDelay();
        boolean success;
        if (paymentMode==PaymentMode.CASH){
            success=true;
        }else {
            success=(Math.random()<0.90);
        }
        if (success){
            this.status=PaymentStatus.SUCCESS;
            this.transcationRef=generateTranscationRef();
            System.out.println("[PAYMENT GATEWAY Payment SUCCESSFUL.");
            System.out.println("Transaction Ref: "+transcationRef);
        }else{
            this.status=PaymentStatus.FAILED;
            System.out.println("[PAYMENT GATEWAY] Payment Failed .Please try again");
        }
        return success;
    }

    public void processRefund(){
        this.status=PaymentStatus.REFUNDED;
        System.out.println("\n  [PAYMENT GATEWAY] Refund initiated for Rs. " +
                String.format("%.2f", amount));
        if (paymentMode == PaymentMode.CASH) {
            System.out.println("  Cash refund will be provided at the front desk.");
        } else {
            System.out.println("  Refund of Rs. " + String.format("%.2f", amount) +
                    " will be credited within 3-5 business days.");
        }    }

    private String generateTranscationRef(){
        long ref=(long)(Math.random()* 9_000_000_000L) + 1_000_000_000L;
        return paymentMode.name().charAt(0)+String.valueOf(ref);
    }

    private void simulateNetworkDelay(){
        try{
            Thread.sleep(800);
        }catch (InterruptedException ignored){
            System.out.println("server busy");
        }
    }

    public String getReceipt() {
        return String.format(
                "\n  -------- PAYMENT RECEIPT --------" +
                        "\n  Payment ID      : %s" +
                        "\n  Reservation ID  : %s" +
                        "\n  Amount          : Rs. %.2f" +
                        "\n  Mode            : %s" +
                        "\n  Status          : %s" +
                        "\n  Date & Time     : %s" +
                        "\n  Transaction Ref : %s" +
                        "\n  ---------------------------------",
                paymentId, reservationId, amount,
                paymentMode, status, timestamp, transcationRef);
    }

    public String toCSV() {
        return paymentId + "," + reservationId + "," + amount + "," +
                paymentMode.name() + "," + status.name() + "," +
                timestamp + "," + transcationRef;
    }

    @Override
    public String toString() {
        return "Payment[" + paymentId + ", Rs." + amount + ", " + status + "]";
    }
}

