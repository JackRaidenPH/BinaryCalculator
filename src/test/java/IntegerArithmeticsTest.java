import dev.jackraidenph.binarycalculator.binarycontainer.BinaryInteger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IntegerArithmeticsTest {

    private static final Integer UPPER_TEST31 = Integer.MAX_VALUE >> 1; //2^30-1
    private static final Integer UPPER_TEST15 = Integer.MAX_VALUE >> 15; //2^15-1

    private static Stream<Arguments> integerProvider31bit() {
        return IntStream
                .range(0, 31)
                .mapToObj(p -> Arguments.of((int) Math.pow(2, p) - 1)); //MAX - 2^30-1
    }

    private static Stream<Arguments> integerProvider15bit() {
        return IntStream
                .range(0, 16)
                .mapToObj(p -> Arguments.of((int) Math.pow(2, p) - 1)); //MAX - 2^15-1
    }

    @ParameterizedTest
    @MethodSource("integerProvider31bit")
    void addition(Integer in) {
        System.out.println(in);
        assertEquals(UPPER_TEST31 + in, new BinaryInteger(UPPER_TEST31).add(in).getDecimal(), "Args: %d".formatted(in));
    }

    @ParameterizedTest
    @MethodSource("integerProvider31bit")
    void subtraction(Integer in) {
        assertEquals(UPPER_TEST31 - in, new BinaryInteger(UPPER_TEST31).subtract(in).getDecimal(), "Args: %d".formatted(in));
    }

    @ParameterizedTest
    @MethodSource("integerProvider15bit")
    void multiplication(Integer in) {
        assertEquals(UPPER_TEST15 * in, new BinaryInteger(UPPER_TEST15).multiply(in).getDecimal(), "Args: %d".formatted(in));
    }

    @ParameterizedTest
    @MethodSource("integerProvider15bit")
    void multiplicationNegative(Integer in) {
        assertEquals(UPPER_TEST15 * -in, new BinaryInteger(UPPER_TEST15).multiply(-in).getDecimal(), "Args: %d".formatted(in));
    }

    @ParameterizedTest
    @MethodSource("integerProvider15bit")
    void division(Integer in) {
        if (in == 0)
            assertThrows(ArithmeticException.class, () -> new BinaryInteger(UPPER_TEST15).divideBy(in).getDecimal());
        else
            assertEquals(UPPER_TEST15 / in, new BinaryInteger(UPPER_TEST15).divideBy(in).getDecimal(), "Args: %d".formatted(in));
    }

    @ParameterizedTest
    @MethodSource("integerProvider15bit")
    void divisionNegative(Integer in) {
        if (in == 0)
            assertThrows(ArithmeticException.class, () -> new BinaryInteger(UPPER_TEST15).divideBy(in).getDecimal());
        else
            assertEquals(UPPER_TEST15 / -in, new BinaryInteger(UPPER_TEST15).divideBy(-in).getDecimal(), "Args: %d".formatted(in));
    }
}
