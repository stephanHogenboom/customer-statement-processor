package io.hogenboom.customerstatementprocessor.deserialization;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.hogenboom.customerstatementprocessor.deserialization.model.inbound.xml.Records;
import io.hogenboom.customerstatementprocessor.model.AccountNumber;
import io.hogenboom.customerstatementprocessor.model.MT940Record;
import io.vavr.control.Try;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class XmlStatementDeserializer implements StatementDeserializer {

    private final XmlMapper mapper;

    public XmlStatementDeserializer(XmlMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ContentType getContentType() {
        return ContentType.XML;
    }

    public Try<List<Try<MT940Record>>> deserialize(String xml) {
        return Try.of(() -> {
            var records = mapper.readValue(xml, Records.class);
            return records.getRecords()
                    .stream()
                    .map(this::toTriedMT940Record)
                    .collect(Collectors.toList());
        });
    }

    private Try<MT940Record> toTriedMT940Record(io.hogenboom.customerstatementprocessor.deserialization.model.inbound.xml.Record r) {
        return Try.of(() ->
                new MT940Record(
                        r.getReference(),
                        new AccountNumber(r.getAccountNumber()),
                        r.getStartBalance(),
                        r.getMutation(),
                        r.getDescription(),
                        r.getEndBalance()
                )
        );
    }
}
