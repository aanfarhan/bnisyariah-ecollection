package id.ac.tazkia.payment.bnisyariah.ecollection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@EntityScan(
		basePackageClasses = {BnisyariahEcollectionApplication.class, Jsr310JpaConverters.class}
)
public class BnisyariahEcollectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(BnisyariahEcollectionApplication.class, args);
	}
}
