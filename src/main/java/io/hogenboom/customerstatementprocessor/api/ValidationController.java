package io.hogenboom.customerstatementprocessor.api;

import io.hogenboom.customerstatementprocessor.deserialization.ContentType;
import io.hogenboom.customerstatementprocessor.service.ValidationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class ValidationController {

    private final ValidationService service;

    private final Set<String> ALLOWED_MEDIA_TYPES = Set.of("application/CSV", MediaType.APPLICATION_XML_VALUE);

    public ValidationController(ValidationService service) {
        this.service = service;
    }

    @PostMapping("validate")
    public ResponseEntity<ValidationService.ValidationResult> validate(
            @RequestHeader("Content-Type") String contentType,
            HttpServletRequest request) throws IOException {
        if (contentType != null
                && (contentType.contains(MediaType.APPLICATION_XML_VALUE)
                || contentType.contains("application/CSV"))
        ) {
            var type = contentType.contains(MediaType.APPLICATION_XML_VALUE)
                    ? ContentType.XML
                    : ContentType.CSV;
            var content = new BufferedReader(
                    new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            return ResponseEntity.ok(service.deserializeAndValidateContent(type, content));
        }
        return ResponseEntity
                .badRequest()
                .body(ValidationService.ValidationResult.deserializationFailed(
                        String.format("%s is not an allowed content-type, should be one of %s",
                                contentType,
                                ALLOWED_MEDIA_TYPES
                        )
                ));
    }

}
