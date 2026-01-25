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

class Car extends Vehicle {
    public Car(String licenseNumber) {
        super(licenseNumber, VehicleType.CAR);
    }
}

class Bike extends Vehicle {
    public Bike(String licenseNumber) {
        super(licenseNumber, VehicleType.BIKE);
    }
}

class Truck extends Vehicle {
    public Truck(String licenseNumber){
        super(licenseNumber, VechicleType.TRUCK);
    }
}


public class ParkingSpot {
    private ParkingSpotType type;
    private Vehicle vehicle;
    private boolean isFree;

    public ParkingSpot(ParkingSpotType type){
        this.type = type;
        this.isFree = true;
    }
    
    public ParkingSpotType getType() {
        return this.type;
    }

    public boolean canFitVehicle(Vehicle vehicle){
        return isFree && this.type.equals(vehicle.getType());
    }

    public void parkVehicle(Vehicle vehicle){
        this.vehicle = vehicle;
        this.isFree = false;
    }

    public void unParkVehicle(Vehicle vehicle){
        this.vehicle = null;
        this.isFree = true;
    }
}

public class ParkingFloor {
  private String floorId;
  private List<ParkingSpot> parkingSpots;
    
  public Optional<ParkingSpot> getAvailableSpot(Vehicle vehicle) {
      for(ParkingSpot parkingSpot : parkingSpots){
          if(parkingSpot.canFitVehicle(vehicle){
              return parkingSpot;
          }
      }
      return Optional.empty();
  }
}

public ParkingLot {
    private List<ParkingFloor> parkingFloors;
    private FeeCalculator feeCalculator;

    public ParkingLot(List<ParkingFloor> parkingFloors,FeeCalculator feeCalculator){
        this.parkingFloors = parkingFloors;
        this.feeCalculator = feeCalculator;
    }

    public ParkngTicket parkVehicle(Vehicle vehicle){
        for(ParkingFloor parkingFloor : parkingFloors){'
            ParkingSpot parkingSpot = getAvailableSpot(vehicle);
            if(parkingSpot.isPresent()){
                parkingSpot.parkVehicle(vehicle);
                return ParkingTicket(UUID.random().toString(),parkingSpot);
            }
        }
        throw new ParkingSpotNotFoundException("No parking spot available);
    }

    public double unParkVehicle(ParkngTicket ParkngTicket){
        long exitTime = System.currrentTimeMillis();
        return feeCalculator.CalculateFee(exitTime,parkingTicket.getEntryTime());
    }
    

}

class ParkingTicket {
    private String ticketId;
    private long entryTime;
    private ParkingSpot spot;

    public ParkingTicket(String ticketId, ParkingSpot spot) {
        this.ticketId = ticketId;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
    }

    public long getEntryTime() {
        return entryTime;
    }

    public ParkingSpot getSpot() {
        return spot;
    }
}

interface FeeCalculator {
    double calculateFee(long entryTime, long exitTime);
}

class HourlyFeeCalculator implements FeeCalculator {
    private static final double RATE_PER_HOUR = 50;

    @Override
    public double calculateFee(long entryTime, long exitTime) {
        long duration = exitTime - entryTime;
        long hours = (long) Math.ceil(duration / (1000.0 * 60 * 60));
        return hours * RATE_PER_HOUR;
    }
}


