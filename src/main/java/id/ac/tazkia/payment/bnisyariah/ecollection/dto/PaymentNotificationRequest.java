package id.ac.tazkia.payment.bnisyariah.ecollection.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter @Setter
public class PaymentNotificationRequest {
    private String virtualAccount;
    private String customerName;
    private String trxId;
    private String trxAmount;
    private String paymentAmount;
    private String cumulativePaymentAmount;
    private String paymentNtb;
    private String datetimePayment;
    private String datetimePaymentIso8601;
}
