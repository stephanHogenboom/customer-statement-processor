package io.hogenboom.customerstatementprocessor.deserialization.model.inbound.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement
public class Records {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "record")
    List<Record> records = new ArrayList<>();
}
