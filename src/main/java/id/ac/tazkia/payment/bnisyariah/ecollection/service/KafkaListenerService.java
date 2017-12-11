package id.ac.tazkia.payment.bnisyariah.ecollection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bnisyariah.ecollection.dao.VirtualAccountRequestDao;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class KafkaListenerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerService.class);

    @Autowired private ObjectMapper objectMapper;
    @Autowired private VirtualAccountRequestDao virtualAccountRequestDao;
    @KafkaListener(topics = "${kafka.topic.createva}", groupId = "${spring.kafka.consumer.group-id}")
    public void receiveVirtualAccountRequest(String message){
        try {
            LOGGER.debug("Receive message : {}", message);
            VirtualAccountRequest vaRequest = objectMapper.readValue(message, VirtualAccountRequest.class);
            virtualAccountRequestDao.save(vaRequest);
        } catch (Exception err){
            LOGGER.error(err.getMessage(), err);
        }
    }
}
