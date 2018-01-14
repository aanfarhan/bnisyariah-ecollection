package id.ac.tazkia.payment.bnisyariah.ecollection.entity;

import id.ac.tazkia.payment.bnisyariah.ecollection.dto.RequestStatus;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class VirtualAccountRequest {
    private String bankId;
    private String accountNumber;
    private String invoiceNumber;
    private String name;
    private String description;
    private String email;
    private String phone;
    private BigDecimal amount;
    private LocalDateTime requestTime = LocalDateTime.now();
    private LocalDate expireDate;
    private AccountType accountType;
    private RequestType requestType;
    private RequestStatus requestStatus;
}
