package io.hogenboom.customerstatementprocessor.validation;

import io.hogenboom.customerstatementprocessor.model.MT940Record;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.hogenboom.customerstatementprocessor.validation.StatementValidator.ValidationRecordResult.inValid;
import static io.hogenboom.customerstatementprocessor.validation.StatementValidator.ValidationRecordResult.valid;
import static java.util.function.Predicate.not;

@Component
public class StatementValidator {

    public ValidationResultAggregateResult validate(final List<MT940Record> records) {
        final var invalidIndividualValidationResults = records.stream()
                .map(this::validateRecord)
                .filter(not(ValidationRecordResult::isValid))
                .collect(Collectors.toSet());

        final var references = records.stream()
                .map(MT940Record::getTransactionReference)
                .collect(Collectors.toList());
        final var duplicateReferences = findDuplicateElements(references);

        if (!duplicateReferences.isEmpty() || !invalidIndividualValidationResults.isEmpty()) {
            return ValidationResultAggregateResult.invalid(
                    invalidIndividualValidationResults,
                    duplicateReferences.isEmpty()
                            ? Collections.emptySet()
                            : Set.of(
                            String.format(
                                    "The following transaction references were duplicate %s",
                                    duplicateReferences
                            )
                    ));
        }
        return ValidationResultAggregateResult.valid();

    }

    private Set<?> findDuplicateElements(final List<?> elements) {
        final var duplicates = new HashSet<>();
        final var uniques = new HashSet<>();
        elements.forEach(e -> {
            if (!uniques.add(e)) {
                duplicates.add(e);
            }
        });
        return duplicates;
    }


    private ValidationRecordResult validateRecord(MT940Record record) {
        var mutationResult = isValidMutation(record);
        return mutationResult.isValid()
                ? valid(record)
                : inValid(record, formulateMutationErrors(record, mutationResult));
    }

    private Set<String> formulateMutationErrors(MT940Record record, MutationResult mutationResult) {
        return Set.of(
                String.format(
                        "The mutation of record %s was not equal to the difference '%s' between the start and end balance",
                        record, mutationResult.getActualMutation()
                )
        );
    }

    private MutationResult isValidMutation(MT940Record record) {
        return record.getEndBalance().subtract(record.getStartBalance()).equals(record.getMutation())
                ? MutationResult.validMutation()
                : MutationResult.invalidMutation(record.getEndBalance().subtract(record.getStartBalance()));
    }

    @Value
    public static class MutationResult {
        boolean valid;
        BigDecimal actualMutation;

        public static MutationResult validMutation() {
            return new MutationResult(true, BigDecimal.ZERO);
        }

        public static MutationResult invalidMutation(BigDecimal actualMutation) {
            return new MutationResult(false, actualMutation);
        }
    }

    @Value
    public static class ValidationRecordResult {
        MT940Record record;
        boolean valid;
        Set<String> validationViolations;

        public static ValidationRecordResult valid(MT940Record record) {
            return new ValidationRecordResult(record, true, Collections.emptySet());
        }

        public static ValidationRecordResult inValid(MT940Record record, Set<String> validationViolations) {
            return new ValidationRecordResult(record, false, validationViolations);
        }
    }

    @Value
    public static class ValidationResultAggregateResult {
        boolean valid;
        Set<ValidationRecordResult> recordWithValidationViolations;
        Set<String> aggregateViolations;

        public static ValidationResultAggregateResult valid() {
            return new ValidationResultAggregateResult(true, Collections.emptySet(), Collections.emptySet());
        }

        public static ValidationResultAggregateResult invalid(
                Set<ValidationRecordResult> recordWithValidationViolations,
                Set<String> aggregateViolations
        ) {
            return new ValidationResultAggregateResult(false, recordWithValidationViolations, aggregateViolations);
        }
    }
}
