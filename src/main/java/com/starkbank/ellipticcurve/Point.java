package com.starkbank.ellipticcurve;
import java.math.BigInteger;


public class Point {

    public BigInteger x;
    public BigInteger y;
    public BigInteger z;

    /**
     *
     * @param x
     * @param y
     */
    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
        this.z = BigInteger.ZERO;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     */
    public Point(BigInteger x, BigInteger y, BigInteger z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
