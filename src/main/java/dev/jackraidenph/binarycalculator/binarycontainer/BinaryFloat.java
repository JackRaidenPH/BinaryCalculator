package dev.jackraidenph.binarycalculator.binarycontainer;

import dev.jackraidenph.binarycalculator.utility.BinaryArrayUtils;

import java.util.Arrays;

public class BinaryFloat extends BinaryContainer<Float, BinaryFloat> {
    public static final int LENGTH = 32;
    public static final int MANTISSA_SIZE = 23;
    public static final int EXPONENT_SIZE = 8;
    public static final int DECIMAL_BIAS = 127;
    private static final boolean[] bias = new boolean[]{
            true, true, true, true, true, true, true, false
    };

    public BinaryFloat(Float pFloat) {
        super(LENGTH);
        decimalToBinary(pFloat);
    }

    public BinaryFloat(BinaryFloat toCopy) {
        super(toCopy);
    }

    private void decimalToBinary(Float pFloat) {
        if (pFloat == 0)
            return;

        int sign = (int) Math.signum(pFloat);
        pFloat = Math.abs(pFloat);
        float fractional = pFloat % 1;
        int integer = (int) (pFloat - fractional);

        int fractionOffset = 0;

        boolean[] binaryIntegerPart = BinaryArrayUtils.magnitudeToBinary(integer, MANTISSA_SIZE + 1);
        boolean[] binaryFractionalPart = new boolean[MANTISSA_SIZE + 1];
        int integerOffset = BinaryArrayUtils.mostSignificantOne(binaryIntegerPart);

        int fractionalOffset = 0;
        while (!(Math.signum(fractional) == 0) && fractional < 1.0f && fractionalOffset < DECIMAL_BIAS) {
            fractional *= 2;
            fractionalOffset++;
        }

        for (int bit = MANTISSA_SIZE; (bit >= 0) && !(Math.signum(fractional) == 0); bit--) {
            if (fractional >= 1.0) {
                fractionOffset = Math.max(fractionOffset, bit);
                binaryFractionalPart[bit] = true;
                fractional -= 1;
            }
            fractional *= 2;
        }

        boolean[] biasedExponent = new boolean[EXPONENT_SIZE];
        System.arraycopy(bias, 0, biasedExponent, 0, EXPONENT_SIZE);

        if (integerOffset >= 0) {
            System.arraycopy(binaryIntegerPart, 0, container, MANTISSA_SIZE - integerOffset, integerOffset);
            if (fractionalOffset != 0) {
                System.arraycopy(
                        binaryFractionalPart,
                        integerOffset + fractionalOffset,
                        container,
                        0,
                        MANTISSA_SIZE - integerOffset - (fractionalOffset - 1));
            }
            biasedExponent = addToExponent(biasedExponent, integer);
        } else {
            System.arraycopy(
                    binaryFractionalPart,
                    0,
                    container,
                    MANTISSA_SIZE - BinaryArrayUtils.mostSignificantOne(binaryFractionalPart),
                    BinaryArrayUtils.mostSignificantOne(binaryFractionalPart));
            biasedExponent = subtractExponent(biasedExponent, fractionalOffset);
        }

        setExponent(biasedExponent);
        setSign(sign < 0);
    }

    private boolean[] subtractExponent(boolean[] toCorrect, int correction) {
        return BinaryArrayUtils.binarySubtract(
                toCorrect,
                BinaryArrayUtils.magnitudeToBinary(correction, EXPONENT_SIZE));
    }

    private boolean[] addToExponent(boolean[] toCorrect, int correction) {
        return BinaryArrayUtils.binaryAdd(
                toCorrect,
                BinaryArrayUtils.magnitudeToBinary(correction, EXPONENT_SIZE));
    }

    @Override
    public BinaryFloat multiply(BinaryFloat toMultiplyBy) {
        BinaryFloat copy = new BinaryFloat(this);

        boolean sign = copy.container[LENGTH - 1] ^ toMultiplyBy.container[LENGTH - 1];
        copy.setSign(sign);

        boolean[] firstExponent = copy.getExponent();
        boolean[] secondExponent = toMultiplyBy.getExponent();
        boolean[] resultExponent = BinaryArrayUtils.binaryAdd(firstExponent, secondExponent);
        resultExponent = BinaryArrayUtils.binarySubtract(resultExponent, bias);

        boolean[] multiple = BinaryArrayUtils.binaryMultiply(copy.getFullMantissa(),
                toMultiplyBy.getFullMantissa());

        if (multiple[MANTISSA_SIZE * 2 + 1]) {
            multiple = BinaryArrayUtils.binaryShiftRight(multiple, 1);
            resultExponent = BinaryArrayUtils.binaryIncrement(resultExponent);
        }

        int shift = MANTISSA_SIZE * 2 - (BinaryArrayUtils.mostSignificantOne(multiple) - 1);
        multiple = BinaryArrayUtils.binaryShiftLeft(multiple, shift);
        boolean[] shorten = new boolean[MANTISSA_SIZE];
        System.arraycopy(multiple, MANTISSA_SIZE + 1, shorten, 0, MANTISSA_SIZE);

        for (int i = 0; i < shift - 1; i++) resultExponent = BinaryArrayUtils.binaryDecrement(resultExponent);

        copy.setExponent(resultExponent);
        copy.setMantissa(shorten);

        return copy;
    }

    public BinaryFloat multiply(Float toMultiplyBy) {
        return multiply(new BinaryFloat(toMultiplyBy));
    }

    @Override
    public BinaryFloat divideBy(BinaryFloat toDivideBy) {
        if (toDivideBy.getDecimal() == 0)
            throw new ArithmeticException("Division by zero");

        BinaryFloat copy = new BinaryFloat(this);

        //Decide sign
        boolean sign = copy.container[LENGTH - 1] ^ toDivideBy.container[LENGTH - 1];
        copy.setSign(sign);

        boolean[] firstExponent = copy.getExponent();
        boolean[] secondExponent = toDivideBy.getExponent();
        boolean[] resultExponent;

        //Calculate result exponent
        if (BinaryArrayUtils.binaryGreaterOrEquals(firstExponent, secondExponent)) {
            resultExponent = BinaryArrayUtils.binarySubtract(firstExponent, secondExponent);
            resultExponent = BinaryArrayUtils.binaryAdd(bias, resultExponent);
        } else {
            resultExponent = BinaryArrayUtils.binarySubtract(secondExponent, firstExponent);
            resultExponent = BinaryArrayUtils.binarySubtract(bias, resultExponent);
        }

        //Due to combination of non-resizable nature of arrays,
        //binary nature of boolean and leading zeroes significance for normalization,
        //potential leading zero must be accounted manually
        if (!BinaryArrayUtils.binaryGreaterOrEquals(copy.getFullMantissa(), toDivideBy.getFullMantissa()))
            resultExponent = BinaryArrayUtils.binaryDecrement(resultExponent);

        //Extend dividend mantissa for better precision
        boolean[] extended = new boolean[MANTISSA_SIZE * 2 + 2];
        System.arraycopy(copy.getFullMantissa(), 0, extended, MANTISSA_SIZE + 1, MANTISSA_SIZE + 1);
        boolean[] division = BinaryArrayUtils.binaryDivide(extended, toDivideBy.getFullMantissa()).getKey();

        //Normalization step
        int shift = MANTISSA_SIZE - (BinaryArrayUtils.mostSignificantOne(division) - 1);
        division = BinaryArrayUtils.binaryShiftLeft(division, shift);
        //Truncate mantissa back to 24-1 bits
        boolean[] shorten = new boolean[MANTISSA_SIZE];
        System.arraycopy(division, 1, shorten, 0, MANTISSA_SIZE);

        //Account for normalization shift
        for (int i = 0; i < shift - 1; i++) resultExponent = BinaryArrayUtils.binaryDecrement(resultExponent);

        copy.setExponent(resultExponent);
        copy.setMantissa(shorten);

        return copy;
    }

    public BinaryFloat divideBy(Float toDivideBy) {
        return divideBy(new BinaryFloat(toDivideBy));
    }

    @Override
    public BinaryFloat add(BinaryFloat toAdd) {
        if (compare(toAdd) < 0)
            return toAdd.add(this);

        if (toAdd.getDecimal() == 0)
            return this;

        BinaryFloat copy = new BinaryFloat(this);

        boolean firstSign = copy.container[LENGTH - 1];
        boolean secondSign = toAdd.container[LENGTH - 1];
        boolean resulSign = firstSign ^ secondSign;

        boolean[] greaterExponent = copy.getExponent();
        boolean[] lesserExponent = toAdd.getExponent();
        boolean[] exponentDelta = BinaryArrayUtils.binarySubtract(greaterExponent, lesserExponent);

        boolean[] greaterMantissa = getFullMantissa();
        boolean[] lesserMantissa = toAdd.getFullMantissa();

        lesserMantissa =
                BinaryArrayUtils.binaryShiftRight(
                        lesserMantissa, BinaryArrayUtils.magnitudeFromBinary(exponentDelta));

        boolean carry = !resulSign && BinaryArrayUtils.binaryAddGetCarry(greaterMantissa, lesserMantissa).getValue();
        greaterMantissa = !resulSign ?
                BinaryArrayUtils.binaryAdd(greaterMantissa, lesserMantissa) :
                BinaryArrayUtils.binarySubtract(greaterMantissa, lesserMantissa);

        if (carry) {
            greaterMantissa = BinaryArrayUtils.binaryShiftRight(greaterMantissa, 1);
            greaterMantissa[MANTISSA_SIZE] = true;
            greaterExponent = BinaryArrayUtils.binaryIncrement(greaterExponent);
        }

        int shift = MANTISSA_SIZE - (BinaryArrayUtils.mostSignificantOne(greaterMantissa) - 1);
        greaterMantissa = BinaryArrayUtils.binaryShiftLeft(greaterMantissa, shift);
        boolean[] shorten = new boolean[MANTISSA_SIZE];
        System.arraycopy(greaterMantissa, 1, shorten, 0, MANTISSA_SIZE);
        greaterExponent = BinaryArrayUtils.binarySubtract(greaterExponent, BinaryArrayUtils.magnitudeToBinary(shift - 1, EXPONENT_SIZE));

        copy.setSign(firstSign);
        copy.setExponent(greaterExponent);
        copy.setMantissa(shorten);

        return copy;
    }

    public BinaryFloat add(Float toAdd) {
        return add(new BinaryFloat(toAdd));
    }

    @Override
    public BinaryFloat subtract(BinaryFloat toSubtract) {
        return add(toSubtract.setSign(!(toSubtract.getSign() < 0)));
    }

    public BinaryFloat subtract(Float toSubtract) {
        return add(-1 * toSubtract);
    }

    public boolean[] getExponent() {
        return Arrays.copyOfRange(container, MANTISSA_SIZE, MANTISSA_SIZE + EXPONENT_SIZE);
    }

    public void setExponent(boolean[] exponent) {
        System.arraycopy(exponent, 0, container, MANTISSA_SIZE, EXPONENT_SIZE);
    }

    public boolean[] getMantissa() {
        return Arrays.copyOf(container, MANTISSA_SIZE);
    }

    public void setMantissa(boolean[] mantissa) {
        System.arraycopy(mantissa, 0, container, 0, MANTISSA_SIZE);
    }

    public int getDecimalExponent() {
        int e = BinaryArrayUtils.magnitudeFromBinary(getExponent());
        return e - DECIMAL_BIAS;
    }

    public float getDecimalMantissa() {
        float m = 0.f;
        for (int i = MANTISSA_SIZE - 1; i >= 0; i--) {
            if (getMantissa()[i])
                m += Math.pow(2.f, (float) -(MANTISSA_SIZE - i));
        }
        return 1.f + m;
    }

    public boolean exponentGreaterOrEquals(BinaryFloat another) {
        return BinaryArrayUtils.binaryGreaterOrEquals(getExponent(), another.getExponent());
    }

    public boolean exponentEquals(BinaryFloat another) {
        return BinaryArrayUtils.equals(getExponent(), another.getExponent());
    }

    public boolean mantissaGreaterOrEquals(BinaryFloat another) {
        return BinaryArrayUtils.binaryGreaterOrEquals(getMantissa(), another.getMantissa());
    }

    public boolean mantissaEquals(BinaryFloat another) {
        return BinaryArrayUtils.equals(getMantissa(), another.getMantissa());
    }

    @Override
    public int compare(BinaryFloat another) {
        if (!exponentGreaterOrEquals(another))
            return -1;
        if (exponentGreaterOrEquals(another) && !exponentEquals(another))
            return 1;
        if (mantissaEquals(another))
            return 0;
        return mantissaGreaterOrEquals(another) ? 1 : -1;
    }

    public boolean[] getFullMantissa() {
        boolean[] mh = new boolean[MANTISSA_SIZE + 1];
        System.arraycopy(container, 0, mh, 0, MANTISSA_SIZE);
        mh[MANTISSA_SIZE] = true;
        return mh;
    }

    @Override
    public Float getDecimal() {
        if ((BinaryArrayUtils.mostSignificantOne(getExponent()) == -1) &&
                BinaryArrayUtils.mostSignificantOne(getMantissa()) == -1)
            return 0f;

        return getSign() * getDecimalMantissa() * (float) Math.pow(2, getDecimalExponent());
    }

    @Override
    public int getSign() {
        return container[LENGTH - 1] ? -1 : 1;
    }

    @Override
    protected BinaryFloat setSign(boolean negative) {
        container[LENGTH - 1] = negative;
        return this;
    }

    @Override
    public String toString() {
        return super
                .toString()
                .replaceAll(" ", "")
                .replaceFirst("(.{9})", "$1 ")
                .replaceFirst("(.)", "$1 ");
    }
}