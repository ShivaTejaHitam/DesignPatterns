class AntiqueFactory implements FurnitureFactory{
    @Override
    public Chair createChair() {
        return new AntiqueChair();
    }

    @Override
    public Bed createBed() {
        return new AntiqueBed();
    }
}