package ca.portfolio.payments.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private final KafkaTemplate<String, Object> kafka;
    public PaymentEventPublisher(KafkaTemplate<String, Object> kafka) { this.kafka = kafka; }
    public void publish(PaymentEvent event) {
        try {
            kafka.send("payment-events", event.transferId().toString(), event)
                    .whenComplete((result, error) -> {
                        if (error != null) log.warn("Payment event publish failed for {}", event.transferId(), error);
                    });
        } catch (Exception ex) {
            log.warn("Kafka unavailable; event was not published for {}", event.transferId(), ex);
        }
    }
}
