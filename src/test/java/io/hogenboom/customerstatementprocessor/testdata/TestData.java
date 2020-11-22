package io.hogenboom.customerstatementprocessor.testdata;

import io.hogenboom.customerstatementprocessor.model.AccountNumber;
import io.hogenboom.customerstatementprocessor.model.MT940Record;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TestData {
    public static MT940Record validMT940Record(long reference) {
        var start = new BigDecimal("2");
        var end = new BigDecimal("5");
        var mutation = end.subtract(start);
        return new MT940Record (
                reference,
                // should probably be an Iban
                new AccountNumber(randomIban()),
                start,
                mutation,
                "someDescription",
                end
        );
    }

    public static MT940Record invalidMT940Record(BigDecimal mutationDifference) {
        var start = new BigDecimal("2");
        var end = new BigDecimal("5");
        var mutation = end
                .add(mutationDifference)
                .subtract(start);
        return new MT940Record (
                new Random().nextLong(),
                // should probably be an Iban
                new AccountNumber(randomIban()),
                start,
                mutation,
                "someDescription",
                end
        );
    }


    public static MT940Record validMT940Record() {
        return validMT940Record(new Random().nextLong());
    }

    private static  String randomIban() {
        String numeric = "0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for(int i = 0; i < 10; i++)
            sb.append(numeric.charAt(rnd.nextInt(numeric.length())));
        return "NL" + randomEnum(Bank.class) + sb.toString();
    }

    private static <T extends Enum<T>> T randomEnum(Class<T> someEnum) {
        T[] enumConstants = someEnum.getEnumConstants();
        return enumConstants[randomInt(0, enumConstants.length)];
    }

    private static int randomInt(int lowerBound, int upperBoundExclusive) {
        return ThreadLocalRandom.current()
                .nextInt(0, upperBoundExclusive - lowerBound) + lowerBound;
    }

    public static <T> Stream<T> randomStream(int minimumLength, int maximumLengthExclusive, Supplier<T> supplier) {
        return IntStream.range(0, randomInt(minimumLength, maximumLengthExclusive))
                .mapToObj(i -> supplier.get());
    }

    public static <T> List<T> fixedList(int length, Supplier<T> supplier) {
        return randomStream(length, length + 1, supplier)
                .collect(toList());
    }

}
