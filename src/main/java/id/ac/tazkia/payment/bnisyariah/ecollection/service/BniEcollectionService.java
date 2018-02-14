package id.ac.tazkia.payment.bnisyariah.ecollection.service;

import com.bni.encrypt.BNIHash;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bnisyariah.ecollection.constants.BniEcollectionConstants;
import id.ac.tazkia.payment.bnisyariah.ecollection.dao.VirtualAccountDao;
import id.ac.tazkia.payment.bnisyariah.ecollection.dto.CreateVaRequest;
import id.ac.tazkia.payment.bnisyariah.ecollection.dto.RequestStatus;
import id.ac.tazkia.payment.bnisyariah.ecollection.dto.UpdateVaRequest;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service @Transactional
public class BniEcollectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BniEcollectionService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String TIMEZONE = "GMT+07:00";

    @Value("${bni.client-prefix}") private String clientPrefix;
    @Value("${bni.client-id}") private String clientId;
    @Value("${bni.client-key}") private String clientKey;
    @Value("${bni.server-url}") private String serverUrl;
    @Value("${bni.send-email}") private Boolean sendEmail;

    @Autowired private KafkaSenderService kafkaSenderService;
    @Autowired private RunningNumberService runningNumberService;
    @Autowired private VirtualAccountDao virtualAccountDao;
    @Autowired private ObjectMapper objectMapper;

    @Async
    public void createVirtualAccount(VirtualAccountRequest request){
        VirtualAccount vaInvoice = virtualAccountDao.findByInvoiceNumber(request.getInvoiceNumber());
        if (vaInvoice != null) {
            LOGGER.warn("VA dengan nomor invoice {} sudah ada", request.getInvoiceNumber());
            request.setAccountNumber(vaInvoice.getAccountNumber());
            request.setRequestStatus(RequestStatus.SUCCESS);
            kafkaSenderService.sendVaResponse(request);
            return;
        }

        List<VirtualAccount> existing = virtualAccountDao
                .findByAccountNumberAndAccountStatus(request.getAccountNumber(), AccountStatus.ACTIVE);
        if(!existing.isEmpty()) {
            LOGGER.info("VA dengan nomor {} sudah ada", request.getAccountNumber());
            request.setRequestStatus(RequestStatus.SUCCESS);
            kafkaSenderService.sendVaResponse(request);
            return;
        }

        VirtualAccount va = new VirtualAccount();
        BeanUtils.copyProperties(request, va);
        va.setId(null);

        if (create(va)) {
            request.setRequestStatus(RequestStatus.SUCCESS);
        } else {
            request.setRequestStatus(RequestStatus.ERROR);
        }

        kafkaSenderService.sendVaResponse(request);
    }

    @Async
    public void deleteVirtualAccount(VirtualAccountRequest request) {
        List<VirtualAccount> existing = virtualAccountDao
                .findByAccountNumberAndAccountStatus(request.getAccountNumber(), AccountStatus.ACTIVE);
        if(existing.isEmpty()) {
            LOGGER.warn("VA dengan nomor {} belum ada", request.getAccountNumber());
            return;
        }

        if(existing.size() > 1){
            LOGGER.warn("VA dengan nomor {} ada {} buah. Delete tidak dapat diproses",
                    request.getAccountNumber(), existing.size());
            return;
        }

        VirtualAccount va = existing.iterator().next();

        if (delete(va)) {
            request.setRequestStatus(RequestStatus.SUCCESS);
        } else {
            request.setRequestStatus(RequestStatus.ERROR);
        }

        kafkaSenderService.sendVaResponse(request);
    }

    @Async
    public void updateVirtualAccount(VirtualAccountRequest request){
        List<VirtualAccount> existing = virtualAccountDao
                .findByAccountNumberAndAccountStatus(request.getAccountNumber(), AccountStatus.ACTIVE);
        if(existing.isEmpty()) {
            sendRequestError("VA dengan nomor {} tidak terdaftar", request);
            return;
        }

        if(existing.size() > 1){
            sendRequestError("VA dengan nomor {} duplikat", request);
            return;
        }

        VirtualAccount va = existing.iterator().next();

        if (!delete(va)) {
            sendRequestError("VA dengan nomor {} gagal diupdate", request);
            return;
        }

        // kalau tanggal expire yang baru sudah lewat, tidak perlu bikin VA baru
        if (va.getExpireDate().isBefore(LocalDate.now())) {
            request.setRequestStatus(RequestStatus.SUCCESS);
            kafkaSenderService.sendVaResponse(request);
            return;
        }

        va.setExpireDate(request.getExpireDate());
        va.setAmount(request.getAmount());
        va.setDescription(request.getDescription());
        va.setEmail(request.getEmail());
        va.setName(request.getName());
        va.setPhone(request.getPhone());

        if (!create(va)) {
            sendRequestError("VA dengan nomor {} gagal diupdate", request);
            request.setRequestStatus(RequestStatus.ERROR);
            kafkaSenderService.sendVaResponse(request);
            return;
        }
        request.setRequestStatus(RequestStatus.SUCCESS);
        kafkaSenderService.sendVaResponse(request);
    }

    private void sendRequestError(String message, VirtualAccountRequest request) {
        LOGGER.warn(message, request.getAccountNumber());
        request.setRequestStatus(RequestStatus.ERROR);
        kafkaSenderService.sendVaResponse(request);
    }

    private boolean create(VirtualAccount virtualAccount) {
        String datePrefix = DATE_FORMAT.format(LocalDateTime.now(ZoneId.of(TIMEZONE)));
        Long runningNumber = runningNumberService.getNumber(datePrefix);
        String trxId = datePrefix + String.format("%06d", runningNumber);
        virtualAccount.setTransactionId(trxId);

        CreateVaRequest createVaRequest = CreateVaRequest.builder()
                .clientId(clientId)
                .customerEmail(virtualAccount.getEmail())
                .customerName(virtualAccount.getName())
                .customerPhone(virtualAccount.getPhone())
                .datetimeExpired(toIso8601(virtualAccount.getExpireDate()))
                .description(virtualAccount.getDescription())
                .trxAmount(virtualAccount.getAmount().setScale(0, RoundingMode.DOWN).toString())
                .trxId(trxId)
                .virtualAccount(clientPrefix+clientId + virtualAccount.getAccountNumber())
                .build();

        if(AccountType.CLOSED.equals(virtualAccount.getAccountType())){
            createVaRequest.setBillingType(BniEcollectionConstants.BILLING_TYPE_CLOSED);
        } else if(AccountType.INSTALLMENT.equals(virtualAccount.getAccountType())){
            createVaRequest.setBillingType(BniEcollectionConstants.BILLING_TYPE_INSTALLMENT);
        } else if(AccountType.OPEN.equals(virtualAccount.getAccountType())){
            createVaRequest.setBillingType(BniEcollectionConstants.BILLING_TYPE_OPEN);
        }

        if (!sendEmail) {
            createVaRequest.setCustomerEmail(null);
        }

        try {
            String requestJson = objectMapper.writeValueAsString(createVaRequest);
            LOGGER.debug("Create VA Request : {}", requestJson);
            Map<String, String> hasil = executeRequest(createVaRequest);
            LOGGER.debug("Create VA Response : {}", objectMapper.writeValueAsString(hasil));
            if(hasil != null) {
                virtualAccount.setAccountStatus(AccountStatus.ACTIVE);
                virtualAccount.setCreateTime(LocalDateTime.now());
                virtualAccountDao.save(virtualAccount);
                LOGGER.info("BNI : Create VA [{}-{}] sukses", virtualAccount.getAccountNumber(), virtualAccount.getName());
                return true;
            } else {
                LOGGER.warn("BNI : Create VA [{}-{}] error", virtualAccount.getAccountNumber(), virtualAccount.getName());
            }
        } catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
        return false;
    }

    private boolean delete(VirtualAccount virtualAccount) {
        UpdateVaRequest updateVaRequest = UpdateVaRequest.builder()
                .clientId(clientId)
                .customerEmail(virtualAccount.getEmail())
                .customerName(virtualAccount.getName())
                .customerPhone(virtualAccount.getPhone())
                .datetimeExpired(toIso8601(LocalDate.now().minusDays(5)))
                .description(virtualAccount.getDescription() + " dihapus")
                .trxAmount(virtualAccount.getAmount().setScale(0, RoundingMode.DOWN).toString())
                .trxId(virtualAccount.getTransactionId())
                .build();

        try {
            String requestJson = objectMapper.writeValueAsString(updateVaRequest);
            LOGGER.debug("Delete VA Request : {}", requestJson);
            Map<String, String> hasil = executeRequest(updateVaRequest);
            LOGGER.debug("Delete VA Response : {}", objectMapper.writeValueAsString(hasil));
            if(hasil != null) {
                virtualAccount.setAccountStatus(AccountStatus.INACTIVE);
                virtualAccountDao.save(virtualAccount);
                LOGGER.info("BNI : Delete VA [{}-{}] sukses", virtualAccount.getAccountNumber(), virtualAccount.getName());
                return true;
            } else {
                LOGGER.error("BNI : Delete VA [{}-{}] error", virtualAccount.getAccountNumber(), virtualAccount.getName());
            }
        } catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
        return false;
    }

    private String toIso8601(LocalDate d) {
        return d.atStartOfDay(ZoneId.of(TIMEZONE))
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private Map<String, String> executeRequest(Object request) throws Exception {
        String responseData = sendRequest(request);

        if(responseData != null) {
            LOGGER.debug("Response Data : {}", responseData);

            String decryptedResponse = BNIHash.parseData(responseData, clientId, clientKey);
            LOGGER.debug("Decrypted Response : {}", decryptedResponse);

            return objectMapper.readValue(decryptedResponse, Map.class);
        }
        return null;
    }

    private String sendRequest(Object requestData) throws Exception {
        String rawData = objectMapper.writeValueAsString(requestData);
        LOGGER.debug("BNI : Raw Data : {}",rawData);

        String encryptedData = BNIHash.hashData(rawData, clientId, clientKey);
        LOGGER.debug("BNI : Encrypted Data : {}",encryptedData);

        Map<String, String> wireData = new TreeMap<>();
        wireData.put("client_id", clientId);
        wireData.put("data", encryptedData);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<Map<String, String>>(wireData, headers);
        ResponseEntity<Map> response = restTemplate.exchange(serverUrl, HttpMethod.POST, request, Map.class);
        Map<String, String> hasil = response.getBody();
        String responseStatus = hasil.get("status");
        LOGGER.debug("BNI : Response Status : {}",responseStatus);

        if(!BniEcollectionConstants.BNI_RESPONSE_STATUS_SUCCESS.equals(responseStatus)) {
            LOGGER.error("BNI : Response status : {}", responseStatus);
            return null;
        }

        return hasil.get("data");
    }
}
