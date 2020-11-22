package io.hogenboom.customerstatementprocessor.deserialization;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DeserializeStrategies {
    private final Map<ContentType, StatementDeserializer> strategies;

    public DeserializeStrategies(List<StatementDeserializer> deserializers) {
        this.strategies = deserializers
                .stream().collect(Collectors.toMap(
                        StatementDeserializer::getContentType,
                        Function.identity()
                ));
    }

    public StatementDeserializer getDeserializer(ContentType contentType) {
        return this.strategies.get(contentType);
    }
}
