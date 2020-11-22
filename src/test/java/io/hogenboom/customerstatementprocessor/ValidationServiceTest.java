package io.hogenboom.customerstatementprocessor;

import io.hogenboom.customerstatementprocessor.deserialization.ContentType;
import io.hogenboom.customerstatementprocessor.model.AccountNumber;
import io.hogenboom.customerstatementprocessor.model.MT940Record;
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
@SpringBootTest
class ValidationServiceTest {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private ResourceLoader resourceLoader = null;

    @Test
    public void shouldReturnInvalidResultOnDuplicateCsv() throws IOException {
        var csv = Files.lines(
                Paths.get(
                        resourceLoader.getResource("classpath:data/test.csv").getFile().getPath()
                )
        ).collect(Collectors.joining("\n"));
        var result = validationService.deserializeAndValidateContent(ContentType.CSV, csv);
        assertThat(result).isEqualTo(ValidationService.ValidationResult.invalid(List.of("The following transaction references were duplicate [112806]")));
    }

    @Test
    public void shouldReturnInvalidResultOnIncorrectMutationXml() throws IOException {
        var xml = Files.lines(
                Paths.get(
                        resourceLoader.getResource("classpath:data/test.xml").getFile().getPath()
                )
        ).collect(Collectors.joining("\n"));
        var result = validationService.deserializeAndValidateContent(ContentType.XML, xml);
        var invalidRecord1 = new MT940Record(
                110666,
                new AccountNumber("NL91RABO0315273637"),
                new BigDecimal("5429"),
                new BigDecimal("-939"),
                "Candy for Vincent de Vries",
                new BigDecimal("6368")
        );
        var invalidRecord2 = new MT940Record(
                131390,
                new AccountNumber("NL27SNSB0917829871"),
                new BigDecimal("3980"),
                new BigDecimal("1000"),
                "Candy for Jan Theuß",
                new BigDecimal("4981")
        );
        assertThat(result).isEqualTo(ValidationService.ValidationResult.invalid(List.of(
                String.format("The mutation of record %s was not equal to the difference '939' between the start and end balance", invalidRecord1),
                String.format("The mutation of record %s was not equal to the difference '1001' between the start and end balance", invalidRecord2)

        )));
    }

    @Test
    public void shouldReturnInvalidIfWrongContentTypeIsGivenXmlAsScv() throws IOException {
        var xml = Files.lines(
                Paths.get(
                        resourceLoader.getResource("classpath:data/test.xml").getFile().getPath()
                )
        ).collect(Collectors.joining("\n"));
        var result = validationService.deserializeAndValidateContent(ContentType.CSV, xml);
        assertThat(result.isDeserializationSucceeded()).isFalse();
        assertThat(result.isValidationSucceeded()).isFalse();
        assertThat(result.getDeserializationErrorMessage()).contains("expected one of [<?xml version=\"1.0\" ?>]");
    }

    @Test
    public void shouldReturnInvalidIfWrongContentTypeIsGivenCsvAsXml() throws IOException {
        var csv = Files.lines(
                Paths.get(
                        resourceLoader.getResource("classpath:data/test.csv").getFile().getPath()
                )
        ).collect(Collectors.joining("\n"));
        var result = validationService.deserializeAndValidateContent(ContentType.XML, csv);
        assertThat(result.isDeserializationSucceeded()).isFalse();
        assertThat(result.isValidationSucceeded()).isFalse();
        assertThat(result.getDeserializationErrorMessage()).contains("Unexpected character");
    }

    @Test
    public void shouldReturnvalidICorrectCsvIsGiven() throws IOException {
        var csv = "Reference\tAccount Number\tDescription\tStart Balance\tMutation\tEnd Balance\n" +
                "116249\tNL32RABO0195610843\tCandy from Peter de Vries\t74.85\t31.89\t106.74\n" +
                "112806\tNL93ABNA0585619023\tTickets from Willem Bakker\t48.89\t-33.47\t15.42";
        var result = validationService.deserializeAndValidateContent(ContentType.CSV, csv);
        assertThat(result).isEqualTo(ValidationService.ValidationResult.valid());
    }

    @Test
    public void shouldReturnvalidICorrecXmlIsGiven() throws IOException {
        var xml = "<records>\n" +
                "    <record reference=\"199352\">\n" +
                "        <accountNumber>NL74ABNA0248990274</accountNumber>\n" +
                "        <description>Subscription for Daniël Theuß</description>\n" +
                "        <startBalance>73.32</startBalance>\n" +
                "        <mutation>-11.59</mutation>\n" +
                "        <endBalance>61.73</endBalance>\n" +
                "    </record>\n" +
                "    <record reference=\"159469\">\n" +
                "        <accountNumber>NL74ABNA0248990274</accountNumber>\n" +
                "        <description>Subscription for Peter de Vries</description>\n" +
                "        <startBalance>58.74</startBalance>\n" +
                "        <mutation>+0.16</mutation>\n" +
                "        <endBalance>58.9</endBalance>\n" +
                "    </record>\n" +
                "</records>";

        var result = validationService.deserializeAndValidateContent(ContentType.XML, xml);
        assertThat(result).isEqualTo(ValidationService.ValidationResult.valid());
    }

}