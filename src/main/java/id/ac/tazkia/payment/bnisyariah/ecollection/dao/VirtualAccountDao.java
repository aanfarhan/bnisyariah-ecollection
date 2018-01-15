package id.ac.tazkia.payment.bnisyariah.ecollection.dao;

import id.ac.tazkia.payment.bnisyariah.ecollection.entity.AccountStatus;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccount;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface VirtualAccountDao extends PagingAndSortingRepository<VirtualAccount, String> {
    VirtualAccount findByTransactionId(String transactionId);
    VirtualAccount findByInvoiceNumber(String invoiceNumber);
    List<VirtualAccount> findByAccountNumberAndAccountStatus(String number, AccountStatus status);
}
