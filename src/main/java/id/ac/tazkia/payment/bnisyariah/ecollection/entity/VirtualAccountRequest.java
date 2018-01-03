package id.ac.tazkia.payment.bnisyariah.ecollection.entity;

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

@Entity @Builder @Getter @Setter @EqualsAndHashCode @NoArgsConstructor @AllArgsConstructor
public class VirtualAccountRequest {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull @NotEmpty
    private String accountNumber;

    @NotNull @NotEmpty
    private String invoiceNumber;

    @NotNull @NotEmpty
    private String name;

    private String description;

    @Email
    private String email;

    private String phone;

    @NotNull @Min(0)
    private BigDecimal amount;

    @NotNull
    private LocalDateTime requestTime = LocalDateTime.now();

    @NotNull @Column(columnDefinition = "DATE")
    private LocalDate expireDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus = RequestStatus.NEW;
}
