package io.hogenboom.customerstatementprocessor.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class MT940Record {
    long transactionReference;
    AccountNumber accountNumber;
    BigDecimal startBalance;
    BigDecimal mutation;
    String description;
    BigDecimal endBalance;
}
