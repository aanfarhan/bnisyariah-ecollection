package id.ac.tazkia.payment.bnisyariah.ecollection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bnisyariah.ecollection.constants.BniEcollectionConstants;
import id.ac.tazkia.payment.bnisyariah.ecollection.dto.CreateVaRequest;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.AccountType;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service @Transactional
public class BniEcollectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BniEcollectionService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String TIMEZONE = "GMT+07:00";

    @Value("${bni.client-id}") private String clientId;

    @Autowired private RunningNumberService runningNumberService;
    @Autowired private ObjectMapper objectMapper;

    public void createVirtualAccount(VirtualAccountRequest request){
        String datePrefix = DATE_FORMAT.format(LocalDateTime.now(ZoneId.of(TIMEZONE)));
        String prefix = datePrefix;
        Long runningNumber = runningNumberService.getNumber(prefix);
        String trxId = datePrefix + String.format("%06d", runningNumber);

        CreateVaRequest createVaRequest = CreateVaRequest.builder()
                .clientId(clientId)
                .customerEmail(request.getEmail())
                .customerName(request.getName())
                .customerPhone(request.getPhone())
                .datetimeExpired(toIso8601(request.getExpireDate()))
                .description(request.getDescription())
                .trxAmount(request.getAmount().setScale(0, BigDecimal.ROUND_HALF_EVEN).toString())
                .trxId(trxId)
                .virtualAccount("8"+clientId + request.getNumber())
                .build();

        if(AccountType.CLOSED.equals(request.getAccountType())){
            createVaRequest.setBillingType(BniEcollectionConstants.BILLING_TYPE_CLOSED);
        } else if(AccountType.INSTALLMENT.equals(request.getAccountType())){
            createVaRequest.setBillingType(BniEcollectionConstants.BILLING_TYPE_INSTALLMENT);
        } else if(AccountType.OPEN.equals(request.getAccountType())){
            createVaRequest.setBillingType(BniEcollectionConstants.BILLING_TYPE_OPEN);
        }

        try {
            String requestJson = objectMapper.writeValueAsString(createVaRequest);
            LOGGER.debug("Create VA Request : {}", requestJson);
        } catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
    }

    private String toIso8601(LocalDate d) {
        return d.atStartOfDay(ZoneId.of(TIMEZONE))
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
