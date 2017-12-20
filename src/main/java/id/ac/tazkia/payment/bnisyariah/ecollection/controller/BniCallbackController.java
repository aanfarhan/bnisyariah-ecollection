package id.ac.tazkia.payment.bnisyariah.ecollection.controller;

import com.bni.encrypt.BNIHash;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bnisyariah.ecollection.constants.BniEcollectionConstants;
import id.ac.tazkia.payment.bnisyariah.ecollection.dao.PaymentDao;
import id.ac.tazkia.payment.bnisyariah.ecollection.dao.VirtualAccountDao;
import id.ac.tazkia.payment.bnisyariah.ecollection.dto.PaymentNotificationRequest;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.AccountStatus;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.Payment;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Transactional
@RestController
@RequestMapping("/api/callback/bni")
public class BniCallbackController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BniCallbackController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${bni.client-id}") private String clientId;
    @Value("${bni.client-key}") private String clientKey;

    @Autowired private VirtualAccountDao virtualAccountDao;
    @Autowired private PaymentDao paymentDao;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping("/payment")
    public Map<String, String> paymentNotification(@RequestBody Map<String, String> requestData){

        Map<String, String> response = new HashMap<>();
        response.put("status", BniEcollectionConstants.BNI_RESPONSE_STATUS_SUCCESS);

        String encryptedData = requestData.get("data");
        if(encryptedData == null || encryptedData.length() < 1){
            LOGGER.error("BNI : Invalid payment notification");
            response.put("status", "999");
            return response;
        }

        LOGGER.debug("BNI : Callback : Encrypted Data : {}", encryptedData);
        String data = BNIHash.parseData(encryptedData, clientId, clientKey);
        LOGGER.debug("BNI : Callback : Decrypted Data : {}", data);

        try {
            PaymentNotificationRequest paymentNotificationRequest = objectMapper.readValue(data, PaymentNotificationRequest.class);
            VirtualAccount va = virtualAccountDao.findByTransactionId(paymentNotificationRequest.getTrxId());
            if (va == null) {
                LOGGER.error("BNI : Virtual Account dengan trx_id {}, va {}, atas nama {}, dengan nominal {} tidak ada di database",
                        paymentNotificationRequest.getTrxId(),
                        paymentNotificationRequest.getVirtualAccount(),
                        paymentNotificationRequest.getCustomerName(),
                        paymentNotificationRequest.getTrxAmount());
                return response;
            }

            Payment p = new Payment();
            p.setVirtualAccount(va);
            p.setAmount(new BigDecimal(paymentNotificationRequest.getPaymentAmount()));
            p.setCumulativeAmount(new BigDecimal(paymentNotificationRequest.getCumulativePaymentAmount()));
            p.setPaymentReference(paymentNotificationRequest.getPaymentNtb());
            try {
                p.setTransactionTime(LocalDateTime.parse(paymentNotificationRequest.getDatetimePayment(), DATE_TIME_FORMATTER));
            } catch (DateTimeParseException err){
                LOGGER.error("BNI : Format tanggal {} tidak sesuai yyyy-MM-dd HH:mm:ss", paymentNotificationRequest.getDatetimePayment());
                p.setTransactionTime(LocalDateTime.now());
            }

            paymentDao.save(p);
            if(p.getCumulativeAmount().compareTo(va.getAmount()) == 0){
                va.setAccountStatus(AccountStatus.INACTIVE);
                virtualAccountDao.save(va);
            }
            return response;
        } catch (IOException e) {
            LOGGER.error("BNI : Invalid payment notification payload : {}", data);
            response.put("status", "999");
            return response;
        }
    }
}
