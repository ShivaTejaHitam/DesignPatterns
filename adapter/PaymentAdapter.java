public interface PaymentProcessor {
    void pay(int amount);
}

public class RazorPayService {
    public void makePayment(double amount) {
        System.out.println("Paid using RazorPay: " + amount);
    }
}

public class RazorPayAdapter implements PaymentProcessor {
  private final RazorPayService razorPayService;
  
  public RazorPayAdapter(RazorPayService razorPayService){
    this.razorPayService = razorPayService;
  }

  public void pay(int amount){
    this.razorPayService.makePayment(amount);
  }
}

public class PaymentClient {
    public static void main(String[] args) {

        PaymentProcessor paymentProcessor = new RazorPayAdapter(new RazorPayService());

        paymentProcessor.pay(500);
    }
}
