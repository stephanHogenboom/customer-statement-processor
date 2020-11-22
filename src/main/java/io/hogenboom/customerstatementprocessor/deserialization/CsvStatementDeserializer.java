package io.hogenboom.customerstatementprocessor.deserialization;

import io.hogenboom.customerstatementprocessor.deserialization.model.inbound.csv.RecordCsvHeader;
import io.hogenboom.customerstatementprocessor.model.AccountNumber;
import io.hogenboom.customerstatementprocessor.model.MT940Record;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CsvStatementDeserializer implements StatementDeserializer {
    private final CSVFormat format;

    public CsvStatementDeserializer(CSVFormat format) {
        this.format = format;
    }

    @Override
    public ContentType getContentType() {
        return ContentType.CSV;
    }

    public Try<List<Try<MT940Record>>> deserialize(String csv) {
        return Try.of(() ->
                CSVParser.parse(csv, format)
                        .getRecords()
                        .stream()
                        .map(toMT940Record())
                        .collect(Collectors.toList())
        );
    }

    private Function<CSVRecord, Try<MT940Record>> toMT940Record() {
        return record -> Try.of(() -> {
            var reference = Long.parseLong(record.get(RecordCsvHeader.REFERNECE.getValue()));
            var accountNumber = new AccountNumber(record.get(RecordCsvHeader.ACCOUNT_NUMBER.getValue()));
            var description = record.get(RecordCsvHeader.DESCRIPTION.getValue());
            var startBalance = new BigDecimal(record.get(RecordCsvHeader.START_BALANCE.getValue()));
            var mutation = new BigDecimal(record.get(RecordCsvHeader.MUTATION.getValue()));
            var endBalance = new BigDecimal(record.get(RecordCsvHeader.END_BALANCE.getValue()));
            return new MT940Record(
                    reference,
                    accountNumber,
                    startBalance,
                    mutation,
                    description,
                    endBalance
            );
        });
    }
}
