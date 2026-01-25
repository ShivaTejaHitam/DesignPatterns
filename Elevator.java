
public enum Direction {
   UP,
   DOWN,
   IDLE
}

public enum ElevatorState {
  MOVING,
  STOPPED,
  IDLE
}

public class ExternalRequest {
   private int floor;
   private Direction direction;

   public ExternalRequest(int floor, Direction direction){
     this.floor = floor;
     this.direction = direction;
   }
}

class InternalRequest {
    int destinationFloor;

    public InternalRequest(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }
}

class Elevator implements Runnable {
  private final int id;
  private int currentFloor;
  private Direction direction;
  private ElevatorState state;

  private TreeSet<Integer> upQueue = new TreeSet();
  private TreeSet<Integer> downQueue = new TreeSet(Collections.reverseOrder());

  public Elevator(int id) {
        this.id = id;
        this.currentFloor = 0;
        this.direction = Direction.IDLE;
        this.state = ElevatorState.IDLE;
  }

  public void synchronized addStop(int floor){
    if(floor == currentFloor) return;
    if(floor < currentFloor) {
      downQueue.add(floor);
    } else {
      upQueue.add(floor);
    }
    notify();
  }

  public void addInternalRequest(InternalRequest request) {
        addStop(request.destinationFloor);
    }

    public void addExternalRequest(ExternalRequest request) {
        addStop(request.floor);
    }


  @Override
  public void run() {
    while(true){
      processRequests();
    }
  }

  private synchronized processRequests() {
    try{
      while(upQueue.isEmpty() && downQueue.isEmpty()){
        this.state = IDLE;
        this.direction = IDLE;
        wait();
      }
  
      if(!upQueue.isEmpty()){
        this.direction = DIRECTION.UP;
        moveUp();
      } else if(!downQueue.isEmpty()){
        this.direction = DIRECTION.DOWN;
        moveDown();
      }
    } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
    }
  }

  private void moveUp(){
    while(!upQueue.isEmpty()){
      moveToFloor(upQueue.pollFirst());
    }
  }

  private void moveDown() {
        while (!downQueue.isEmpty()) {
            moveToFloor(downQueue.pollFirst());
        }
  }

  private void moveToFloor(int floor) {
    state = ElevatorState.MOVING;
    System.out.println("Elevator " + id +
                " moving " + direction +
                " to floor " + floor);

    currentFloor = floor;
    state = ElevatorState.STOPPED;
  }

  public int getCurrentFloor() {
        return currentFloor;
    }

  public Direction getDirection() {
        return direction;
    }
    
  public boolean isIdle() {
        return state == ElevatorState.IDLE;
  }
}

class Dispatcher {
  
  private final List<Elevator> elevators;

  public Dispatcher(List<Elevator> elevators) {
    this.elevators = elevators;
  }

  public void submitExternalRequest(ExternalRequest request){
    Elevator elevator = selectBestElevator(request);
    elevator.addExternalRequest(request);
  }

  private Elevator selectBestElevator(ExternalRequest request) {
    Elevator bestElevator = null;
    int minDistance = Integer.MAX_VALUE;
    
    for(Elevator elevator : elevators){
      if(isEligible(elevator,request.getDirection())){
        int distance = Math.abs(request.floor - elevator.currentFloor);
        if(distance < minDistance){
          minDistance = distance;
          bestElevator = elevator;
        }
      }
    }
    
    return bestElevator != null ? bestElevator : elevators.get(0);
  }


  private boolean isEligible(Elevator elevator, Direction direction) {

    if(elevator.getDirection == Direction.IDLE) return true;

    if(elevator.getDirection == direction) {
      if (request.direction == Direction.UP &&
                    elevator.getCurrentFloor() <= request.floor) {
                return true;
          }

      if (request.direction == Direction.DOWN &&
                    elevator.getCurrentFloor() >= request.floor) {
                return true;
      }

    }
    return false;
    
}

class ElevatorSystem {

    private final Dispatcher dispatcher;
    private final List<Elevator> elevators = new ArrayList<>();

    public ElevatorSystem(int elevatorCount) {
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = new Elevator(i);
            elevators.add(elevator);
            new Thread(elevator).start();
        }
        dispatcher = new Dispatcher(elevators);
    }

    public void requestElevator(int floor, Direction direction) {
        dispatcher.submitExternalRequest(
                new ExternalRequest(floor, direction)
        );
    }

    public Elevator getElevator(int id) {
        return elevators.get(id);
    }
}

public class Main {
    public static void main(String[] args) {

        ElevatorSystem system = new ElevatorSystem(3);

        system.requestElevator(3, Direction.UP);
        system.requestElevator(8, Direction.DOWN);

        Elevator elevator = system.getElevator(0);
        elevator.addInternalRequest(new InternalRequest(7));
    }
}
