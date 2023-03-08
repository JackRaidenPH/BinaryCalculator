package dev.jackraidenph.binarycalculator.utility;

import javafx.util.Pair;

public class BinaryArrayUtils {
    public static Pair<boolean[], Boolean> binaryAddGetCarry(boolean[] addTo, boolean[] toAdd) {
        int opLength = Math.max(addTo.length, toAdd.length);
        boolean v1, v2, v3, C = false;
        boolean[] X = new boolean[opLength];
        boolean[] Y = new boolean[opLength];
        System.arraycopy(addTo, 0, X, 0, addTo.length);
        System.arraycopy(toAdd, 0, Y, 0, toAdd.length);
        for (int i = 0; i < opLength; i++) {
            v1 = X[i] ^ Y[i];
            v2 = X[i] & Y[i];
            v3 = v1 & C;
            X[i] = v1 ^ C;
            C = v2 | v3;
        }
        return new Pair<>(X, C);
    }

    public static boolean[] binaryAdd(boolean[] addTo, boolean[] toAdd) {
        return binaryAddGetCarry(addTo, toAdd).getKey();
    }

    public static boolean[] binaryAddFull(boolean[] addTo, boolean[] toAdd) {
        Pair<boolean[], Boolean> pair = binaryAddGetCarry(addTo, toAdd);
        boolean[] res = pair.getKey();
        if(pair.getValue())
            res = binaryAddFull(res, new boolean[]{true});
        return res;
    }

    public static Pair<boolean[], Boolean> incrementGetCarry(boolean[] addTo) {
        boolean[] inc = new boolean[addTo.length];
        inc[0] = true;
        return binaryAddGetCarry(addTo, inc);
    }

    public static boolean[] increment(boolean[] addTo) {
        return incrementGetCarry(addTo).getKey();
    }

    public static Pair<boolean[], Boolean> decrementGetBorrow(boolean[] subtractFrom) {
        boolean[] inc = new boolean[subtractFrom.length];
        inc[0] = true;
        return binarySubtractGetBorrow(subtractFrom, inc);
    }

    public static boolean[] decrement(boolean[] subtractFrom) {
        return decrementGetBorrow(subtractFrom).getKey();
    }

    public static Pair<boolean[], Boolean> binarySubtractGetBorrow(boolean[] subtractFrom, boolean[] toSubtract) {
        int opLength = Math.max(subtractFrom.length, toSubtract.length);
        boolean v1, v2, v3, B = false;
        boolean[] X = new boolean[opLength];
        boolean[] Y = new boolean[opLength];
        System.arraycopy(subtractFrom, 0, X, 0, subtractFrom.length);
        System.arraycopy(toSubtract, 0, Y, 0, toSubtract.length);
        for (int i = 0; i < opLength; i++) {
            v1 = X[i] ^ Y[i];
            v2 = !X[i] & Y[i];
            v3 = !v1 & B;
            X[i] = v1 ^ B;
            B = v2 | v3;
        }
        return new Pair<>(X, B);
    }

    public static boolean[] binarySubtract(boolean[] subtractFrom, boolean[] toSubtract) {
        return binarySubtractGetBorrow(subtractFrom, toSubtract).getKey();
    }

    public static boolean[] binaryMultiply(boolean[] multiple, boolean[] multiplyBy) {
        int opLength = Math.max(multiple.length, multiplyBy.length);
        boolean[] X = new boolean[opLength * 2]; //Multiplicand
        boolean[] Y = new boolean[opLength]; //Multiplier
        System.arraycopy(multiple, 0, X, 0, multiple.length);
        System.arraycopy(multiplyBy, 0, Y, 0, multiplyBy.length);
        boolean[] O = new boolean[opLength * 2]; //Result

        for (int i = 0; i < opLength; i++) {
            //O = binaryShiftRight(O, 1);
            if (Y[i])
                O = binaryAddFull(O, X);
            X = binaryShiftLeft(X, 1);
        }

        return O;
    }

    public static Pair<boolean[], boolean[]> binaryDivide(boolean[] dividend, boolean[] divisor) {
        int opLength = Math.max(dividend.length, divisor.length);
        boolean[] Q = new boolean[opLength]; //Quotient
        boolean[] R = new boolean[opLength]; //Remainder
        boolean[] N = new boolean[opLength]; //Dividend
        boolean[] D = new boolean[opLength]; //Divisor
        System.arraycopy(dividend, 0, N, 0, dividend.length);
        System.arraycopy(divisor, 0, D, 0, divisor.length);

        for (int i = opLength - 1; i >= 0; i--) {
            R = binaryShiftLeft(R, 1);
            R[0] = N[i];

            if (binaryGreaterOrEquals(R, D)) {
                R = binarySubtract(R, D);
                Q[i] = true;
            }
        }

        return new Pair<>(Q, R);
    }

    public static boolean[] binaryShiftLeft(boolean[] toShift, int shift) {
        if(shift <= 0)
            return  toShift;

        boolean[] buffer = new boolean[toShift.length];
        System.arraycopy(toShift, 0, buffer, shift, toShift.length - shift);
        return buffer;
    }

    public static boolean[] binaryShiftRight(boolean[] toShift, int shift) {
        if(shift <= 0)
            return  toShift;

        boolean[] buffer = new boolean[toShift.length];
        System.arraycopy(toShift, shift, buffer, 0, toShift.length - shift);
        return buffer;
    }

    public static int magnitudeFromBinary(boolean[] binary) {
        int res = 0;
        for (int i = 0; i < binary.length; i++)
            if (binary[i])
                res += Math.pow(2, i);
        return res;
    }

    public static boolean[] magnitudeToBinary(Integer integer, Integer size) {
        integer = Math.abs(integer);
        boolean[] out = new boolean[size];
        for (int i = 0; i < size; i++)
            out[i] = (integer & (1 << i)) != 0;
        return out;
    }

    public static int mostSignificantOne(boolean[] container) {
        for (int i = container.length - 1; i >= 0; i--)
            if (container[i])
                return i;
        return -1;
    }

    public static int leastSignificantOne(boolean[] container) {
        for (int i = 0; i < container.length; i++)
            if (container[i])
                return i;
        return -1;
    }

    public static boolean equals(boolean[] first, boolean[] second) {
        return binaryGreaterOrEquals(first, second) && binaryLessOrEquals(first, second);
    }

    public static boolean binaryGreaterOrEquals(boolean[] first, boolean[] second) {
        int opLength = Math.max(first.length, second.length);
        boolean[] F = new boolean[opLength];
        boolean[] S = new boolean[opLength];
        System.arraycopy(first, 0, F, 0, first.length);
        System.arraycopy(second, 0, S, 0, second.length);
        for (int i = opLength - 1; i >= 0; i--) {
            if (F[i] != S[i])
                return F[i];
        }
        return true;
    }

    public static boolean binaryLessOrEquals(boolean[] first, boolean[] second) {
        int opLength = Math.max(first.length, second.length);
        boolean[] F = new boolean[opLength];
        boolean[] S = new boolean[opLength];
        System.arraycopy(first, 0, F, 0, first.length);
        System.arraycopy(second, 0, S, 0, second.length);
        for (int i = opLength - 1; i >= 0; i--) {
            if (F[i] != S[i])
                return !F[i];
        }
        return true;
    }
}
