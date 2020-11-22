package io.hogenboom.customerstatementprocessor.model;

import lombok.Getter;

public enum CsvHeader {

    REFERNECE("Reference"),
    ACCOUNT_NUMBER("Account Number"),
    DESCRIPTION("Description"),
    START_BALANCE("Start Balance"),
    MUTATION("Mutation"),
    END_BALANCE("End Balance");


    @Getter
    String value;

    CsvHeader(String value) {
        this.value = value;
    }

}
