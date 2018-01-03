package id.ac.tazkia.payment.bnisyariah.ecollection.service;

import id.ac.tazkia.payment.bnisyariah.ecollection.entity.AccountType;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RequestStatus;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.RequestType;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.VirtualAccountRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BniEcollectionServiceTests {

    @Autowired private BniEcollectionService bniEcollectionService;

    @Test
    public void testCreateVa(){
        VirtualAccountRequest vaRequest = VirtualAccountRequest
                .builder()
                .requestTime(LocalDateTime.now())
                .accountNumber(String.valueOf(new Random().nextLong()& Long.MAX_VALUE).substring(0,12))
                .invoiceNumber(String.valueOf(new Random().nextLong()& Long.MAX_VALUE).substring(0,12))
                .requestStatus(RequestStatus.NEW)
                .requestType(RequestType.CREATE)
                .accountType(AccountType.CLOSED)
                .amount(BigDecimal.valueOf(100000.00))
                .description("Tagihan Test")
                .email("endy@tazkia.ac.id")
                .expireDate(LocalDate.now().plusMonths(1))
                .name("Endy Muhardin")
                .phone("081234567890")
                .build();

        bniEcollectionService.createVirtualAccount(vaRequest);
    }
}