package HotelReservationSystem;



import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class HotelManagementSystem {

    private final Map<Integer, Room>        rooms        = new LinkedHashMap<>();
    private final Map<String, Customer>     customers    = new HashMap<>();
    private final Map<String, Reservation>  reservations = new LinkedHashMap<>();
    private final Map<String, Payment>      payments     = new HashMap<>();

    private final Scanner scanner = new Scanner(System.in);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private void initialiseRooms() {
        for (int i = 101; i <= 108; i++)
            rooms.put(i, new Room(i, Room.RoomType.STANDARD));
        for (int i = 201; i <= 206; i++)
            rooms.put(i, new Room(i, Room.RoomType.DELUXE));
        for (int i = 301; i <= 305; i++)
            rooms.put(i, new Room(i, Room.RoomType.SUITE));
    }

    private void loadData() {
        FileManager.loadRooms(rooms);

        Map<String, Customer> savedCustomers = FileManager.loadCustomers();
        customers.putAll(savedCustomers);

        Map<String, Reservation> savedRes =
                FileManager.loadReservations(rooms, customers);
        reservations.putAll(savedRes);

        Map<String, Payment> savedPayments = FileManager.loadPayments();
        payments.putAll(savedPayments);

        for (Payment p : payments.values()) {
            Reservation res = reservations.get(p.getReservationId());
            if (res != null) res.setPayment(p);
        }

        System.out.println("  [System] Data loaded from files.");
    }

    private void saveAllData() {
        FileManager.saveRooms(rooms);
        FileManager.saveCustomers(customers);
        FileManager.saveReservations(reservations);
        FileManager.savePayments(payments);
        System.out.println("  [System] Data saved successfully.");
    }

    public void start() {
        printHotelBanner();
        initialiseRooms();
        loadData();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1  -> searchAvailableRooms();
                case 2  -> bookRoom();
                case 3  -> cancelReservation();
                case 4  -> viewBookingDetails();
                case 5  -> viewAllReservations();
                case 6  -> checkInGuest();
                case 7  -> checkOutGuest();
                case 8  -> viewRoomStatus();
                case 9  -> viewCustomerHistory();
                case 10 -> { saveAllData(); running = false; printGoodbye(); }
                default -> System.out.println("  Invalid choice. Please select 1-10.");
            }
        }
        scanner.close();
    }

    private void printMainMenu() {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("          Select the option for Hotel Booking ");
        System.out.println("═".repeat(50));
        System.out.println("  1.  Search Available Rooms");
        System.out.println("  2.  Book a Room");
        System.out.println("  3.  Cancel Reservation");
        System.out.println("  4.  View Booking Details");
        System.out.println("  5.  View All Reservations");
        System.out.println("  6.  Check-In Guest");
        System.out.println("  7.  Check-Out Guest");
        System.out.println("  8.  View Room Status");
        System.out.println("  9.  Customer History");
        System.out.println("  10. Save & Exit");
        System.out.println("═".repeat(50));
    }

    private void searchAvailableRooms() {
        printSectionHeader("SEARCH AVAILABLE ROOMS");

        System.out.println("  Filter by Room Type:");
        System.out.println("  1. All types");
        System.out.println("  2. Standard  (Rs.2500/night, max 2 guests)");
        System.out.println("  3. Deluxe    (Rs.5000/night, max 3 guests)");
        System.out.println("  4. Suite     (Rs.10000/night, max 5 guests)");
        int typeChoice = readInt("  Your choice (1-4): ");

        Room.RoomType filterType = switch (typeChoice) {
            case 2 -> Room.RoomType.STANDARD;
            case 3 -> Room.RoomType.DELUXE;
            case 4 -> Room.RoomType.SUITE;
            default -> null;
        };

        List<Room> available = rooms.values().stream()
            .filter(Room::isAvailable)
            .filter(r -> filterType == null || r.getRoomType() == filterType)
            .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("\n  No rooms available for the selected type.");
        } else {
            System.out.println("\n  Available Rooms (" + available.size() + " found):");
            System.out.println("  " + "-".repeat(70));
            available.forEach(r -> System.out.println("  " + r.getSummary()));
            System.out.println("  " + "-".repeat(70));
        }
    }


    private void bookRoom() {
        printSectionHeader("BOOK A ROOM");

        // ── Step 1: Customer details ─────────────────────────────────────────
        Customer customer = getOrCreateCustomer();
        if (customer == null) return;

        // ── Step 2: Show available rooms ─────────────────────────────────────
        List<Room> availableRooms = rooms.values().stream()
            .filter(Room::isAvailable).collect(Collectors.toList());

        if (availableRooms.isEmpty()) {
            System.out.println("\n  Sorry, no rooms are currently available.");
            return;
        }

        System.out.println("\n  Available Rooms:");
        System.out.println("  " + "-".repeat(70));
        availableRooms.forEach(r -> System.out.println("  " + r.getSummary()));
        System.out.println("  " + "-".repeat(70));

        int roomNumber = readInt("  Enter Room Number to book: ");
        Room selectedRoom = rooms.get(roomNumber);
        if (selectedRoom == null || !selectedRoom.isAvailable()) {
            System.out.println("  Invalid room number or room is not available.");
            return;
        }

        // ── Step 3: Stay dates ───────────────────────────────────────────────
        LocalDate checkIn  = readDate("  Check-In  Date (dd-MM-yyyy): ");
        LocalDate checkOut = readDate("  Check-Out Date (dd-MM-yyyy): ");
        if (checkIn == null || checkOut == null) return;

        int guests = readInt("  Number of Guests (max " + selectedRoom.getMaxOccupancy() + "): ");

        // ── Step 4: Create reservation ───────────────────────────────────────
        Reservation reservation;
        try {
            reservation = new Reservation(customer, selectedRoom, checkIn, checkOut, guests);
        } catch (IllegalArgumentException e) {
            System.out.println("  Booking error: " + e.getMessage());
            return;
        }

        // ── Step 5: Show cost summary and confirm ────────────────────────────
        System.out.printf("%n  Booking Summary:%n");
        System.out.printf("  Room     : %d (%s)%n",
            selectedRoom.getRoomNumber(), selectedRoom.getRoomType().getDisplayName());
        System.out.printf("  Nights   : %d%n", reservation.getNumberOfNights());
        System.out.printf("  Total    : Rs. %.2f%n", reservation.getTotalAmount());

        String confirm = readString("  Confirm booking? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("  Booking cancelled by user.");
            return;
        }

        // ── Step 6: Payment ──────────────────────────────────────────────────
        Payment.PaymentMode mode = selectPaymentMode();
        Payment payment = new Payment(
            reservation.getReservationId(),
            reservation.getTotalAmount(),
            mode
        );
        boolean paid = payment.processPayment();

        if (!paid) {
            System.out.println("  Booking aborted due to payment failure.");
            return;
        }

        // ── Step 7: Finalise ─────────────────────────────────────────────────
        selectedRoom.setAvailable(false);           // mark room as booked
        reservation.setPayment(payment);
        reservations.put(reservation.getReservationId(), reservation);
        customers.put(customer.getCustomerId(), customer);
        payments.put(payment.getPaymentId(), payment);

        System.out.println(reservation.getDetails());
        System.out.println(payment.getReceipt());
        System.out.println("\n  ✅ Booking CONFIRMED! Reservation ID: " +
                           reservation.getReservationId());
    }


    private Customer getOrCreateCustomer() {
        System.out.println("\n  --- Customer Details ---");
        String phone = readString("  Mobile Number (10 digits): ");

        Optional<Customer> existing = customers.values().stream()
            .filter(c -> c.getPhone().equals(phone)).findFirst();

        if (existing.isPresent()) {
            Customer c = existing.get();
            System.out.println("  Welcome back, " + c.getName() + "! (ID: " + c.getCustomerId() + ")");
            return c;
        }

        // New customer
        String name    = readString("  Full Name          : ");
        String email   = readString("  Email Address      : ");
        String idProof = readString("  ID Proof (Aadhaar/Passport/PAN): ");

        try {
            return new Customer(name, phone, email, idProof);
        } catch (IllegalArgumentException e) {
            System.out.println("  Invalid customer data: " + e.getMessage());
            return null;
        }
    }

    private Payment.PaymentMode selectPaymentMode() {
        System.out.println("\n  Select Payment Mode:");
        System.out.println("  1. Cash");
        System.out.println("  2. Credit Card");
        System.out.println("  3. Debit Card");
        System.out.println("  4. UPI");
        System.out.println("  5. Net Banking");
        int choice = readInt("  Your choice (1-5): ");
        return switch (choice) {
            case 2 -> Payment.PaymentMode.CREDIT_CARD;
            case 3 -> Payment.PaymentMode.DEBIT_CARD;
            case 4 -> Payment.PaymentMode.UPI;
            case 5 -> Payment.PaymentMode.NET_BANKING;
            default -> Payment.PaymentMode.CASH;
        };
    }

    private void cancelReservation() {
        printSectionHeader("CANCEL RESERVATION");

        String resId = readString("  Enter Reservation ID to cancel: ").toUpperCase();
        Reservation res = reservations.get(resId);

        if (res == null) {
            System.out.println("  Reservation not found: " + resId);
            return;
        }
        if (res.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            System.out.println("  This reservation is already cancelled.");
            return;
        }

        System.out.println(res.getDetails());
        String confirm = readString("\n  Are you sure you want to cancel? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("  Cancellation aborted.");
            return;
        }

        res.cancel();   // handles status change, room release, and refund
    }

    private void viewBookingDetails() {
        printSectionHeader("VIEW BOOKING DETAILS");
        String resId = readString("  Enter Reservation ID: ").toUpperCase();
        Reservation res = reservations.get(resId);

        if (res == null) {
            System.out.println("  Reservation not found.");
            return;
        }

        System.out.println(res.getDetails());
        if (res.getPayment() != null) {
            System.out.println(res.getPayment().getReceipt());
        } else {
            System.out.println("  No payment record linked to this reservation.");
        }
    }

    private void viewAllReservations() {
        printSectionHeader("ALL RESERVATIONS");

        if (reservations.isEmpty()) {
            System.out.println("  No reservations found.");
            return;
        }

        System.out.println("  Filter by status:");
        System.out.println("  1. All   2. Confirmed   3. Cancelled   4. Checked-In   5. Checked-Out");
        int filterChoice = readInt("  Your choice: ");

        Reservation.ReservationStatus filterStatus = switch (filterChoice) {
            case 2 -> Reservation.ReservationStatus.CONFIRMED;
            case 3 -> Reservation.ReservationStatus.CANCELLED;
            case 4 -> Reservation.ReservationStatus.CHECKED_IN;
            case 5 -> Reservation.ReservationStatus.CHECKED_OUT;
            default -> null;
        };

        System.out.printf("%n  %-12s %-12s %-20s %-8s %-12s %-12s %-12s%n",
            "ResvID", "RoomNo", "Guest", "Guests", "CheckIn", "CheckOut", "Status");
        System.out.println("  " + "-".repeat(88));

        reservations.values().stream()
            .filter(r -> filterStatus == null || r.getStatus() == filterStatus)
            .forEach(r -> System.out.printf(
                "  %-12s %-12d %-20s %-8d %-12s %-12s %-12s%n",
                r.getReservationId(),
                r.getRoom().getRoomNumber(),
                r.getCustomer().getName(),
                r.getNumberOfGuests(),
                r.getCheckInDate().format(DATE_FMT),
                r.getCheckOutDate().format(DATE_FMT),
                r.getStatus()
            ));
    }

    private void checkInGuest() {
        printSectionHeader("GUEST CHECK-IN");
        String resId = readString("  Enter Reservation ID: ").toUpperCase();
        Reservation res = reservations.get(resId);

        if (res == null) {
            System.out.println("  Reservation not found.");
            return;
        }
        try {
            res.checkIn();
        } catch (IllegalStateException e) {
            System.out.println("  Cannot check in: " + e.getMessage());
        }
    }

    private void checkOutGuest() {
        printSectionHeader("GUEST CHECK-OUT");
        String resId = readString("  Enter Reservation ID: ").toUpperCase();
        Reservation res = reservations.get(resId);

        if (res == null) {
            System.out.println("  Reservation not found.");
            return;
        }
        try {
            res.checkOut();
            System.out.println("  Thank you for staying at Grand Java Hotel!");
        } catch (IllegalStateException e) {
            System.out.println("  Cannot check out: " + e.getMessage());
        }
    }

    private void viewRoomStatus() {
        printSectionHeader("ROOM STATUS DASHBOARD");

        long totalRooms    = rooms.size();
        long availableCount = rooms.values().stream().filter(Room::isAvailable).count();
        long bookedCount   = totalRooms - availableCount;

        System.out.printf("  Total Rooms : %-4d  |  Available : %-4d  |  Booked : %-4d%n",
            totalRooms, availableCount, bookedCount);
        System.out.println("  " + "─".repeat(70));
        System.out.printf("  %-6s %-10s %-12s %-10s %-20s%n",
            "Room", "Type", "Price/Night", "Status", "Amenities");
        System.out.println("  " + "─".repeat(70));

        rooms.values().forEach(r ->
            System.out.printf("  %-6d %-10s Rs.%-9.0f %-10s %-20s%n",
                r.getRoomNumber(),
                r.getRoomType().getDisplayName(),
                r.getPricePerNight(),
                r.isAvailable() ? "Available" : "Booked",
                r.getAmenities()
            )
        );
    }

    private void viewCustomerHistory() {
        printSectionHeader("CUSTOMER RESERVATION HISTORY");

        String input = readString("  Enter Customer ID or Mobile Number: ");
        Customer customer = null;

        // Try by customer ID first
        if (customers.containsKey(input.toUpperCase())) {
            customer = customers.get(input.toUpperCase());
        } else {
            // Fallback: search by phone number
            Optional<Customer> opt = customers.values().stream()
                .filter(c -> c.getPhone().equals(input)).findFirst();
            if (opt.isPresent()) customer = opt.get();
        }

        if (customer == null) {
            System.out.println("  Customer not found.");
            return;
        }

        System.out.println(customer.getDetails());

        String custId = customer.getCustomerId();
        List<Reservation> history = reservations.values().stream()
            .filter(r -> r.getCustomer().getCustomerId().equals(custId))
            .collect(Collectors.toList());

        if (history.isEmpty()) {
            System.out.println("  No reservation history found.");
        } else {
            System.out.println("\n  Reservation History (" + history.size() + " records):");
            history.forEach(r -> {
                System.out.println(r.getDetails());
                if (r.getPayment() != null) System.out.println(r.getPayment().getReceipt());
            });
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            int val = Integer.parseInt(scanner.nextLine().trim());
            return val;
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid number entered. Using 0.");
            return 0;
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return LocalDate.parse(input, DATE_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("  Invalid date format. Please use dd-MM-yyyy.");
            return null;
        }
    }

    private void printSectionHeader(String title) {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("  " + title);
        System.out.println("─".repeat(50));
    }

    private void printHotelBanner() {
        System.out.println("\n" + "═".repeat(55));
        System.out.println("   ┌────────────────────────────────┐");
        System.out.println("   │    HOTEL MANAGEMENT SYSTEM     │");
        System.out.println("   └────────────────────────────────┘");
        System.out.println("          Hotel — Reservation System     ");
        System.out.println("═".repeat(55));
    }

    private void printGoodbye() {
        System.out.println("\n  Thank you for using Grand Java Hotel System.");
        System.out.println("  Have a wonderful day! \n");
    }


    public static void main(String[] args) {

        new HotelManagementSystem().start();
    }
}
