package HotelReservationSystem;

public class Customer {

    private final String customerId;
    private String name;
    private String phone;
    private String email;
    private String idProof;

    private static int idCounter = 1000;

    public Customer(String name, String phone, String email, String idProof) {
        this.customerId = "CUST" + (++idCounter);
        setName(name);
        setPhone(phone);
        setEmail(email);
        this.idProof = idProof;
    }
    public Customer(String customerId, String name, String phone, String email, String idProof) {
        this.customerId = customerId;
        this.name       = name;
        this.phone      = phone;
        this.email      = email;
        this.idProof    = idProof;
    }

    public String getCustomerId(){
         return customerId;
    }
    public String getName(){
        return name;
    }
    public String getPhone(){
        return phone;
    }
    public String getEmail(){
        return email;
    }
    public String getIdProof(){
        return idProof;
    }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Customer name cannot be empty.");
        this.name = name.trim();
    }
    public void setPhone(String phone) {
        // Accept exactly 10 digits
        if (phone == null || !phone.matches("\\d{10}"))
            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        this.phone = phone;
    }
    public void setEmail(String email) {
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email address.");
        this.email = email.trim().toLowerCase();
    }
    public void setIdProof(String idProof) { this.idProof = idProof; }

    public String getDetails() {
        return String.format(
                "\n  Customer ID : %s"  +
                        "\n  Name        : %s"  +
                        "\n  Phone       : %s"  +
                        "\n  Email       : %s"  +
                        "\n  ID Proof    : %s",
                customerId, name, phone, email, idProof
        );
    }

    public String toCSV() {
        return customerId + "," + name + "," + phone + "," + email + "," + idProof;
    }
    @Override
    public String toString() {
        return "Customer[" + customerId + ", " + name + "]";
    }
}