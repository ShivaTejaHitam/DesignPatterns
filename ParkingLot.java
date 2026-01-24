public enum VehicleType {
    BIKE, CAR, TRUCK
}

public enum ParkingSpotType {
    BIKE, CAR, TRUCK
}

public enum TicketStatus {
    ACTIVE, PAID, LOST
}

public enum PaymentStatus {
    SUCCESS, FAILED, PENDING
}


public abstract class Vehicle {
  private String licenseNumber;
  private VechicleType type;

  public Vehicle(String licenseNumber, VehicleType type) {
        this.licenseNumber = licenseNumber;
        this.type = type;
    }

    public VehicleType getType() {
        return type;
    }
  
}

public class ParkingSpot {

}

public abstract class ParkingFloor {
  private String floorNumber;
  private List<ParkingSpot> parkingSpots;

  // find free spot
  
}

public 
