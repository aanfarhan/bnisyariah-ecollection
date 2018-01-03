package id.ac.tazkia.payment.bnisyariah.ecollection.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VaPayment {
    private String accountNumber;
    private String reference;
    private BigDecimal amount;
    private BigDecimal cumulativeAmount;
    private LocalDateTime paymentTime;
}