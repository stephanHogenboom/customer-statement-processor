package io.hogenboom.customerstatementprocessor.service;

import io.hogenboom.customerstatementprocessor.deserialization.ContentType;
import io.hogenboom.customerstatementprocessor.deserialization.DeserializeStrategies;
import io.hogenboom.customerstatementprocessor.model.MT940Record;
import io.hogenboom.customerstatementprocessor.validation.StatementValidator;
import io.vavr.control.Try;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This component contains a component that determines which deserialization should be used. After the
 * the deserialization is done it delegates the validation to validator. If both are done without errors or violations
 * a valid result will be returned. Other wise an invalid result will be returned which will explain if either the
 * failed or if there were any violation errors.
 */
@Component
public class ValidationService {
    private final StatementValidator validator;
    private final DeserializeStrategies strategies;

    public ValidationService(StatementValidator validator, DeserializeStrategies strategies) {
        this.validator = validator;
        this.strategies = strategies;
    }

    public ValidationResult deserializeAndValidateContent(ContentType contentType, String content) {
        var deserializer = strategies.getDeserializer(contentType);
        return deserializer.deserialize(content).fold(
                t -> ValidationResult.deserializationFailed(t.getMessage()),
                validateAndMapToAggregateResult()
        );
    }

    private Function<List<Try<MT940Record>>, ValidationResult> validateAndMapToAggregateResult() {
        return triedRecords -> {
            var hasMappingErrors = !triedRecords.stream().allMatch(Try::isSuccess);
            if (hasMappingErrors) {
                var mapErrors = triedRecords.stream()
                        .filter(Try::isFailure)
                        .map(Try::getCause)
                        .map(Throwable::getMessage)
                        .collect(Collectors.joining(", "));
                return ValidationResult.deserializationFailed(mapErrors);
            }
            var records = triedRecords
                    .stream()
                    .filter(Try::isSuccess) //No Failures should be in the list at this point
                    .map(Try::get)
                    .collect(Collectors.toList());

            var result = validator.validate(records);
            if (result.isValid()) {
                return ValidationResult.valid();
            }
            var validationViolations = Stream.concat(
                    result.getAggregateViolations().stream(),
                    result.getRecordWithValidationViolations().stream()
                            .flatMap(r -> r.getValidationViolations().stream())
            ).collect(Collectors.toList());
            return ValidationResult.invalid(validationViolations);
        };
    }

    @Value
    public static class ValidationResult {
        boolean deserializationSucceeded;
        String deserializationErrorMessage;
        boolean validationSucceeded;
        List<String> validationViolations;

        public static ValidationResult deserializationFailed(String errorMessage) {
            return new ValidationResult(false, errorMessage, false, Collections.emptyList());
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, "", true, Collections.emptyList());
        }

        public static ValidationResult invalid(List<String> validationViolations) {
            return new ValidationResult(true, "", false, validationViolations);
        }
    }
}
