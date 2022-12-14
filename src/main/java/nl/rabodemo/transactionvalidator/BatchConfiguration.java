package nl.rabodemo.transactionvalidator;

import nl.rabodemo.transactionvalidator.model.Transaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    public JobBuilderFactory jobBuilderFactory;

    public StepBuilderFactory stepBuilderFactory;

    @Value("${csv.file}")
    private String csvFile;

    @Value("${xml.file}")
    private String xmlFile;

    @Value("${csv.skip.limit}")
    private int csvSkipLimit;

    @Value("${xml.skip.limit}")
    private int xmlSkipLimit;

    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public FlatFileItemReader<Transaction> csvReader() {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("transactionItemReader")
                .resource(new ClassPathResource(csvFile))
                .delimited()
                .names(new String[]{"reference", "accountNumber", "description", "startBalance", "mutation", "endBalance"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(Transaction.class);
                }})
                .encoding("ISO-8859-1")
                .linesToSkip(1)
                .build();
    }

    @Bean
    public ItemReader<Transaction> xmlReader() {
        Jaxb2Marshaller transactionMarshaller = new Jaxb2Marshaller();
        transactionMarshaller.setClassesToBeBound(Transaction.class);

        return new StaxEventItemReaderBuilder<Transaction>()
                .name("xmlReader")
                .resource(new ClassPathResource(xmlFile))
                .addFragmentRootElements("record")
                .strict(true)
                .unmarshaller(transactionMarshaller)
                .build();
    }

    @Bean
    public TransactionItemProcessor processor() {
        return new TransactionItemProcessor();
    }

    @Bean
    public SkipPolicy csvSkipper() {
        return new CsvErrorSkipper();
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO transaction (reference, description, valid) VALUES (:reference, :description, :valid)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importTransactionJob(JobCompletionNotificationListener listener, Step processCsvStep, Step processXmlStep) {
        return jobBuilderFactory.get("importTransactionJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(processCsvStep)
                .next(processXmlStep)
                .end()
                .build();
    }

    @Bean
    public Step processCsvStep(JdbcBatchItemWriter<Transaction> writer) {
        return stepBuilderFactory.get("processCsvStep")
                .<Transaction, Transaction>chunk(10)
                .reader(csvReader())
                .processor(processor())
                .writer(writer)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipPolicy(csvSkipper())
                .skipLimit(csvSkipLimit)
                .build();
    }

    @Bean
    public Step processXmlStep(JdbcBatchItemWriter<Transaction> writer) {
        return stepBuilderFactory.get("processXmlStep")
                .<Transaction, Transaction>chunk(10)
                .reader(xmlReader())
                .processor(processor())
                .writer(writer)
                .faultTolerant()
                .skip(NumberFormatException.class)
                .skipLimit(xmlSkipLimit)
                .build();
    }
}
