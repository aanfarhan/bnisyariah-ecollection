package id.ac.tazkia.payment.bnisyariah.ecollection.dao;

import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccountRequest;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface VirtualAccountRequestDao extends PagingAndSortingRepository<VirtualAccountRequest, String> {
}
