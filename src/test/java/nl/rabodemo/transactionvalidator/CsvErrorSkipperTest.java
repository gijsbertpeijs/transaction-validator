package nl.rabodemo.transactionvalidator;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileParseException;

import java.util.List;

public class CsvErrorSkipperTest {

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        Logger testLogger = (Logger) LoggerFactory.getLogger(CsvErrorSkipper.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        testLogger.addAppender(listAppender);
    }

    @Test
    void shouldSkipWithParseExceptionShouldLogSkippingLineMessage() {
        CsvErrorSkipper es = new CsvErrorSkipper();

        boolean result = es.shouldSkip(new FlatFileParseException(
                "Error parsing flat file", "Something went wrong", 123), 5);

        Assertions.assertFalse(result);
        List<ILoggingEvent> logsList = listAppender.list;
        Assertions.assertEquals("Skipping line 123 due to malformed CSV", logsList.get(0).getMessage());
    }

    @Test
    void shouldSkipWithGenericExceptionLogsGenericMessage() {
        CsvErrorSkipper es = new CsvErrorSkipper();

        boolean result = es.shouldSkip(new NumberFormatException("Generic Exception"), 5);

        Assertions.assertFalse(result);
        List<ILoggingEvent> logsList = listAppender.list;
        Assertions.assertEquals("Skipping line due to generic error:Generic Exception", logsList.get(0).getMessage());
    }
}
