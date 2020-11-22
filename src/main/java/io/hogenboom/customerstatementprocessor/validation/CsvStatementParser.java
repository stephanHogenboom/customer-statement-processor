package io.hogenboom.customerstatementprocessor.validation;

import io.hogenboom.customerstatementprocessor.model.CsvHeader;
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

import static java.util.Collections.emptyList;

@Component
@Slf4j
public class CsvStatementParser {
    private final CSVFormat format;

    public CsvStatementParser(CSVFormat format) {
        this.format = format;
    }

    public List<Try<MT940Record>> parse(String csv) {
        try {
            return CSVParser.parse(csv, format)
                    .getRecords()
                    .stream()
                    .map(toMT940Record())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("error whiles parsing csv record from {}", csv, e);
        }
        return emptyList();
    }

    private Function<CSVRecord, Try<MT940Record>> toMT940Record() {
        return record -> Try.of(() -> {
            var reference = Long.parseLong(record.get(CsvHeader.REFERNECE.getValue()));
            var accountNumber = new AccountNumber(record.get(CsvHeader.ACCOUNT_NUMBER.getValue()));
            var description = record.get(CsvHeader.DESCRIPTION.getValue());
            var startBalance = new BigDecimal(record.get(CsvHeader.START_BALANCE.getValue()));
            var mutation = new BigDecimal(record.get(CsvHeader.MUTATION.getValue()));
            var endBalance = new BigDecimal(record.get(CsvHeader.END_BALANCE.getValue()));
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
