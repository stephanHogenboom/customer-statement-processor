package io.hogenboom.customerstatementprocessor.deserialization.model.inbound.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {
    @JacksonXmlProperty(isAttribute = true)
    long reference;
    String accountNumber;
    String description;
    BigDecimal startBalance;
    BigDecimal mutation;
    BigDecimal endBalance;
}
