package dev.jackraidenph.binarycalculator.binarycontainer;

import dev.jackraidenph.binarycalculator.utility.BinaryArrayUtils;
import javafx.util.Pair;

import java.util.Arrays;

public class BinaryInteger extends BinaryContainer<Integer, BinaryInteger> {

    private final BinaryInteger.RepresentationType representation;
    public static int LENGTH = 32;

    public BinaryInteger(Integer integer, RepresentationType type) {
        super(LENGTH);
        switch (type) {
            case SIGNED_MAGNITUDE -> {
                representation = RepresentationType.SIGNED_MAGNITUDE;
                signedMagnitude(integer);
            }
            case ONES_COMPLEMENT -> {
                representation = RepresentationType.ONES_COMPLEMENT;
                onesComplement(integer);
            }
            case TWOS_COMPLEMENT -> {
                representation = RepresentationType.TWOS_COMPLEMENT;
                twosComplement(integer);
            }
            default -> representation = null;
        }
    }

    public BinaryInteger(Integer integer) {
        this(integer, RepresentationType.TWOS_COMPLEMENT);
    }

    public BinaryInteger(BinaryInteger toCopy) {
        super(toCopy);
        representation = toCopy.getRepresentation();
    }

    private void write(Integer integer) {
        System.arraycopy(BinaryArrayUtils.magnitudeToBinary(integer, LENGTH), 0, container, 0, LENGTH);
    }

    private void signedMagnitude(Integer integer) {
        write(integer);
        container[LENGTH - 1] = integer < 0;
    }

    private void onesComplement(Integer integer) {
        write(integer);
        if (integer < 0)
            copyFrom(invert());
    }

    private void twosComplement(Integer integer) {
        write(integer);
        if (integer < 0) {
            copyFrom(invert());
            copyFrom(add(1).get());
        }
    }

    @Override
    public BinaryInteger add(BinaryInteger toAdd) {
        BinaryInteger copy = new BinaryInteger(this);
        boolean sum1Sign = copy.container[LENGTH - 1];
        boolean sum2Sign = toAdd.container[LENGTH - 1];
        boolean isBiggerMagnitude = copy.compareAbs(toAdd) > 0;

        if ((sum1Sign || sum2Sign) && (copy.getRepresentation() != toAdd.getRepresentation()))
            throw new RuntimeException("Can not perform negative sum on different representations!");

        if (getRepresentation().equals(RepresentationType.SIGNED_MAGNITUDE)) {
            if (sum1Sign == sum2Sign)
                return copy.copyFrom(BinaryArrayUtils.binaryAdd(copy.get(), toAdd.get()));
            else
                return isBiggerMagnitude ?
                        copy
                                .copyFrom(BinaryArrayUtils.binarySubtract(copy.get(), toAdd.get()))
                                .setSign(sum1Sign) :
                        toAdd
                                .copyFrom(BinaryArrayUtils.binarySubtract(toAdd.get(), copy.get()))
                                .setSign(sum2Sign);
        } else {
            Pair<boolean[], Boolean> res = BinaryArrayUtils.binaryAddGetCarry(copy.get(), toAdd.get());
            if (getRepresentation().equals(RepresentationType.ONES_COMPLEMENT) && res.getValue())
                return copy.copyFrom(BinaryArrayUtils.increment(res.getKey()));
            return copy.copyFrom(res.getKey());
        }
    }

    public BinaryInteger add(Integer integer) {
        return add(new BinaryInteger(integer, getRepresentation()));
    }

    @Override
    public BinaryInteger subtract(BinaryInteger toSubtract) {
        return add(-1 * toSubtract.getDecimal());
    }

    public BinaryInteger subtract(Integer toSubtract) {
        return add(-1 * toSubtract);
    }

    @Override
    public BinaryInteger multiply(BinaryInteger toMultiplyBy) {
        BinaryInteger res = new BinaryInteger(0, RepresentationType.SIGNED_MAGNITUDE);
        boolean sign = this.container[LENGTH - 1] ^ toMultiplyBy.container[LENGTH - 1];
        res.copyFrom(BinaryArrayUtils.binaryMultiply(this.toSignedMagnitude().get(), toMultiplyBy.toSignedMagnitude().get()));
        return res.setSign(sign);
    }

    @Override
    public BinaryInteger divideBy(BinaryInteger toDivideBy) {
        if(toDivideBy.getDecimal() == 0)
            throw new ArithmeticException("Division by zero");

        BinaryInteger res = new BinaryInteger(0, RepresentationType.SIGNED_MAGNITUDE);
        boolean sign = this.container[LENGTH - 1] ^ toDivideBy.container[LENGTH - 1];
        res.copyFrom(BinaryArrayUtils.binaryDivide(this.toSignedMagnitude().get(), toDivideBy.toSignedMagnitude().get()).getKey());
        return res.setSign(sign);
    }

    public BinaryInteger divideBy(Integer toDivideBy) {
        return divideBy(new BinaryInteger(toDivideBy, getRepresentation()));
    }

    public BinaryInteger multiply(Integer toMultiplyBy) {
        return multiply(new BinaryInteger(toMultiplyBy, getRepresentation()));
    }

    @Override
    public Integer getDecimal() {
        int sum = 0;
        int sign = getSign();
        if (sign > 0)
            for (int i = 0; i < LENGTH - 1; i++)
                sum += container[i] ? Math.pow(2, i) : 0;
        else
            switch (representation) {
                case SIGNED_MAGNITUDE -> {
                    for (int i = 0; i < LENGTH - 1; i++)
                        sum += container[i] ? Math.pow(2, i) : 0;
                    sum *= sign;
                    return sum;
                }
                case ONES_COMPLEMENT -> {
                    for (int i = 0; i < LENGTH; i++) {
                        sum += invert()[i] ? Math.pow(2, i) : 0;
                    }
                    sum *= sign;
                    return sum;
                }
                case TWOS_COMPLEMENT -> {
                    for (int i = 0; i < LENGTH; i++)
                        sum += invert()[i] ? Math.pow(2, i) : 0;
                    sum += 1;
                    sum *= sign;
                    return sum;
                }
            }
        return sum;
    }

    public BinaryInteger.RepresentationType getRepresentation() {
        return representation;
    }

    private boolean[] invert() {
        boolean[] copy = new boolean[LENGTH];
        for (int i = 0; i < LENGTH; i++)
            copy[i] = !container[i];
        return copy;
    }

    @Override
    public int getSign() {
        return container[LENGTH - 1] ? -1 : 1;
    }

    @Override
    protected BinaryInteger setSign(boolean negative) {
        if (!getRepresentation().equals(RepresentationType.SIGNED_MAGNITUDE))
            throw new RuntimeException("Trying to force set sign bit for complement representation!");
        container[LENGTH - 1] = negative;
        return this;
    }

    @Override
    public int compare(BinaryInteger another) {
        int sign = getSign();

        if (sign != another.getSign())
            return sign;

        return sign * compareAbs(another);
    }

    public int compareAbs(BinaryInteger another) {
        boolean[] c1 = get();
        boolean[] c2 = another.get();
        for (int i = c1.length - 1; i > 0; i--)
            if (c1[i] != c2[i])
                return c1[i] ? 1 : -1;

        return 0;
    }

    @Override
    public boolean[] get() {
        if(representation.equals(RepresentationType.SIGNED_MAGNITUDE))
            return Arrays.copyOf(container, container.length - 1);
        return super.get();
    }

    public BinaryInteger toSignedMagnitude() {
        Integer decimal = this.getDecimal();
        return new BinaryInteger(decimal, RepresentationType.SIGNED_MAGNITUDE);
    }

    @Override
    public String toString() {
        return super
                .toString()
                .replaceFirst("(.)", getRepresentation().equals(RepresentationType.SIGNED_MAGNITUDE) ? "$1 " : "$1") +
                " " +
                representation.toString();
    }

    public enum RepresentationType {
        SIGNED_MAGNITUDE,
        ONES_COMPLEMENT,
        TWOS_COMPLEMENT
    }
}
