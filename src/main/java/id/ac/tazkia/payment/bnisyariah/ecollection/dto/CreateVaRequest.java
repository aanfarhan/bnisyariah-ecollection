package id.ac.tazkia.payment.bnisyariah.ecollection.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import id.ac.tazkia.payment.bnisyariah.ecollection.constants.BniEcollectionConstants;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreateVaRequest {
    private final String type = BniEcollectionConstants.CREATE_BILLING;
    private String clientId;
    private String trxId;
    private String trxAmount;
    private String billingType;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String virtualAccount;
    private String datetimeExpired;
    private String description;
}
