package io.hogenboom.customerstatementprocessor.deserialization.model.inbound.csv;

import lombok.Getter;

public enum RecordCsvHeader {

    REFERNECE("Reference"),
    ACCOUNT_NUMBER("Account Number"),
    DESCRIPTION("Description"),
    START_BALANCE("Start Balance"),
    MUTATION("Mutation"),
    END_BALANCE("End Balance");

    @Getter
    String value;

    RecordCsvHeader(String value) {
        this.value = value;
    }

}
