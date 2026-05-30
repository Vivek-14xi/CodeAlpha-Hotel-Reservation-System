package HotelReservationSystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;


public class Reservation {

    public enum ReservationStatus {
        CONFIRMED, CANCELLED, CHECKED_IN, CHECKED_OUT
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final String            reservationId;
    private final Customer          customer;
    private final Room              room;
    private final LocalDate         checkInDate;
    private final LocalDate         checkOutDate;
    private final int               numberOfGuests;
    private       ReservationStatus status;
    private       Payment           payment;
    private final double            totalAmount;
    private final String            bookingDate;

    private static int resCounter = 100;

    public Reservation(Customer customer, Room room, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        if (checkInDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        if (!checkOutDate.isAfter(checkInDate))
            throw new IllegalArgumentException("Check-out must be after check-in.");
        if (numberOfGuests < 1 || numberOfGuests > room.getMaxOccupancy())
            throw new IllegalArgumentException(
                    "Guest count must be between 1 and " + room.getMaxOccupancy() +
                            " for a " + room.getRoomType().getDisplayName() + " room.");

        this.reservationId  = "RES" + (++resCounter);
        this.customer       = customer;
        this.room           = room;
        this.checkInDate    = checkInDate;
        this.checkOutDate   = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.status         = ReservationStatus.CONFIRMED;
        this.bookingDate    = LocalDate.now().format(DATE_FMT);

        long nights        = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        this.totalAmount   = nights * room.getPricePerNight();
    }

    public Reservation(String reservationId, Customer customer, Room room, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests, ReservationStatus status, double totalAmount, String bookingDate) {
        this.reservationId  = reservationId;
        this.customer       = customer;
        this.room           = room;
        this.checkInDate    = checkInDate;
        this.checkOutDate   = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.status         = status;
        this.totalAmount    = totalAmount;
        this.bookingDate    = bookingDate;
    }


    public String            getReservationId()  { return reservationId; }
    public Customer          getCustomer()       { return customer; }
    public Room              getRoom()           { return room; }
    public LocalDate         getCheckInDate()    { return checkInDate; }
    public LocalDate         getCheckOutDate()   { return checkOutDate; }
    public int               getNumberOfGuests() { return numberOfGuests; }
    public ReservationStatus getStatus()         { return status; }
    public Payment           getPayment()        { return payment; }
    public double            getTotalAmount()    { return totalAmount; }
    public String            getBookingDate()    { return bookingDate; }

    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }


    public void setStatus(ReservationStatus status) { this.status = status; }
    public void setPayment(Payment payment)         { this.payment = payment; }


    public void cancel() {
        if (this.status == ReservationStatus.CANCELLED) {
            System.out.println("  Reservation is already cancelled.");
            return;
        }
        this.status = ReservationStatus.CANCELLED;
        this.room.setAvailable(true);

        if (payment != null && payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            payment.processRefund();
        }
        System.out.println("  Reservation " + reservationId + " has been cancelled.");
    }

    public void checkIn() {
        if (status != ReservationStatus.CONFIRMED)
            throw new IllegalStateException("Cannot check in: reservation is " + status);
        this.status = ReservationStatus.CHECKED_IN;
        System.out.println("  Guest " + customer.getName() +
                " has checked into Room " + room.getRoomNumber() + ".");
    }


    public void checkOut() {
        if (status != ReservationStatus.CHECKED_IN)
            throw new IllegalStateException("Cannot check out: reservation is " + status);
        this.status = ReservationStatus.CHECKED_OUT;
        this.room.setAvailable(true);
        System.out.println("  Guest " + customer.getName() +
                " has checked out from Room " + room.getRoomNumber() + ".");
    }


    public String getDetails() {
        return String.format(
                "\n  ======= RESERVATION DETAILS ======="   +
                        "\n  Reservation ID  : %s"                  +
                        "\n  Booking Date    : %s"                  +
                        "\n  Status          : %s"                  +
                        "\n  --- Guest Info ---"                    +
                        "\n  Name            : %s"                  +
                        "\n  Customer ID     : %s"                  +
                        "\n  Phone           : %s"                  +
                        "\n  --- Room Info ---"                     +
                        "\n  Room Number     : %d"                  +
                        "\n  Room Type       : %s"                  +
                        "\n  Amenities       : %s"                  +
                        "\n  --- Stay Info ---"                     +
                        "\n  Check-In        : %s"                  +
                        "\n  Check-Out       : %s"                  +
                        "\n  No. of Nights   : %d"                  +
                        "\n  No. of Guests   : %d"                  +
                        "\n  Rate/Night      : Rs. %.2f"            +
                        "\n  Total Amount    : Rs. %.2f"            +
                        "\n  ===================================",
                reservationId, bookingDate, status,
                customer.getName(), customer.getCustomerId(), customer.getPhone(),
                room.getRoomNumber(), room.getRoomType().getDisplayName(), room.getAmenities(),
                checkInDate.format(DATE_FMT), checkOutDate.format(DATE_FMT),
                getNumberOfNights(), numberOfGuests,
                room.getPricePerNight(), totalAmount
        );
    }

    public String toCSV() {
        return reservationId + "," +
                customer.getCustomerId() + "," +
                room.getRoomNumber() + "," +
                checkInDate.format(DATE_FMT) + "," +
                checkOutDate.format(DATE_FMT) + "," +
                numberOfGuests + "," +
                status.name() + "," +
                totalAmount + "," +
                bookingDate;
    }

    @Override
    public String toString() {
        return "Reservation[" + reservationId + ", Room " +
                room.getRoomNumber() + ", " + customer.getName() + ", " + status + "]";
    }
}