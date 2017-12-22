package id.ac.tazkia.payment.bnisyariah.ecollection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bnisyariah.ecollection.dto.VaPayment;
import id.ac.tazkia.payment.bnisyariah.ecollection.dto.VaResponse;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.Payment;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class KafkaSenderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSenderService.class);

    @Value("${bni.client-id}") private String clientId;
    @Value("${kafka.topic.bni.va.response}") private String kafkaTopicResponse;
    @Value("${kafka.topic.bni.va.payment}") private String kafkaTopicPayment;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    @Async
    public void sendVaResponse(VirtualAccountRequest va){
        try {
            VaResponse vaResponse = new VaResponse();
            BeanUtils.copyProperties(va, vaResponse);
            vaResponse.setNumber("8"+clientId+vaResponse.getNumber());
            String jsonResponse = objectMapper.writeValueAsString(vaResponse);
            LOGGER.debug("VA Response : {}", jsonResponse);
            kafkaTemplate.send(kafkaTopicResponse, jsonResponse);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Async
    public void sendPaymentNotification(Payment payment) {
        try {
            VaPayment vaPayment = new VaPayment();
            vaPayment.setAccountNumber("8"+clientId+payment.getVirtualAccount().getNumber());
            vaPayment.setAmount(payment.getAmount());
            vaPayment.setCumulativeAmount(payment.getCumulativeAmount());
            vaPayment.setPaymentTime(payment.getTransactionTime());
            vaPayment.setReference(payment.getPaymentReference());
            String jsonData = objectMapper.writeValueAsString(vaPayment);
            LOGGER.debug("VA Payment : {}", jsonData);
            kafkaTemplate.send(kafkaTopicPayment, jsonData);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
