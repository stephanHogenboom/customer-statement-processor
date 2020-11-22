package io.hogenboom.customerstatementprocessor.deserialization;

import io.hogenboom.customerstatementprocessor.model.MT940Record;
import io.vavr.control.Try;

import java.util.List;

public interface StatementDeserializer {
    ContentType getContentType();
    Try<List<Try<MT940Record>>> deserialize(String content);
}
