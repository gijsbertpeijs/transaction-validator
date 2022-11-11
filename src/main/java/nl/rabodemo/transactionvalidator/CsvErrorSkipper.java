package nl.rabodemo.transactionvalidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

public class CsvErrorSkipper implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger(CsvErrorSkipper.class);

    @Override
    public boolean shouldSkip(Throwable throwable, int skipCounter) throws SkipLimitExceededException {

        if (throwable instanceof FlatFileParseException) {
            log.error("Skipping line " + ((FlatFileParseException) throwable).getLineNumber() + " due to malformed CSV" );
        } else {
            log.error("Skipping line due to generic error:" + throwable.getMessage());
        }

        return false;
    }
}
