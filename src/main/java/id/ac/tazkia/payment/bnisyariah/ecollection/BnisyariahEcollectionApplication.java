package id.ac.tazkia.payment.bnisyariah.ecollection;

import id.ac.tazkia.payment.bnisyariah.ecollection.dao.PaymentDao;
import id.ac.tazkia.payment.bnisyariah.ecollection.dao.ResendPaymentDao;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.Payment;
import id.ac.tazkia.payment.bnisyariah.ecollection.entity.ResendPayment;
import id.ac.tazkia.payment.bnisyariah.ecollection.service.KafkaSenderService;
import id.ac.tazkia.payment.bnisyariah.ecollection.service.RunningNumberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka @EnableAsync @EnableScheduling
@SpringBootApplication
public class BnisyariahEcollectionApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(BnisyariahEcollectionApplication.class);

	@Autowired private RunningNumberService runningNumberService;
	@Autowired private KafkaSenderService kafkaSenderService;
	@Autowired private PaymentDao paymentDao;
	@Autowired private ResendPaymentDao resendPaymentDao;

	@Override
	public void run(String... args) throws Exception {
		LOGGER.debug("Inisialisasi Running Number");
		runningNumberService.getNumber();
		resendPayment();
	}

	private void resendPayment() {
		Integer counter = 0;
		for (ResendPayment rp : resendPaymentDao.findAll()) {
			for (Payment payment : paymentDao.findByPaymentReference(rp.getPaymentReference())) {
				LOGGER.info("Resend payment : {} - {} - {}", payment.getVirtualAccount().getAccountNumber(), payment.getPaymentReference(), payment.getAmount());
				kafkaSenderService.sendPaymentNotification(payment);
				counter++;
			}
			resendPaymentDao.delete(rp);
		}
		LOGGER.info("Resend {} payments", counter);
	}

	public static void main(String[] args) {
		SpringApplication.run(BnisyariahEcollectionApplication.class, args);
	}
}
