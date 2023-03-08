package dev.jackraidenph.binarycalculator.binarycontainer;

import dev.jackraidenph.binarycalculator.utility.BinaryArrayUtils;

import java.util.Arrays;

public class BinaryFloat extends BinaryContainer<Float, BinaryFloat> {
    public static int LENGTH = 32;
    public static int MANTISSA_SIZE = 23;
    public static int EXPONENT_SIZE = 8;
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
        if(pFloat == 0)
            return;

        int sign = (int) Math.signum(pFloat);
        pFloat = Math.abs(pFloat);
        float fractional = pFloat % 1;
        int integer = (int) (pFloat - fractional);

        int fractionOffset = 0;

        boolean[] binaryIntegerPart = BinaryArrayUtils.magnitudeToBinary(integer, 24);
        boolean[] binaryFractionalPart = new boolean[24];
        int integerOffset = BinaryArrayUtils.mostSignificantOne(binaryIntegerPart);

        int eCorrection = 0;
        while (!(Math.signum(fractional) == 0) && fractional < 1.0f && eCorrection < 127) {
            fractional *= 2;
            eCorrection++;
        }

        for (int i = 23; (i >= 0) && !(Math.signum(fractional) == 0); i--) {
            if (fractional >= 1.0) {
                fractionOffset = Math.max(fractionOffset, i);
                binaryFractionalPart[i] = true;
                fractional -= 1;
            }
            fractional *= 2;
        }

        boolean[] biasedExponent = new boolean[EXPONENT_SIZE];
        System.arraycopy(bias, 0, biasedExponent, 0, EXPONENT_SIZE);

        if (integerOffset >= 0) {
            System.arraycopy(binaryIntegerPart, 0, container, MANTISSA_SIZE - integerOffset, integerOffset);
            if(eCorrection != 0)
                System.arraycopy(binaryFractionalPart, integerOffset + eCorrection, container, 0, MANTISSA_SIZE - integerOffset - (eCorrection - 1));
            biasedExponent = BinaryArrayUtils.binaryAdd(biasedExponent, BinaryArrayUtils.magnitudeToBinary(integerOffset, EXPONENT_SIZE));
        }
        else {
            System.arraycopy(binaryFractionalPart, 0, container, MANTISSA_SIZE - BinaryArrayUtils.mostSignificantOne(binaryFractionalPart), BinaryArrayUtils.mostSignificantOne(binaryFractionalPart));
            biasedExponent = BinaryArrayUtils.binarySubtract(biasedExponent, BinaryArrayUtils.magnitudeToBinary(eCorrection, EXPONENT_SIZE));
        }

        System.arraycopy(biasedExponent, 0, container, MANTISSA_SIZE, EXPONENT_SIZE);

        container[LENGTH - 1] = sign < 0;
    }

    @Override
    public BinaryFloat multiply(BinaryFloat toMultiplyBy) {
        BinaryFloat copy = new BinaryFloat(this);

        boolean sign = copy.container[LENGTH - 1] ^ toMultiplyBy.container[LENGTH - 1];
        copy.setSign(sign);

        boolean[] e1 = copy.getExponent();
        boolean[] e2 = toMultiplyBy.getExponent();
        boolean[] e = BinaryArrayUtils.binaryAdd(e1, e2);
        e = BinaryArrayUtils.binarySubtract(e, bias);

        boolean[] mul = BinaryArrayUtils.binaryMultiply(copy.getFullMantissa(), toMultiplyBy.getFullMantissa());

        if (mul[MANTISSA_SIZE * 2 + 1]) {
            mul = BinaryArrayUtils.binaryShiftRight(mul, 1);
            e = BinaryArrayUtils.increment(e);
        }

        int shift = MANTISSA_SIZE * 2 - (BinaryArrayUtils.mostSignificantOne(mul) - 1);
        mul = BinaryArrayUtils.binaryShiftLeft(mul, shift);
        boolean[] shorten = new boolean[MANTISSA_SIZE];
        System.arraycopy(mul, MANTISSA_SIZE + 1, shorten, 0, MANTISSA_SIZE);

        for (int i = 0; i < shift - 1; i++) e = BinaryArrayUtils.decrement(e);

        copy.setExponent(e);
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

        boolean[] e1 = copy.getExponent();
        boolean[] e2 = toDivideBy.getExponent();
        boolean[] e;

        //Calculate result exponent
        if (BinaryArrayUtils.binaryGreaterOrEquals(e1, e2)) {
            e = BinaryArrayUtils.binarySubtract(e1, e2);
            e = BinaryArrayUtils.binaryAdd(bias, e);
        } else {
            e = BinaryArrayUtils.binarySubtract(e2, e1);
            e = BinaryArrayUtils.binarySubtract(bias, e);
        }

        //Due to combination of non-resizable nature of arrays,
        //binary nature of boolean and leading zeroes significance for normalization,
        //potential leading zero must be accounted manually
        if (!BinaryArrayUtils.binaryGreaterOrEquals(copy.getFullMantissa(), toDivideBy.getFullMantissa()))
            e = BinaryArrayUtils.decrement(e);

        //Extend dividend mantissa for better precision
        boolean[] extended = new boolean[MANTISSA_SIZE * 2 + 2];
        System.arraycopy(copy.getFullMantissa(), 0, extended, MANTISSA_SIZE + 1, MANTISSA_SIZE + 1);
        boolean[] div = BinaryArrayUtils.binaryDivide(extended, toDivideBy.getFullMantissa()).getKey();

        //Normalization step
        int shift = MANTISSA_SIZE - (BinaryArrayUtils.mostSignificantOne(div) - 1);
        div = BinaryArrayUtils.binaryShiftLeft(div, shift);
        //Truncate mantissa back to 24-1 bits
        boolean[] shorten = new boolean[MANTISSA_SIZE];
        System.arraycopy(div, 1, shorten, 0, MANTISSA_SIZE);

        //Account for normalization shift
        for (int i = 0; i < shift - 1; i++) e = BinaryArrayUtils.decrement(e);

        copy.setExponent(e);
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

        boolean s1 = copy.container[LENGTH - 1];
        boolean s2 = toAdd.container[LENGTH - 1];
        boolean s = s1 ^ s2;

        boolean[] e1 = copy.getExponent();
        boolean[] e2 = toAdd.getExponent();
        boolean[] dE = BinaryArrayUtils.binarySubtract(e1, e2);

        boolean[] m1 = getFullMantissa();
        boolean[] m2 = toAdd.getFullMantissa();

        m2 = BinaryArrayUtils.binaryShiftRight(m2, BinaryArrayUtils.magnitudeFromBinary(dE));

        boolean carry = !s && BinaryArrayUtils.binaryAddGetCarry(m1, m2).getValue();
        m1 = !s ? BinaryArrayUtils.binaryAdd(m1, m2) : BinaryArrayUtils.binarySubtract(m1, m2);

        if (carry) {
            m1 = BinaryArrayUtils.binaryShiftRight(m1, 1);
            m1[MANTISSA_SIZE] = true;
            e1 = BinaryArrayUtils.increment(e1);
        }

        int shift = MANTISSA_SIZE - (BinaryArrayUtils.mostSignificantOne(m1) - 1);
        m1 = BinaryArrayUtils.binaryShiftLeft(m1, shift);
        boolean[] shorten = new boolean[MANTISSA_SIZE];
        System.arraycopy(m1, 1, shorten, 0, MANTISSA_SIZE);
        e1 = BinaryArrayUtils.binarySubtract(e1, BinaryArrayUtils.magnitudeToBinary(shift - 1, EXPONENT_SIZE));

        copy.setSign(s1);
        copy.setExponent(e1);
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
        return e - 127;
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
        if((BinaryArrayUtils.mostSignificantOne(getExponent()) == -1) && BinaryArrayUtils.mostSignificantOne(getMantissa()) == -1)
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