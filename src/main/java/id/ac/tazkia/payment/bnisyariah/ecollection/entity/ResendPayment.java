package id.ac.tazkia.payment.bnisyariah.ecollection.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity @Data
public class ResendPayment {
    @Id
    private String paymentReference;
}
