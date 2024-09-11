import java.util.Scanner;

class Uber {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the vehicle type:");
        String vehicleType = scanner.nextLine();
        VehicleFactory vehicleFactory = new VehicleFactory();
        Vehicle vehicle = vehicleFactory.createVehicle(vehicleType);
        vehicle.startRide();
    }
}