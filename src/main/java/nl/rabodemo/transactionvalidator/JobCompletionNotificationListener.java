package nl.rabodemo.transactionvalidator;

import nl.rabodemo.transactionvalidator.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("");
            log.info("---------------------------------------------------------");
            log.info("-- Validations results ----------------------------------");
            log.info("---------------------------------------------------------");

            log.info("");
            log.info("Transactions with incorrect end balance:");

            jdbcTemplate.query("SELECT reference, description FROM transaction WHERE valid = 0",
                    (rs, row) -> new Transaction(
                            rs.getInt(1),
                            rs.getString(2))
            ).forEach(transaction -> log.info("Reference " + transaction.getReference() + ", Description: " +
                    transaction.getDescription() + "."));

            log.info("");
            log.info("Transactions with duplicate reference:");

            jdbcTemplate.query("SELECT reference, description FROM transaction " +
                            "WHERE reference IN (SELECT reference FROM transaction GROUP BY reference HAVING COUNT(reference) > 1)",
                    (rs, row) -> new Transaction(
                            rs.getInt(1),
                            rs.getString(2))
            ).forEach(transaction -> log.info("Reference " + transaction.getReference() + ", Description: " +
                            transaction.getDescription() + "."));

            log.info("");
            log.info("---------------------------------------------------------");
            log.info("");
        }
    }
}