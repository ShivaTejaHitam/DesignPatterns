class Ikea{
    private Chair chair;
    private Bed bed;

    public Ikea(FurnitureFactory factory){
        this.chair = factory.createChair();
        this.bed = factory.createBed();
    }

    public void display() {
        chair.display();
        bed.display();
    }
}