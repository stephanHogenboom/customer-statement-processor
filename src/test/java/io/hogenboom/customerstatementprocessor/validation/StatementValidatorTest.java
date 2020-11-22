package io.hogenboom.customerstatementprocessor.validation;

import io.hogenboom.customerstatementprocessor.testdata.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.hogenboom.customerstatementprocessor.testdata.TestData.fixedList;
import static io.hogenboom.customerstatementprocessor.testdata.TestData.randomStream;
import static io.hogenboom.customerstatementprocessor.testdata.TestData.validMT940Record;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = StatementValidator.class)
class StatementValidatorTest {
    @Autowired
    private StatementValidator validator;

    @Test
    public void shouldSuccessfulValidateAListOfValidStatements() {
        var toBeValidatedRecords = TestData.fixedList(10, TestData::validMT940Record);
        var result = validator.validate(toBeValidatedRecords);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getAggregateViolations()).isEmpty();
        assertThat(result.getRecordWithValidationViolations()).isEmpty();
    }

    @Test
    public void shouldSuccessfulValidateAListWithDuplicateReference() {
        final var reference = 13456L;
        var toBeValidatedRecords =
                Stream.of(
                        randomStream(2, 3, () -> validMT940Record(reference)),
                        randomStream(10, 11, TestData::validMT940Record)
                )
                        .flatMap(Function.identity())
                        .collect(Collectors.toList());

        var result = validator.validate(toBeValidatedRecords);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getRecordWithValidationViolations()).isEmpty();
        assertThat(result.getAggregateViolations()).hasSize(1)
                .singleElement()
                .satisfies((msg ->
                        assertThat(msg).isEqualTo(String.format("The following transaction references were duplicate %s",
                                List.of(reference)))
                ));
    }

    @Test
    public void shouldSuccessfulValidateAListWithInvalidMutationValues() {
        var difference = new BigDecimal("1.10");
        var invalidRecords = fixedList(2, () -> TestData.invalidMT940Record(difference));
        var toBeValidatedRecords =
                Stream.of(
                        invalidRecords.stream(),
                        randomStream(10, 11, TestData::validMT940Record)
                )
                        .flatMap(Function.identity())
                        .collect(Collectors.toList());

        var result = validator.validate(toBeValidatedRecords);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getRecordWithValidationViolations()).hasSize(2);
        assertThat(result.getRecordWithValidationViolations()).allSatisfy(r -> {
            assertThat(r.isValid()).isFalse();
            assertThat(r.getValidationViolations())
                    .singleElement()
                    .satisfies(msg ->
                            assertThat(msg).isEqualTo(String.format("The mutation of record %s was not equal to the difference '%s' between the start and end balance",
                                    r.getRecord(),
                                    r.getRecord().getEndBalance().subtract(r.getRecord().getStartBalance())))
                    );
        });
        assertThat(result.getAggregateViolations()).isEmpty();
    }

}