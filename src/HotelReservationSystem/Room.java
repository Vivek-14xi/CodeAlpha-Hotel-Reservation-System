package HotelReservationSystem;

public class Room {
   public enum RoomType{
       STANDARD("Standard",2500),
       DELUXE("Deluxe",5000),
       SUITE("Suite",10000);

       private final String displayName;
       private final double pricePerNight;

        RoomType(String displayName,double pricePerNight){
           this.displayName=displayName;
           this.pricePerNight=pricePerNight;
       }
       public String getDisplayName(){
            return displayName;
       }

       public double getPricePerNight() {
           return pricePerNight;
       }
   }
   private final int roomNumber;
   private final RoomType roomType;
   private boolean isAvailable;
   private final int maxOccupancy;
   private final String amenities;

   public Room(int roomNumber, RoomType roomType, boolean isAvailable, int maxOccupancy, String amenities) {
       this.roomNumber = roomNumber;
       this.roomType = roomType;
       this.isAvailable = isAvailable;
       this.maxOccupancy = switch (roomType) {
           case STANDARD -> 2;
           case DELUXE -> 3;
           case SUITE -> 5;
       };
       this.amenities=switch (roomType){
           case STANDARD -> "TV,Wifi,AC";
           case DELUXE -> "TV,Wifi,AC,City View";
           case SUITE -> "TV,Wifi,AC, Mini-bar,kitchen";
       };
   }

   public int getRoomNumber(){
       return roomNumber;
   }
   public RoomType getRoomType(){
       return roomType;
   }
   public boolean isAvailable(){
       return isAvailable;
   }
   public int getMaxOccupancy(){
       return maxOccupancy;
   }
   public String getAmenities(){
       return amenities;
   }

   public void setAvailable(boolean available){
       this.isAvailable=available;
   }
   public String getSummary(){
       String status = isAvailable?"AVAILABLE":"BOOKED ";
       return String.format(
               "Room %-4d | %-8s | Rs.%-8.0f/night | Max: %d guests | [%s]",
               roomNumber,
               roomType.getDisplayName(),
               roomType.getPricePerNight(),
               maxOccupancy,
               status
       );
   }
   public String getDetails(){
       return String.format(
               "\n  Room Number   : %d"   +
                       "\n  Type          : %s"   +
                       "\n  Price/Night   : Rs. %.2f" +
                       "\n  Max Occupancy : %d persons" +
                       "\n  Amenities     : %s"   +
                       "\n  Status        : %s",
               roomNumber,
               roomType.getDisplayName(),
               roomType.getPricePerNight(),
               maxOccupancy,
               amenities,
               isAvailable ? "Available" : "Booked"
       );
   }
   public String  toCSV(){
       return roomNumber + ","+ roomType.name()+","+isAvailable;
   }
    @Override
    public String toString() {
        return "Room{" +
                "roomType=" + roomType +
                '}';
    }
}
