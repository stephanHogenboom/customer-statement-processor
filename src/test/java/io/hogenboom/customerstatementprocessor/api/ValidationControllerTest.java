package io.hogenboom.customerstatementprocessor.api;

import io.hogenboom.customerstatementprocessor.configuration.CsvConfiguration;
import io.hogenboom.customerstatementprocessor.configuration.XmlConfiguration;
import io.hogenboom.customerstatementprocessor.deserialization.CsvStatementDeserializer;
import io.hogenboom.customerstatementprocessor.deserialization.DeserializeStrategies;
import io.hogenboom.customerstatementprocessor.deserialization.XmlStatementDeserializer;
import io.hogenboom.customerstatementprocessor.service.ValidationService;
import io.hogenboom.customerstatementprocessor.validation.StatementValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = ValidationController.class)
@Import(value = {
        XmlConfiguration.class, CsvConfiguration.class,
        CsvStatementDeserializer.class, XmlStatementDeserializer.class,
        StatementValidator.class, DeserializeStrategies.class,
        ValidationService.class, ValidationController.class
})
class ValidationControllerTest {

    @Autowired
    protected MockMvc mvc;

    @Test
    public void shouldAcceptXmlAndReturnResult() throws Exception {
        var validXml = "<records>\n" +
                "    <record reference=\"199352\">\n" +
                "        <accountNumber>NL74ABNA0248990274</accountNumber>\n" +
                "        <description>Subscription for Daniël Theuß</description>\n" +
                "        <startBalance>73.32</startBalance>\n" +
                "        <mutation>-11.59</mutation>\n" +
                "        <endBalance>61.73</endBalance>\n" +
                "    </record>\n" +
                "    <record reference=\"159469\">\n" +
                "        <accountNumber>NL74ABNA0248990274</accountNumber>\n" +
                "        <description>Subscription for Peter de Vries</description>\n" +
                "        <startBalance>58.74</startBalance>\n" +
                "        <mutation>+0.16</mutation>\n" +
                "        <endBalance>58.9</endBalance>\n" +
                "    </record>\n" +
                "</records>";

        mvc.perform(
                post("/validate")
                        .contentType(MediaType.APPLICATION_XML_VALUE)
                        .content(validXml)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().xml(
                        "<ValidationResult>" +
                                "   <deserializationSucceeded>true</deserializationSucceeded>" +
                                "   <deserializationErrorMessage></deserializationErrorMessage>" +
                                "   <validationSucceeded>true</validationSucceeded>" +
                                "</ValidationResult>"
                ));
    }

    @Test
    public void shouldReturnBadRequestIfNotACorrectMediaType() throws Exception {
        var content = "";
        mvc.perform(
                post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().xml(
                        "<ValidationResult>" +
                                "   <deserializationSucceeded>false</deserializationSucceeded>" +
                                "   <deserializationErrorMessage>application/json;charset=UTF-8 is not an allowed content-type, should be one of [application/xml, application/CSV]</deserializationErrorMessage>" +
                                "   <validationSucceeded>false</validationSucceeded>" +
                                "</ValidationResult>"
                ));
    }

}