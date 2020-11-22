package io.hogenboom.customerstatementprocessor.validation;

import io.hogenboom.customerstatementprocessor.configuration.CsvConfiguration;
import io.hogenboom.customerstatementprocessor.model.AccountNumber;
import io.hogenboom.customerstatementprocessor.model.MT940Record;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CsvStatementParser.class, CsvConfiguration.class})
class CsvStatementParserTest {
    @Autowired
    private CsvStatementParser parser;

    @Autowired
    private ResourceLoader resourceLoader = null;


    @Test
    public void testParsing() throws IOException {
        var csv = Files.lines(
                Paths.get(
                        resourceLoader.getResource("classpath:data/test.csv").getFile().getPath()
                )
        ).collect(Collectors.joining("\n"));
        List<Try<MT940Record>> records = parser.parse(csv);
        records.forEach(System.out::println);
        assertThat(records).hasSize(10);

        //116249	NL32RABO0195610843	Candy from Peter de Vries	74.85	31.89	106.74
        assertThat(records.get(0).isFailure()).isFalse();
        assertThat(records.get(0).toEither().get()).satisfies(record -> {
            assertThat(record.getTransactionReference()).isEqualTo(116249L);
            assertThat(record.getAccountNumber()).isEqualTo(new AccountNumber("NL32RABO0195610843"));
            assertThat(record.getDescription()).isEqualTo("Candy from Peter de Vries");
            assertThat(record.getStartBalance()).isEqualTo(new BigDecimal("74.85"));
            assertThat(record.getMutation()).isEqualTo(new BigDecimal("31.89"));
            assertThat(record.getEndBalance()).isEqualTo(new BigDecimal("106.74"));
        });
    }
}
