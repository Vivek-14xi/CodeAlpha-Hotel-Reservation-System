package HotelReservationSystem;


import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class FileManager {

    private static final String DATA_DIR        = "data/";
    private static final String ROOMS_FILE      = DATA_DIR + "rooms.csv";
    private static final String CUSTOMERS_FILE  = DATA_DIR + "customers.csv";
    private static final String RESERVATIONS_FILE = DATA_DIR + "reservations.csv";
    private static final String PAYMENTS_FILE   = DATA_DIR + "payments.csv";

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void ensureDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();
    }


    public static void saveRooms(Map<Integer, Room> rooms) {
        ensureDataDirectory();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            bw.write("roomNumber,roomType,isAvailable"); bw.newLine();
            for (Room room : rooms.values()) {
                bw.write(room.toCSV()); bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Error saving rooms: " + e.getMessage());
        }
    }

    public static void saveCustomers(Map<String, Customer> customers) {
        ensureDataDirectory();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CUSTOMERS_FILE))) {
            bw.write("customerId,name,phone,email,idProof"); bw.newLine();
            for (Customer c : customers.values()) {
                bw.write(c.toCSV()); bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Error saving customers: " + e.getMessage());
        }
    }

    public static void saveReservations(Map<String, Reservation> reservations) {
        ensureDataDirectory();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RESERVATIONS_FILE))) {
            bw.write("reservationId,customerId,roomNumber,checkIn,checkOut," +
                    "guests,status,total,bookingDate"); bw.newLine();
            for (Reservation r : reservations.values()) {
                bw.write(r.toCSV()); bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Error saving reservations: " + e.getMessage());
        }
    }


    public static void savePayments(Map<String, Payment> payments) {
        ensureDataDirectory();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PAYMENTS_FILE))) {
            bw.write("paymentId,reservationId,amount,mode,status,timestamp,txnRef");
            bw.newLine();
            for (Payment p : payments.values()) {
                bw.write(p.toCSV()); bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Error saving payments: " + e.getMessage());
        }
    }

    public static void loadRooms(Map<Integer, Room> rooms) {
        File file = new File(ROOMS_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                int     roomNum   = Integer.parseInt(parts[0].trim());
                boolean available = Boolean.parseBoolean(parts[2].trim());
                Room room = rooms.get(roomNum);
                if (room != null) room.setAvailable(available);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("[FileManager] Error loading rooms: " + e.getMessage());
        }
    }

    public static Map<String, Customer> loadCustomers() {
        Map<String, Customer> customers = new HashMap<>();
        File file = new File(CUSTOMERS_FILE);
        if (!file.exists()) return customers;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", 5); // limit 5: name may have commas
                if (parts.length < 5) continue;
                Customer c = new Customer(
                        parts[0].trim(), parts[1].trim(),
                        parts[2].trim(), parts[3].trim(), parts[4].trim()
                );
                customers.put(c.getCustomerId(), c);
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Error loading customers: " + e.getMessage());
        }
        return customers;
    }

    public static Map<String, Reservation> loadReservations(Map<Integer, Room> rooms, Map<String, Customer> customers) {

        Map<String, Reservation> reservations = new LinkedHashMap<>();
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists()) return reservations;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(",");
                if (p.length < 9) continue;

                String   resId      = p[0].trim();
                Customer customer   = customers.get(p[1].trim());
                Room     room       = rooms.get(Integer.parseInt(p[2].trim()));
                LocalDate checkIn   = LocalDate.parse(p[3].trim(), DATE_FMT);
                LocalDate checkOut  = LocalDate.parse(p[4].trim(), DATE_FMT);
                int       guests    = Integer.parseInt(p[5].trim());
                Reservation.ReservationStatus status =
                        Reservation.ReservationStatus.valueOf(p[6].trim());
                double   total      = Double.parseDouble(p[7].trim());
                String   bookDate   = p[8].trim();

                if (customer == null || room == null) continue; // skip orphaned records

                Reservation res = new Reservation(
                        resId, customer, room, checkIn, checkOut,
                        guests, status, total, bookDate
                );
                reservations.put(resId, res);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("[FileManager] Error loading reservations: " + e.getMessage());
        }
        return reservations;
    }

    public static Map<String, Payment> loadPayments() {
        Map<String, Payment> payments = new HashMap<>();
        File file = new File(PAYMENTS_FILE);
        if (!file.exists()) return payments;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(",", 7);
                if (p.length < 7) continue;
                Payment pay = new Payment(
                        p[0].trim(), p[1].trim(),
                        Double.parseDouble(p[2].trim()),
                        Payment.PaymentMode.valueOf(p[3].trim()),
                        Payment.PaymentStatus.valueOf(p[4].trim()),
                        p[5].trim(), p[6].trim()
                );
                payments.put(pay.getPaymentId(), pay);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("[FileManager] Error loading payments: " + e.getMessage());
        }
        return payments;
    }
}