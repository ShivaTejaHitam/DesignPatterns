Below is a **practical, production-style implementation** of an **Email Notification System using Apache Kafka with Spring Boot**.
This follows an **event-driven architecture** where one service produces email events and another consumes them to send emails.

---

# Architecture Overview

**Flow:**

1. A service (e.g., Order Service, User Service) publishes an **EmailEvent** to Kafka.
2. Kafka stores the message in a **topic**.
3. Email Service consumes the event.
4. Email Service sends email using **SMTP**.

```
[Producer Service]
        |
        v
   Kafka Topic
        |
        v
[Email Notification Service]
        |
        v
     SMTP Server
```

Technologies:

* **Spring Boot**
* **Apache Kafka**
* **Spring Kafka**
* **Java Mail Sender**
* **JSON Serialization**

---

# Step 1 — Add Dependencies

### Maven (`pom.xml`)

```xml
<dependencies>

    <!-- Spring Boot Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- Email Sender -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>

</dependencies>
```

---

# Step 2 — Define Email Event Model

This is the **Kafka message payload**.

```java
public class EmailEvent {

    private String to;
    private String subject;
    private String body;

    public EmailEvent() {}

    public EmailEvent(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
```

---

# Step 3 — Kafka Producer Configuration

```java
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, EmailEvent> producerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092"
        );

        config.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class
        );

        config.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            JsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, EmailEvent> kafkaTemplate() {

        return new KafkaTemplate<>(producerFactory());
    }
}
```

---

# Step 4 — Kafka Producer Service

This service **publishes email events**.

```java
@Service
public class EmailProducerService {

    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;

    private static final String TOPIC = "email-topic";

    public EmailProducerService(
            KafkaTemplate<String, EmailEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEmailEvent(EmailEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getTo(),
                event
        );
    }
}
```

---

# Step 5 — Kafka Consumer Configuration

```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, EmailEvent> consumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092"
        );

        config.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            "email-group"
        );

        config.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class
        );

        config.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            JsonDeserializer.class
        );

        config.put(
            JsonDeserializer.TRUSTED_PACKAGES,
            "*"
        );

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(EmailEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String,
            EmailEvent> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<
                String,
                EmailEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        return factory;
    }
}
```

---

# Step 6 — Email Sender Service

Uses Spring Mail.

```java
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(EmailEvent event) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(event.getTo());
        message.setSubject(event.getSubject());
        message.setText(event.getBody());

        mailSender.send(message);
    }
}
```

---

# Step 7 — Kafka Consumer Listener

Consumes Kafka message and triggers email.

```java
@Service
public class EmailConsumerService {

    private final EmailService emailService;

    public EmailConsumerService(
            EmailService emailService) {

        this.emailService = emailService;
    }

    @KafkaListener(
        topics = "email-topic",
        groupId = "email-group"
    )
    public void consume(EmailEvent event) {

        emailService.sendEmail(event);

        System.out.println(
            "Email sent to: " + event.getTo()
        );
    }
}
```

---

# Step 8 — Email SMTP Configuration

### `application.yml`

Example using Gmail SMTP:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

  kafka:
    bootstrap-servers: localhost:9092
```

---

# Step 9 — Kafka Topic Creation

Create topic:

```bash
kafka-topics.sh \
--create \
--topic email-topic \
--bootstrap-server localhost:9092 \
--partitions 3 \
--replication-factor 1
```

---

# Step 10 — Test via REST API

Expose an API to trigger email.

```java
@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailProducerService producer;

    public EmailController(
            EmailProducerService producer) {

        this.producer = producer;
    }

    @PostMapping("/send")
    public String sendEmail(
            @RequestBody EmailEvent event) {

        producer.sendEmailEvent(event);

        return "Email Event Published";
    }
}
```

---

# Production Enhancements (Important)

Real systems need more than the basic version.

---

## 1 — Retry Mechanism

Use Kafka retry:

```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 2000)
)
@KafkaListener(topics = "email-topic")
public void consume(EmailEvent event) {

    emailService.sendEmail(event);
}
```

---

## 2 — Dead Letter Topic (DLT)

Handle failed emails.

```java
@DltHandler
public void dltHandler(
        EmailEvent event) {

    log.error("Failed email: {}", event);
}
```

---

## 3 — Email Templates

Use:

* Thymeleaf
* FreeMarker

Example:

```java
String html = templateEngine.process(
        "welcome-template",
        context
);
```

---

## 4 — Idempotency

Prevent duplicate email sending.

Use:

* messageId
* Redis cache
* DB record

---

## 5 — Observability

Add:

* Logging
* Metrics
* Tracing

Tools:

* Prometheus
* Grafana
* Zipkin

---

# Real-World Use Cases

This pattern is widely used in:

* Order confirmation emails
* Password reset emails
* OTP delivery
* Invoice notifications
* System alerts

Typical microservices that produce events:

* User Service
* Order Service
* Payment Service

---

# Folder Structure (Recommended)

```
email-notification-service
│
├── config
│   ├── KafkaProducerConfig
│   ├── KafkaConsumerConfig
│
├── controller
│   └── EmailController
│
├── model
│   └── EmailEvent
│
├── producer
│   └── EmailProducerService
│
├── consumer
│   └── EmailConsumerService
│
├── service
│   └── EmailService
│
└── EmailApplication
```

---

# If You're Preparing for Interviews

Expect these follow-up topics:

* Kafka partitions
* Consumer groups
* Message ordering
* Exactly-once semantics
* Retry & Dead Letter Queue
* Idempotent consumers
* Schema evolution (Avro)

---

# If You're Building a Real System

Tell me:

1. **Single service or microservices architecture?**
2. **Kafka running locally or cloud (e.g., Confluent)?**
3. **Email provider (Gmail, SES, SendGrid)?**
4. **Expected email volume per day?**

With that, I can generate:

* Full production-ready implementation
* Docker setup
* Multi-service architecture
* Load-tested configuration
