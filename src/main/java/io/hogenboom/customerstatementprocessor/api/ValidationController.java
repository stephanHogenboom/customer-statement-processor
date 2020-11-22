package io.hogenboom.customerstatementprocessor.api;

import io.hogenboom.customerstatementprocessor.ValidationService;
import io.hogenboom.customerstatementprocessor.deserialization.ContentType;
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
@RequestMapping("/validate")
public class ValidationController {

    private final ValidationService service;

    private final Set<String> allowedContentTypes = Set.of(
            "text/xml", "application/xml", "application/CSV"
    );

    public ValidationController(ValidationService service) {
        this.service = service;
    }

    @PostMapping("/")
    public ResponseEntity<ValidationService.ValidationResult> validate(
            @RequestHeader("Content-Type") String contentType,
            HttpServletRequest request) throws IOException {
        if (allowedContentTypes.contains(contentType)) {
            var type = Set.of("text/xml", "application/xml").contains(contentType)
                    ? ContentType.CSV
                    : ContentType.XML;
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
                                allowedContentTypes
                        )
                ));
    }

}
