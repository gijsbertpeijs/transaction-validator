package nl.rabodemo.transactionvalidator;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@TestPropertySource(locations="classpath:application.properties")
@RunWith(SpringRunner.class)
@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = { BatchConfiguration.class, JobCompletionNotificationListener.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

class TransactionValidatorBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        Logger testLogger = (Logger) LoggerFactory.getLogger(JobCompletionNotificationListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        testLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "transaction");
    }

    @After
    public void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        return paramsBuilder.toJobParameters();
    }

    @Test
    void jobRunsSuccessfully() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        assertThat(actualJobInstance.getJobName(), is("importTransactionJob"));
        assertThat(actualJobExitStatus.getExitCode(), is("COMPLETED"));

        List<ILoggingEvent> logsList = listAppender.list;
        Assertions.assertEquals(17, logsList.size());
        Assertions.assertEquals("Transactions with incorrect end balance:", logsList.get(5).getMessage());
        Assertions.assertEquals("Reference 112806, Description: Tickets for Jan Bakker.", logsList.get(6).getMessage());
        Assertions.assertEquals("Reference 176186, Description: Subscription from Peter Bakker.", logsList.get(7).getMessage());
        Assertions.assertEquals("Reference 145501, Description: Flowers for Daniël Theuß.", logsList.get(8).getMessage());
        Assertions.assertEquals("", logsList.get(9).getMessage());

        Assertions.assertEquals("Transactions with duplicate reference:", logsList.get(10).getMessage());
        Assertions.assertEquals("Reference 112806, Description: Subscription from Willem King.", logsList.get(11).getMessage());
        Assertions.assertEquals("Reference 112806, Description: Tickets for Jan Bakker.", logsList.get(12).getMessage());
        Assertions.assertEquals("Reference 112806, Description: Clothes from Daniël de Vries.", logsList.get(13).getMessage());
        Assertions.assertEquals("", logsList.get(14).getMessage());
    }
}
