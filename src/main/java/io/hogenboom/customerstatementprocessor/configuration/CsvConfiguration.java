package io.hogenboom.customerstatementprocessor.configuration;

import org.apache.commons.csv.CSVFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvConfiguration {

    @Bean
    public CSVFormat csvFormat() {
        return CSVFormat.DEFAULT
                .withHeader(
                        "Reference",
                        "Account Number",
                        "Description",
                        "Start Balance",
                        "Mutation",
                        "End Balance"
                )
                .withFirstRecordAsHeader()
                .withDelimiter('\t');
    }
}
