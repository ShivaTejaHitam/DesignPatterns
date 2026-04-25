Below is a **practical, production-style implementation** of an **Email Notification System using RabbitMQ and Spring Boot**, including architecture, code, configuration, and best practices.

---

# Architecture Overview

**Flow**

1. User triggers an action (e.g., registration, order placed).
2. Producer service sends message → RabbitMQ Queue.
3. Consumer service listens to queue.
4. Consumer processes message → sends email.
5. Retry / Dead Letter Queue handles failures.

**Components**

* Spring Boot Producer
* RabbitMQ Broker
* Spring Boot Consumer
* Email Service (SMTP)
* Dead Letter Queue (DLQ)

---

# Maven Dependencies

Add to both producer and consumer:

```xml
<dependencies>

    <!-- Spring Boot AMQP -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>

    <!-- Email -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- Lombok (optional) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

</dependencies>
```

---

# RabbitMQ Configuration

## application.yml

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

mail:
  host: smtp.gmail.com
  port: 587
  username: your-email@gmail.com
  password: app-password
  properties:
    mail:
      smtp:
        auth: true
        starttls:
          enable: true
```

---

# Queue Configuration

```java
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE =
            "email.exchange";

    public static final String QUEUE =
            "email.queue";

    public static final String ROUTING_KEY =
            "email.routingKey";

    public static final String DLQ =
            "email.dlq";

    @Bean
    public Queue queue() {
        return QueueBuilder
                .durable(QUEUE)
                .withArgument(
                    "x-dead-letter-exchange",
                    EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(
            Queue queue,
            DirectExchange exchange) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY);
    }

    /* Dead Letter Queue */

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ);
    }

}
```

---

# Email DTO (Message Object)

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    private String to;

    private String subject;

    private String body;

}
```

RabbitMQ automatically serializes this using JSON.

---

# Producer Service

Sends email messages to RabbitMQ.

```java
@Service
@RequiredArgsConstructor
public class EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmail(
            EmailMessage message) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                message);

    }
}
```

---

# Producer Controller

Example REST API:

```java
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailProducer producer;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @RequestBody EmailMessage message) {

        producer.sendEmail(message);

        return ResponseEntity
                .ok("Email request queued");

    }
}
```

---

# Consumer (Email Listener)

This processes queue messages.

```java
@Service
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;

    @RabbitListener(
        queues = RabbitMQConfig.QUEUE)
    public void receiveEmail(
            EmailMessage message) {

        sendMail(message);

    }

    private void sendMail(
            EmailMessage msg) {

        SimpleMailMessage mail =
                new SimpleMailMessage();

        mail.setTo(msg.getTo());
        mail.setSubject(msg.getSubject());
        mail.setText(msg.getBody());

        mailSender.send(mail);

    }

}
```

---

# JSON Message Converter (Recommended)

```java
@Configuration
public class MessageConfig {

    @Bean
    public MessageConverter converter() {

        return new Jackson2JsonMessageConverter();

    }

}
```

---

# Retry Configuration

```java
@Bean
public SimpleRabbitListenerContainerFactory
listenerContainerFactory(
        ConnectionFactory connectionFactory) {

    SimpleRabbitListenerContainerFactory factory =
            new SimpleRabbitListenerContainerFactory();

    factory.setConnectionFactory(
            connectionFactory);

    factory.setAdviceChain(
            RetryInterceptorBuilder
                .stateless()
                .maxAttempts(3)
                .backOffOptions(
                        1000,
                        2.0,
                        10000)
                .build());

    return factory;
}
```

---

# Dead Letter Queue Handling

```java
@RabbitListener(
        queues = RabbitMQConfig.DLQ)
public void handleDeadLetter(
        EmailMessage message) {

    log.error(
        "Failed email moved to DLQ: {}",
        message);

}
```

---

# Testing Flow

## Step 1 — Start RabbitMQ

Docker:

```bash
docker run -d \
-p 5672:5672 \
-p 15672:15672 \
rabbitmq:3-management
```

RabbitMQ UI:

```
http://localhost:15672
guest / guest
```

---

## Step 2 — Send API Request

POST:

```json
POST /email/send

{
  "to": "test@gmail.com",
  "subject": "Welcome",
  "body": "Welcome to RabbitMQ Email Service"
}
```

---

# Production Improvements (Important)

These matter in real systems.

---

## 1. Use Email Templates

Use:

* Thymeleaf
* FreeMarker

Example:

```java
Context context = new Context();

context.setVariable(
        "name",
        "John");

String body =
        templateEngine.process(
                "welcome-email",
                context);
```

---

## 2. Use Message Persistence

```java
rabbitTemplate.setDeliveryMode(
        MessageDeliveryMode.PERSISTENT);
```

Prevents message loss.

---

## 3. Add Message TTL

```java
QueueBuilder
    .durable(QUEUE)
    .ttl(60000)
    .build();
```

Expires messages.

---

## 4. Use Circuit Breaker

Add:

```xml
resilience4j-spring-boot2
```

Prevents SMTP overload.

---

## 5. Add Monitoring

Use:

* Spring Boot Actuator
* Prometheus
* Grafana
* RabbitMQ Metrics

---

# Folder Structure

```text
email-service/

 ├── config/
 │     RabbitMQConfig.java
 │     MessageConfig.java
 │
 ├── controller/
 │     EmailController.java
 │
 ├── dto/
 │     EmailMessage.java
 │
 ├── producer/
 │     EmailProducer.java
 │
 ├── consumer/
 │     EmailConsumer.java
 │
 ├── service/
 │     EmailService.java
 │
 └── EmailApplication.java
```

---

# Real-World Use Cases

This exact pattern is used for:

* User registration emails
* OTP notifications
* Order confirmations
* Payment receipts
* Password reset emails
* Bulk marketing campaigns

---

# Scalability Notes

This design supports:

* Horizontal scaling (multiple consumers)
* Message retries
* Fault tolerance
* Load buffering
* Async processing

Add multiple consumers:

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        concurrency: 5
        max-concurrency: 10
```

---

# Common Interview Questions

Likely asked when discussing this system:

1. Why use RabbitMQ for email notifications?
2. How do you handle failures?
3. What is a Dead Letter Queue?
4. How do retries work?
5. How do you guarantee message delivery?
6. How do you scale consumers?

---

# If You Want to Take This Further

I can also help you build:

* Full **microservice-based notification system**
* Multi-channel notifications:

  * Email
  * SMS
  * Push notifications
* Kafka-based version
* Production-ready GitHub project structure
* Kubernetes deployment YAML

Just specify.
