package nl.rabodemo.transactionvalidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransactionBatchValidatorApplication {

    private static final Logger LOG = LoggerFactory
            .getLogger(TransactionBatchValidatorApplication.class);

    public static void main(String[] args) {
        LOG.info("Starting the Batch Transaction Validator");
        SpringApplication.run(TransactionBatchValidatorApplication.class, args);
        LOG.info("Finishing the Batch Transaction Validator");
    }
}
