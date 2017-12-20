package id.ac.tazkia.payment.bnisyariah.ecollection.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Entity
public class Payment {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_virtual_account")
    private VirtualAccount virtualAccount;

    @NotNull @Min(0)
    private BigDecimal amount;

    @NotNull @Min(0)
    private BigDecimal cumulativeAmount;

    @NotNull
    private LocalDateTime transactionTime;

    @NotNull @NotEmpty
    private String paymentReference;
}
