public class Computer {
    // Required parameters
    private int CPU;
    private int RAM;

    // Optional parameters
    private int storage;
    private String graphicsCard;

    // Constructor
    public Computer(int CPU, int RAM, int storage, String graphicsCard) {
        this.CPU = CPU;
        this.RAM = RAM;
        this.storage = storage;
        this.graphicsCard = graphicsCard;
    }

    // Getters for all attributes
    public int getCPU() {
        return CPU;
    }

    public int getRAM() {
        return RAM;
    }

    public int getStorage() {
        return storage;
    }

    public String getGraphicsCard() {
        return graphicsCard;
    }

    @Override
    public String toString() {
        return "Computer [CPU=" + CPU + ", RAM=" + RAM + ", storage=" + storage + ", graphicsCard=" + graphicsCard + "]";
    }
}
