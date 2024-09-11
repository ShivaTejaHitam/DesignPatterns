/* why builder design pattern ? 
    when only some properties of a class are needed , we tend to pass nulls to constructor which is pretty ugly.
    If you want to extend the parent class for introducing new properties or methods , then it would be huge number of subclasses required.

  How it is implemented ?
    The Builder pattern suggests that you extract the object construction code out of its own class and move it to separate objects called builders.
    The pattern organizes object construction into a set of steps (buildWalls, buildDoor, etc.). 
        To create an object, you execute a series of these steps on a builder object. The important part is that you don’t need to call all of the steps. 
        You can call only those steps that are necessary for producing a particular configuration of an object.
    You can pass different builders to director which is responsible for executing steps in order.
    Builder design pattern can be implemented with or without Director. When Director is not used we can call builder in client code itself
*/


public class ComputerBuilder {
    private int CPU;
    private int RAM;
    private int storage;
    private String graphicsCard;

    public ComputerBuilder() {

    }

    public ComputerBuilder setCPU(int CPU) {
        this.CPU = CPU;
        return this;
    }

    public ComputerBuilder setRAM(int RAM) {
        this.RAM = RAM;
        return this;
    }

    public ComputerBuilder setStorage(int storage){
        this.storage = storage;
        return this;
    }

    public ComputerBuilder setGraphicsCard(String graphicsCard){
        this.graphicsCard = graphicsCard;
        return this;
    }

    public Computer build() {
        return new Computer(CPU,RAM,storage,graphicsCard);
    }
}
