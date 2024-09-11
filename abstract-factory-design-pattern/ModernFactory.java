class ModernFactory implements FurnitureFactory {
    @Override
    public Chair createChair() {
        return new ModernChair();
    }

    @Override
    public Bed createBed() {
        return new ModernBed();
    }
}