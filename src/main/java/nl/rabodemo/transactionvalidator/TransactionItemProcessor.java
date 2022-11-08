package nl.rabodemo.transactionvalidator;

import nl.rabodemo.transactionvalidator.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Objects;

public class TransactionItemProcessor implements ItemProcessor<Transaction, Transaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionItemProcessor.class);

    @Override
    public Transaction process(final Transaction transaction) {
        if (isInvalidTransaction(transaction)) {
            transaction.setValid(0);
            log.error("Invalid transaction: {}", transaction);
        }

        return transaction;
    }

    private boolean isInvalidTransaction(Transaction transaction) {
        return !Objects.equals(transaction.getEndBalance(), transaction.getStartBalance().add(transaction.getMutation()));
    }
}
