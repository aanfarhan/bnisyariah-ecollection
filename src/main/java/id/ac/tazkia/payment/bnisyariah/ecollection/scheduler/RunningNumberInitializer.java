package id.ac.tazkia.payment.bnisyariah.ecollection.scheduler;

import id.ac.tazkia.payment.bnisyariah.ecollection.service.RunningNumberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RunningNumberInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunningNumberInitializer.class);

    @Autowired private RunningNumberService runningNumberService;

    @Scheduled(cron = "1 0 0 * * *")
    public void initBod() {
        LOGGER.info("Inisialisasi running number");
        runningNumberService.getNumber();
    }
}
