package id.ac.tazkia.payment.bnisyariah.ecollection.dao;

import id.ac.tazkia.payment.bnisyariah.ecollection.entity.Payment;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccount;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface PaymentDao extends PagingAndSortingRepository<Payment, String> {
    List<Payment> findByVirtualAccount(VirtualAccount virtualAccount);
    Iterable<Payment> findByPaymentReference(String paymentReference);
}
