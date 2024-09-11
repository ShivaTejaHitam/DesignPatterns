class VehicleFactory {
    public Vehicle createVehicle(String type){
        if (type == null || type.isEmpty()) {
            return null;
        }
        if ("Car".equalsIgnoreCase(type)) {
            return new Car();
        } else if ("Bike".equalsIgnoreCase(type)) {
            return new Bike();
        }
        return null;
    }
}