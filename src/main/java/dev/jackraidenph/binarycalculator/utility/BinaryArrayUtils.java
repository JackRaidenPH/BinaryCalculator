package dev.jackraidenph.binarycalculator.utility;

import javafx.util.Pair;

public class BinaryArrayUtils {
    public static Pair<boolean[], Boolean> binaryAddGetCarry(boolean[] addTo, boolean[] addendum) {
        int operationLength = Math.max(addTo.length, addendum.length);
        boolean v1, v2, v3, carry = false;
        boolean[] addToCopy = new boolean[operationLength];
        boolean[] addendumCopy = new boolean[operationLength];
        System.arraycopy(addTo, 0, addToCopy, 0, addTo.length);
        System.arraycopy(addendum, 0, addendumCopy, 0, addendum.length);
        for (int i = 0; i < operationLength; i++) {
            v1 = addToCopy[i] ^ addendumCopy[i];
            v2 = addToCopy[i] & addendumCopy[i];
            v3 = v1 & carry;
            addToCopy[i] = v1 ^ carry;
            carry = v2 | v3;
        }
        return new Pair<>(addToCopy, carry);
    }

    public static boolean[] binaryAdd(boolean[] addTo, boolean[] addendum) {
        return binaryAddGetCarry(addTo, addendum).getKey();
    }

    public static boolean[] binaryAddFull(boolean[] addTo, boolean[] addendum) {
        Pair<boolean[], Boolean> pair = binaryAddGetCarry(addTo, addendum);
        boolean[] res = pair.getKey();
        if (pair.getValue()) {
            res = binaryAddFull(res, new boolean[]{true});
        }
        return res;
    }

    public static Pair<boolean[], Boolean> incrementGetCarry(boolean[] addTo) {
        boolean[] increment = new boolean[addTo.length];
        increment[0] = true;
        return binaryAddGetCarry(addTo, increment);
    }

    public static boolean[] binaryIncrement(boolean[] addTo) {
        return incrementGetCarry(addTo).getKey();
    }

    public static Pair<boolean[], Boolean> binaryDecrementGetBorrow(boolean[] minuend) {
        boolean[] decrement = new boolean[minuend.length];
        decrement[0] = true;
        return binarySubtractGetBorrow(minuend, decrement);
    }

    public static boolean[] binaryDecrement(boolean[] subtrahend) {
        return binaryDecrementGetBorrow(subtrahend).getKey();
    }

    public static Pair<boolean[], Boolean> binarySubtractGetBorrow(boolean[] minuend, boolean[] subtrahend) {
        int opLength = Math.max(minuend.length, subtrahend.length);
        boolean v1, v2, v3, borrow = false;
        boolean[] minuendCopy = new boolean[opLength];
        boolean[] subtrahendCopy = new boolean[opLength];
        System.arraycopy(minuend, 0, minuendCopy, 0, minuend.length);
        System.arraycopy(subtrahend, 0, subtrahendCopy, 0, subtrahend.length);
        for (int i = 0; i < opLength; i++) {
            v1 = minuendCopy[i] ^ subtrahendCopy[i];
            v2 = !minuendCopy[i] & subtrahendCopy[i];
            v3 = !v1 & borrow;
            minuendCopy[i] = v1 ^ borrow;
            borrow = v2 | v3;
        }
        return new Pair<>(minuendCopy, borrow);
    }

    public static boolean[] binarySubtract(boolean[] minuend, boolean[] subtrahend) {
        return binarySubtractGetBorrow(minuend, subtrahend).getKey();
    }

    public static boolean[] binaryMultiply(boolean[] multiplicand, boolean[] multiplier) {
        int operationLength = Math.max(multiplicand.length, multiplier.length);
        boolean[] multiplicandCopy = new boolean[operationLength * 2]; //Multiplicand
        boolean[] multiplierCopy = new boolean[operationLength]; //Multiplier
        System.arraycopy(multiplicand, 0, multiplicandCopy, 0, multiplicand.length);
        System.arraycopy(multiplier, 0, multiplierCopy, 0, multiplier.length);
        boolean[] result = new boolean[operationLength * 2]; //Result

        for (int currentBit = 0; currentBit < operationLength; currentBit++) {
            if (multiplierCopy[currentBit]) {
                result = binaryAddFull(result, multiplicandCopy);
            }
            multiplicandCopy = binaryShiftLeft(multiplicandCopy, 1);
        }

        return result;
    }

    public static Pair<boolean[], boolean[]> binaryDivide(boolean[] dividend, boolean[] divisor) {
        int operationLength = Math.max(dividend.length, divisor.length);
        boolean[] quotient = new boolean[operationLength]; //Quotient
        boolean[] remainder = new boolean[operationLength]; //Remainder
        boolean[] dividendCopy = new boolean[operationLength]; //Dividend
        boolean[] divisorCopy = new boolean[operationLength]; //Divisor
        System.arraycopy(dividend, 0, dividendCopy, 0, dividend.length);
        System.arraycopy(divisor, 0, divisorCopy, 0, divisor.length);

        for (int currentBit = operationLength - 1; currentBit >= 0; currentBit--) {
            remainder = binaryShiftLeft(remainder, 1);
            remainder[0] = dividendCopy[currentBit];

            if (binaryGreaterOrEquals(remainder, divisorCopy)) {
                remainder = binarySubtract(remainder, divisorCopy);
                quotient[currentBit] = true;
            }
        }

        return new Pair<>(quotient, remainder);
    }

    public static boolean[] binaryShiftLeft(boolean[] toShift, int shift) {
        if (shift <= 0)
            return toShift;

        boolean[] buffer = new boolean[toShift.length];
        System.arraycopy(toShift, 0, buffer, shift, toShift.length - shift);
        return buffer;
    }

    public static boolean[] binaryShiftRight(boolean[] toShift, int shift) {
        if (shift <= 0)
            return toShift;

        boolean[] buffer = new boolean[toShift.length];
        System.arraycopy(toShift, shift, buffer, 0, toShift.length - shift);
        return buffer;
    }

    public static int magnitudeFromBinary(boolean[] binary) {
        int res = 0;
        for (int currentBit = 0; currentBit < binary.length; currentBit++) {
            if (binary[currentBit]) {
                res += Math.pow(2, currentBit);
            }
        }
        return res;
    }

    public static boolean[] magnitudeToBinary(Integer integer, Integer size) {
        integer = Math.abs(integer);
        boolean[] out = new boolean[size];
        for (int currentBit = 0; currentBit < size; currentBit++) {
            out[currentBit] = (integer & (1 << currentBit)) != 0;
        }
        return out;
    }

    public static int mostSignificantOne(boolean[] container) {
        for (int currentBit = container.length - 1; currentBit >= 0; currentBit--) {
            if (container[currentBit]){
                return currentBit;
            }
        }

        return -1;
    }

    public static int leastSignificantOne(boolean[] container) {
        for (int currentBit = 0; currentBit < container.length; currentBit++) {
            if (container[currentBit]) {
                return currentBit;
            }
        }
        return -1;
    }

    public static boolean equals(boolean[] first, boolean[] second) {
        return binaryGreaterOrEquals(first, second) && binaryLessOrEquals(first, second);
    }

    public static boolean binaryGreaterOrEquals(boolean[] first, boolean[] second) {
        int operationLength = Math.max(first.length, second.length);
        boolean[] firstCorrected = new boolean[operationLength];
        boolean[] secondCorrected = new boolean[operationLength];
        System.arraycopy(first, 0, firstCorrected, 0, first.length);
        System.arraycopy(second, 0, secondCorrected, 0, second.length);
        for (int currentBit = operationLength - 1; currentBit >= 0; currentBit--) {
            if (firstCorrected[currentBit] != secondCorrected[currentBit])
                return firstCorrected[currentBit];
        }
        return true;
    }

    public static boolean binaryLessOrEquals(boolean[] first, boolean[] second) {
        int opLength = Math.max(first.length, second.length);
        boolean[] firstCorrected = new boolean[opLength];
        boolean[] secondCorrected = new boolean[opLength];
        System.arraycopy(first, 0, firstCorrected, 0, first.length);
        System.arraycopy(second, 0, secondCorrected, 0, second.length);
        for (int currentBit = opLength - 1; currentBit >= 0; currentBit--) {
            if (firstCorrected[currentBit] != secondCorrected[currentBit])
                return !firstCorrected[currentBit];
        }
        return true;
    }
}
