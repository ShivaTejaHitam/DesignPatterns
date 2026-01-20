public class Item {
    private final String code;
    private final String name;
    private final int price;

    public Item(String code, String name, int price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public int getPrice() { return price; }
}


public class Inventory {
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Item> items = new HashMap<>();

    public void addItem(Item item, int quantity) {
        items.put(item.getCode(), item);
        stock.put(item.getCode(), quantity);
    }

    public boolean isAvailable(String code) {
        return stock.getOrDefault(code, 0) > 0;
    }

    public void reduceStock(String code) {
        stock.put(code, stock.get(code) - 1);
    }

    public Item getItem(String code) {
        return items.get(code);
    }
}

public interface State {
    void insertMoney(int amount);
    void selectItem(String code);
    void dispense();
}

public class VendingMachine {
    private State idleState;
    private State hasMoneyState;
    private State dispenseState;

    private State currentState;
    private Inventory inventory;
    private int balance;
    private Item selectedItem;

    public VendingMachine() {
        inventory = new Inventory();
        idleState = new IdleState(this);
        hasMoneyState = new HasMoneyState(this);
        dispenseState = new DispenseState(this);
        currentState = idleState;
    }

    public void insertMoney(int amount) {
        currentState.insertMoney(amount);
    }

    public void selectItem(String code) {
        currentState.selectItem(code);
    }

    public void dispense() {
        currentState.dispense();
    }

    // getters & setters
    public void setState(State state) { this.currentState = state; }
    public State getIdleState() { return idleState; }
    public State getHasMoneyState() { return hasMoneyState; }
    public State getDispenseState() { return dispenseState; }

    public Inventory getInventory() { return inventory; }
    public int getBalance() { return balance; }
    public void addBalance(int amt) { balance += amt; }
    public void deductBalance(int amt) { balance -= amt; }

    public Item getSelectedItem() { return selectedItem; }
    public void setSelectedItem(Item item) { selectedItem = item; }

    public void reset() {
        balance = 0;
        selectedItem = null;
        setState(idleState);
    }
}


public class IdleState implements State {
    private final VendingMachine machine;

    public IdleState(VendingMachine machine) {
        this.machine = machine;
    }

    public void insertMoney(int amount) {
        machine.addBalance(amount);
        machine.setState(machine.getHasMoneyState());
    }

    public void selectItem(String code) {
        System.out.println("Insert money first");
    }

    public void dispense() {
        System.out.println("No money inserted");
    }
}

public class HasMoneyState implements State {
    private final VendingMachine machine;

    public HasMoneyState(VendingMachine machine) {
        this.machine = machine;
    }

    public void insertMoney(int amount) {
        machine.addBalance(amount);
    }

    public void selectItem(String code) {
        Inventory inventory = machine.getInventory();

        if (!inventory.isAvailable(code)) {
            System.out.println("Item out of stock");
            return;
        }

        Item item = inventory.getItem(code);

        if (machine.getBalance() < item.getPrice()) {
            System.out.println("Insufficient balance");
            return;
        }

        machine.setSelectedItem(item);
        machine.setState(machine.getDispenseState());
    }

    public void dispense() {
        System.out.println("Select item first");
    }
}

public class DispenseState implements State {
    private final VendingMachine machine;

    public DispenseState(VendingMachine machine) {
        this.machine = machine;
    }

    public void insertMoney(int amount) {
        System.out.println("Dispensing in progress");
    }

    public void selectItem(String code) {
        System.out.println("Already selected");
    }

    public void dispense() {
        Item item = machine.getSelectedItem();
        Inventory inventory = machine.getInventory();

        inventory.reduceStock(item.getCode());
        machine.deductBalance(item.getPrice());

        System.out.println("Dispensed: " + item.getName());
        System.out.println("Change returned: " + machine.getBalance());

        machine.reset();
    }
}

public class Main {
    public static void main(String[] args) {
        VendingMachine vm = new VendingMachine();

        vm.getInventory().addItem(new Item("C1", "Coke", 25), 5);
        vm.getInventory().addItem(new Item("P1", "Pepsi", 20), 3);

        vm.insertMoney(30);
        vm.selectItem("C1");
        vm.dispense();
    }
}
