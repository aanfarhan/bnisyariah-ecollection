package id.ac.tazkia.payment.bnisyariah.ecollection.dao;

import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RunningNumber;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RunningNumberDao extends PagingAndSortingRepository<RunningNumber, String> {
    RunningNumber findByPrefix(String prefix);
}
