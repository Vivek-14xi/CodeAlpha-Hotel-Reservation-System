package HotelReservationSystem;

public class Customer {
    private final String customerId;
    private  String name;
    private String phone;
    private String email;
    private String idProof;
    private static int idCounter=1000;
    public Customer(String name, String phone ,String idProof){
        this.customerId="CUST"+(++idCounter);

    }
    public Customer(String customerId,String name, String phone,String email,String idProof){
        this.customerId=customerId;
        this.phone=phone;
        this.email=email;
        this.idProof=idProof;
    }

    public String getCustomerId(){
        return customerId;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public static int getIdCounter() {
        return idCounter;
    }
    public String getIdProof() {
        return idProof;
    }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Customer name cannot be empty;");
        this.name = name.trim();
    }

    public void setPhone(String phone) {
        if (phone==null || phone.matches("\\d{10}"))
            throw new IllegalArgumentException("phone must be exactly 10 digits.");
        this.phone = phone.trim();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }
}
