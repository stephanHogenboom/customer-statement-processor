package io.hogenboom.customerstatementprocessor.deserialization;

import io.hogenboom.customerstatementprocessor.configuration.XmlConfiguration;
import io.hogenboom.customerstatementprocessor.model.AccountNumber;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {XmlStatementDeserializer.class, XmlConfiguration.class})
class XmlStatementDeserializerTest {
    @Autowired
    private XmlStatementDeserializer xmlStatementParser;

    @Autowired
    private ResourceLoader resourceLoader = null;

    @Test
    public void shouldParseXml() throws IOException {

        var xml = Files.lines(
                Paths.get(
                        resourceLoader.getResource("classpath:data/test.xml").getFile().getPath()
                )
        ).collect(Collectors.joining("\n"));
        var triedRecords = xmlStatementParser.deserialize(xml);
        assertThat(triedRecords.isSuccess()).isTrue();
        var records = triedRecords.get();
        assertThat(records).hasSize(10);

        assertThat(records).allSatisfy(Try::isSuccess);
        assertThat(records.get(0).toEither().get()).satisfies(record -> {
            assertThat(record.getAccountNumber()).isEqualTo(new AccountNumber("NL56RABO0149876948"));
            assertThat(record.getDescription()).isEqualTo("Subscription from Daniël Theuß");
            assertThat(record.getStartBalance()).isEqualTo(new BigDecimal("18.42"));
            assertThat(record.getMutation()).isEqualTo(new BigDecimal("-11.13"));
            assertThat(record.getEndBalance()).isEqualTo(new BigDecimal("7.29"));
            assertThat(record.getTransactionReference()).isEqualTo(165442L);
        });
    }
}