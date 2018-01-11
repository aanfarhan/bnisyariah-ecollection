package id.ac.tazkia.payment.bnisyariah.ecollection.dao;

import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RunningNumber;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.LockModeType;

public interface RunningNumberDao extends PagingAndSortingRepository<RunningNumber, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    RunningNumber findByPrefix(String prefix);
}
