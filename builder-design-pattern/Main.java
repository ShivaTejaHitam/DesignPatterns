class Main {
    public static void main(String[] args) {
        Computer computer = new ComputerBuilder()
                                .setRAM(4)
                                .setCPU(2)
                                .setGraphicsCard("Intel")
                                .setStorage(4)
                                .build();
        System.out.println(computer);
    }
}