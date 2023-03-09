package dev.jackraidenph.binarycalculator.binarycontainer;

import java.util.Arrays;

public abstract class BinaryContainer<N extends Number, T extends BinaryContainer<N, T>> {
    protected final boolean[] container;

    BinaryContainer(int size) {
        container = new boolean[size];
    }

    public BinaryContainer(T toCopy) {
        container = new boolean[toCopy.container.length];
        System.arraycopy(toCopy.container, 0, container, 0, toCopy.container.length);
    }

    public BinaryContainer() {
        container = new boolean[0];
    }

    public boolean[] get() {
        return Arrays.copyOf(container, container.length);
    }

    public T copyFrom(boolean[] copyFrom) {
        int copyLength = Math.min(container.length, copyFrom.length);
        System.arraycopy(copyFrom, 0, container, 0, copyLength);
        return (T) this;
    }

    public N getDecimal(){return null;}

    public T add(T toAdd){return (T) this;}

    public T subtract(T toSubtract){return (T) this;}

    public T multiply(T toMultiplyBy){return (T) this;}

    public T divideBy(T toDivideBy){return (T) this;}

    public int compare(T another){return 0;}

    public int getSign() {return 0;}
    protected T setSign(boolean negative) {return (T) this;}

    @Override
    public String toString() {
        return new StringBuilder(Arrays.toString(container)
                .replaceAll("false", "0")
                .replaceAll("true", "1")
                .replaceAll("[^01]*", "")
                .replaceAll("(.{4})", "$1 "))
                .reverse().toString().trim();
    }
}
