Designing a rate limiter is a classic system design challenge. Its primary goal is to protect your services from being overwhelmed by too many requests—whether from a malicious DDoS attack or just an enthusiastic (but buggy) script.

---

## 1. Core Requirements
Before diving into the "how," we must define the "what":
* **Accuracy:** Requests should be throttled as soon as the limit is hit.
* **Low Latency:** The limiter should not add significant overhead to the API response time.
* **Distributed Scalability:** It must work across multiple server instances.
* **High Availability:** If the rate limiter fails, the entire system shouldn't go down (fail-open vs. fail-closed).

---

## 2. Where to Place the Rate Limiter?
You have three main options for where the logic lives:
1.  **Client-Side:** Unreliable, as malicious actors can easily bypass it.
2.  **Server-Side:** Integrated into your application code.
3.  **Middleware/API Gateway:** The most common approach. Tools like **Nginx**, **AWS API Gateway**, or **Kong** sit in front of your services to handle throttling before requests even reach your backend.



---

## 3. Rate Limiting Algorithms
Choosing the right algorithm depends on your need for accuracy vs. memory efficiency.

### **Token Bucket** (Most Popular)
* **How it works:** A "bucket" holds a maximum number of tokens. Each request consumes one token. Tokens are refilled at a fixed rate.
* **Pros:** Allows for **bursts** of traffic (as long as tokens are in the bucket) while maintaining a long-term average rate.
* **Used by:** Amazon, Stripe.



### **Leaky Bucket**
* **How it works:** Requests enter a "bucket" (queue) and are processed at a **constant, fixed rate**. If the bucket overflows, requests are dropped.
* **Pros:** Smooths out traffic into a steady stream.
* **Cons:** Not great for bursty traffic; requests may be delayed even if resources are available.



### **Fixed Window Counter**
* **How it works:** Time is divided into fixed intervals (e.g., 1 minute). Each window has a counter.
* **Cons:** "Edge Case" problem—if a user sends 100 requests at 11:59:59 and 100 more at 12:00:01, they effectively sent 200 requests in 2 seconds, potentially crashing the server despite the 100/min limit.

### **Sliding Window Counter**
* **How it works:** A hybrid approach. It looks at the count in the current window and the previous window to estimate the current rate.
* **Formula:** $Count = \text{Requests in current window} + (\text{Requests in previous window} \times \text{Overlap percentage})$
* **Pros:** Smoother than fixed windows and memory-efficient.



---

## 4. Distributed Design with Redis
To make a rate limiter work across a cluster of servers, you need a centralized data store. **Redis** is the industry standard due to its speed and atomic operations.

### Handling Race Conditions
If two servers try to increment a user's count at the exact same time, you might encounter a race condition where the count is updated incorrectly.
* **The Fix:** Use **Lua Scripts**. Redis executes Lua scripts as a single atomic unit, ensuring that the "Read-Check-Update" cycle is never interrupted by another request.

### Basic Redis Logic (Fixed Window)
1.  Check if key `user_123:minute_59` exists.
2.  If yes, increment it. If it exceeds the limit, return **429**.
3.  If no, create the key with a value of 1 and set a **TTL** (Time To Live) of 60 seconds so it auto-deletes.

---

## 5. Communicating with the Client
When a request is throttled, the API should return a **429 Too Many Requests** status code. Best practice is to include headers to tell the client what happened:

| Header | Description |
| :--- | :--- |
| `X-Ratelimit-Limit` | The maximum number of requests allowed in the period. |
| `X-Ratelimit-Remaining` | How many requests the user has left. |
| `X-Ratelimit-Reset` | The Unix timestamp when the limit resets. |
| `Retry-After` | The number of seconds the client should wait before trying again. |

---

> **Quick Tip:** For high-scale systems, consider a "Shadow Mode" first. Log which users *would* have been rate-limited without actually blocking them. This helps you tune your limits before they impact real users.

## LLD 

Implementing a rate limiter in Spring Boot is best done using a **HandlerInterceptor** or a **Filter**. This allows you to intercept the request before it reaches your Controller.

For a production-grade LLD, we use **Redis** to store the bucket state. This ensures that if you have multiple instances of your service, they all share the same "count" for a user.

### 1. The Redis Lua Script
To prevent race conditions (where two requests read the same token count simultaneously), we use a Lua script. Redis executes this atomically.

```lua
-- keys[1] = bucket_key, args[1] = capacity, args[2] = refill_rate, args[3] = now
local bucket = redis.call('hmget', KEYS[1], 'tokens', 'last_updated')
local tokens = tonumber(bucket[1]) or tonumber(ARGV[1])
local last_updated = tonumber(bucket[2]) or tonumber(ARGV[3])

local delta = math.max(0, ARGV[3] - last_updated)
local refill = delta * tonumber(ARGV[2])
tokens = math.min(tonumber(ARGV[1]), tokens + refill)

if tokens >= 1 then
    redis.call('hmset', KEYS[1], 'tokens', tokens - 1, 'last_updated', ARGV[3])
    return 1
else
    return 0
end
```

---

### 2. Java Implementation (Spring Boot)

#### **The Rate Limiter Service**
This service interacts with Redis to execute the logic.

```java
@Service
public class RateLimiterService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private final DefaultRedisScript<Long> script;

    public RateLimiterService() {
        this.script = new DefaultRedisScript<>();
        this.script.setScriptSource(new ResourceScriptSource(new ClassPathResource("rate_limit.lua")));
        this.script.setResultType(Long.class);
    }

    public boolean isAllowed(String clientId, int capacity, double refillRate) {
        List<String> keys = Collections.singletonList("ratelimit:" + clientId);
        Long result = redisTemplate.execute(script, keys, 
                        String.valueOf(capacity), 
                        String.valueOf(refillRate), 
                        String.valueOf(Instant.now().getEpochSecond()));
        return result != null && result == 1;
    }
}
```

#### **The Interceptor**
This acts as the "Guard" for your APIs.

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = request.getHeader("X-Client-Id"); // Or use IP/Auth Token
        
        if (clientId == null || !rateLimiterService.isAllowed(clientId, 10, 0.5)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests - slow down!");
            return false;
        }
        return true;
    }
}
```

---

### 3. Low-Level Class Diagram
To visualize how these pieces fit together in the Spring ecosystem:



* **`RateLimitInterceptor`**: Intercepts `HttpServletRequest`.
* **`RateLimiterService`**: Strategy-agnostic logic handler.
* **`RedisTemplate`**: The data access layer for persistent, distributed state.

---

### 4. Why this LLD works
1.  **Distributed Consistency:** By using Redis, your rate limit isn't tied to a single server instance.
2.  **Atomicity:** The Lua script prevents "Check-then-Act" bugs.
3.  **Low Latency:** Redis is in-memory; the overhead is usually **< 2ms**.
4.  **Separation of Concerns:** Your business logic in the Controller never knows the rate limiter even exists.

