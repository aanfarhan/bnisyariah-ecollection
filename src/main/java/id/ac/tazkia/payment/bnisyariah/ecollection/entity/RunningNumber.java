package id.ac.tazkia.payment.bnisyariah.ecollection.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data @Entity
public class RunningNumber {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull @NotEmpty
    private String prefix;

    @NotNull
    @Min(0)
    private Long lastNumber = 0L;

    public RunningNumber() {
    }
}