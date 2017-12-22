package id.ac.tazkia.payment.bnisyariah.ecollection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bnisyariah.ecollection.dao.VirtualAccountRequestDao;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RequestStatus;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RequestType;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service @Transactional
public class KafkaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Autowired private ObjectMapper objectMapper;
    @Autowired private VirtualAccountRequestDao virtualAccountRequestDao;
    @Autowired private BniEcollectionService bniEcollectionService;

    @KafkaListener(topics = "${kafka.topic.bni.va.request}", groupId = "${spring.kafka.consumer.group-id}")
    public void receiveVirtualAccountRequest(String message){
        try {
            LOGGER.debug("Receive message : {}", message);
            VirtualAccountRequest vaRequest = objectMapper.readValue(message, VirtualAccountRequest.class);
            vaRequest.setRequestTime(LocalDateTime.now());
            vaRequest.setRequestStatus(RequestStatus.NEW);
            virtualAccountRequestDao.save(vaRequest);
            if(RequestType.CREATE.equals(vaRequest.getRequestType())) {
                bniEcollectionService.createVirtualAccount(vaRequest);
            } else if(RequestType.UPDATE.equals(vaRequest.getRequestType())){
                bniEcollectionService.updateVirtualAccount(vaRequest);
            } else {
                LOGGER.warn("Virtual Account Request Type {} belum dibuat", vaRequest.getRequestType());
            }
        } catch (Exception err){
            LOGGER.error(err.getMessage(), err);
        }
    }
}
