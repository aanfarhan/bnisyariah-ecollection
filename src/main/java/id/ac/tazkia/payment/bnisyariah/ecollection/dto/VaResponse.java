package id.ac.tazkia.payment.bnisyariah.ecollection.dto;

import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RequestStatus;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RequestType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VaResponse {
    private RequestType requestType;
    private RequestStatus requestStatus;
    private String number;
    private String name;
    private BigDecimal amount;
}
