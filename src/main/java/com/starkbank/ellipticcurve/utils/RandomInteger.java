package com.starkbank.ellipticcurve.utils;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;


public class RandomInteger {

    /**
     *
     * @param start
     * @param end
     * @return
     */
    public static BigInteger between(BigInteger start, BigInteger end) {
        Random random = new SecureRandom();
        return new BigInteger(end.toByteArray().length * 8 - 1, random).abs().add(start);
    }
}
