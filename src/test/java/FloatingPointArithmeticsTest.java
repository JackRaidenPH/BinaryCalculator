import dev.jackraidenph.binarycalculator.binarycontainer.BinaryFloat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FloatingPointArithmeticsTest {

    private static final Integer LOWER_TEST = -0b111111111111; //-4095
    private static final Integer UPPER_TEST = 0b111111111111; //4095
    private static final Float DELTA = 0.0175f;

    private static Stream<Arguments> fpProvider12bit() {
        return IntStream
                .range(LOWER_TEST, UPPER_TEST + 1)
                .mapToObj(i -> Arguments.of(((float) i) / 100f));
    }

    @ParameterizedTest
    @MethodSource("fpProvider12bit")
    void addition(Float in) {
        assertEquals(UPPER_TEST + in, new BinaryFloat((float) UPPER_TEST).add(in).getDecimal(), DELTA, "Args: %f".formatted(in));
    }

    @ParameterizedTest
    @MethodSource("fpProvider12bit")
    void subtraction(Float in) {
        assertEquals(UPPER_TEST - in, new BinaryFloat((float) UPPER_TEST).subtract(in).getDecimal(), DELTA, "Args: %f".formatted(in));
    }

    @ParameterizedTest
    @MethodSource("fpProvider12bit")
    void multiplication(Float in) {
        assertEquals(UPPER_TEST * in, new BinaryFloat((float) UPPER_TEST).multiply(in).getDecimal(), DELTA, "Args: %f".formatted(in));
    }

    @ParameterizedTest
    @MethodSource("fpProvider12bit")
    void division(Float in) {
        if (Math.signum(in) == 0.0)
            assertThrows(ArithmeticException.class, () -> new BinaryFloat((float) UPPER_TEST).divideBy(in).getDecimal());
        else
            assertEquals(UPPER_TEST / in, new BinaryFloat((float) UPPER_TEST).divideBy(in).getDecimal(), DELTA, "Args: %f".formatted(in));
    }
}
