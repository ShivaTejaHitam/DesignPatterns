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
    

    @Autowired
    PaymentProcessor paymentProcessor;


    public void pay(double amount){
        paymentProcessor.pay(amount);
    }
    
}
