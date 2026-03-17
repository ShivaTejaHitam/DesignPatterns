public class TokenBucketAlgorithm {
    private final double refillRatePerSecond;
    private final long capacity;
    private long lastRefillTs;
    private double currentTokens;

    public TokenBucketAlgorithm(double refillRatePerSecond,long capacity) {
        this.refillRatePerSecond = refillRatePerSecond;
        this.capacity = capacity;
        this.currentTokens = capacity;
        this.lastRefillTs = System.nanoTime();
    }

    public synchronized boolean allowRequest(){
        refill();

        if(currentTokens >= 1){
            currentTokens--;
            return true;
        }

        return false;
    }

    public void refill(){
        long elapsedTime = System.nanoTime() - lastRefillTs;
        double tokensToRefill = refillRatePerSecond * (elapsedTime/1_000_000_000.0);

        if(tokensToRefill > 0){
            currentTokens = Math.min(capacity, currentTokens + tokensToRefill);
            lastRefillTs = System.nanoTime();
        }

    } 

    public static void main(String[] args) throws InterruptedException{

        TokenBucketAlgorithm bucket = new TokenBucketAlgorithm(0.5, 1);

        for(int i = 0; i < 10 ; i++){
            if(bucket.allowRequest()){
                System.out.println("Request :" + i + " Allowed");
            } else{
                System.out.println("Request :" + i + " Rejected");
            }
            Thread.sleep(1000);
        }
        
    }
}


import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterService {
    // Maps UserID -> TokenBucket
    private final ConcurrentHashMap<String, TokenBucket> userBuckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String userId) {
        // Get the bucket for this user, or create a new one if it doesn't exist
        TokenBucket bucket = userBuckets.computeIfAbsent(userId, k -> new TokenBucket(10, 2.0));
        
        return bucket.allowRequest();
    }
}
