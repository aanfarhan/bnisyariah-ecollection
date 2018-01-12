package id.ac.tazkia.payment.bnisyariah.ecollection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.AccountType;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RequestStatus;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RequestType;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccountRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:/sql/delete-varequests.sql")
public class KafkaListenerServiceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerServiceTests.class);

    @Value("${kafka.topic.va.request}") private String topic;
    @Value("${bni.client-id}") private String clientId;
    @Value("${bni.client-prefix}") private String clientPrefix;

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    @Test
    public void testSendVaRequests() throws Exception {
        VirtualAccountRequest vaRequest = VirtualAccountRequest
                .builder()
                .requestTime(LocalDateTime.now())
                .accountNumber(clientPrefix+clientId+"08123456789012")
                .invoiceNumber("01234567890")
                .requestStatus(RequestStatus.NEW)
                .requestType(RequestType.CREATE)
                .accountType(AccountType.CLOSED)
                .amount(BigDecimal.valueOf(100000.00))
                .description("Tagihan Test")
                .email("endy@tazkia.ac.id")
                .expireDate(LocalDate.now().plusMonths(1))
                .name("Endy Muhardin")
                .phone("081234567890")
                .build();

        String vaJson = objectMapper.writeValueAsString(vaRequest);
        LOGGER.debug("VA Request JSON : {}", vaJson);

        //kafkaTemplate.send(topic, vaJson);
        LOGGER.debug("Va Request Sent");
    }
}
