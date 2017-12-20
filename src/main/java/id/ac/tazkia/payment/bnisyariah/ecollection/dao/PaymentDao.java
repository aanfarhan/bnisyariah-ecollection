package id.ac.tazkia.payment.bnisyariah.ecollection.dao;

import id.ac.tazkia.payment.bnisyariah.ecollection.entity.Payment;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PaymentDao extends PagingAndSortingRepository<Payment, String> {
}
