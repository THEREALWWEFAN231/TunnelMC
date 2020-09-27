package com.starkbank.ellipticcurve;
import java.math.BigInteger;
import java.util.*;

/**
 * Elliptic Curve Equation.
 * y^2 = x^3 + A*x + B (mod P)
 *
 */

public class Curve {

    public BigInteger A;
    public BigInteger B;
    public BigInteger P;
    public BigInteger N;
    public Point G;
    public String name;
    public long[] oid;

    /**
     *
     * @param A
     * @param B
     * @param P
     * @param N
     * @param Gx
     * @param Gy
     * @param name
     * @param oid
     */
    public Curve(BigInteger A, BigInteger B, BigInteger P, BigInteger N, BigInteger Gx, BigInteger Gy, String name, long[] oid) {
        this.A = A;
        this.B = B;
        this.P = P;
        this.N = N;
        this.G = new Point(Gx, Gy);
        this.name = name;
        this.oid = oid;
    }

    /**
     * Verify if the point `p` is on the curve
     *
     * @param p Point p = Point(x, y)
     * @return true if point is in the curve otherwise false
     */
    public boolean contains(Point p) {
        return p.y.pow(2).subtract(p.x.pow(3).add(A.multiply(p.x)).add(B)).mod(P).intValue() == 0;
    }

    /**
     *
     * @return
     */
    public int length() {
        return (1 + N.toString(16).length()) / 2;
    }

    /**
     *
     */
    public static final Curve secp256k1 = new Curve(
        BigInteger.ZERO,
        BigInteger.valueOf(7),
        new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16),
        new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16),
        new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16),
        new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16),
        "secp256k1",
        new long[]{1, 3, 132, 0, 10}
    );
    
    //tunnelmc(THEREALWWEFAN231), i got the number information from here, https://asecuritysite.com/encryption/js08
    //oid gotten from https://github.com/nymble/cryptopy/blob/master/ecc/curves.py even though we probably aren't going to use it
    public static final Curve secp256r1 = new Curve(
            new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16),
            new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16),
            new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16),
            new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16),
            new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16),
            new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16),
            "secp256r1",
            new long[]{1, 2, 840, 10045, 3, 1, 7}
        );

    /**
     *
     */
    public static final List supportedCurves = new ArrayList();

    /**
     *
     */
    public static final Map curvesByOid = new HashMap();

    static {
        supportedCurves.add(secp256k1);
        supportedCurves.add(secp256r1);

        for (Object c : supportedCurves) {
            Curve curve = (Curve) c;
            curvesByOid.put(Arrays.hashCode(curve.oid), curve);
        }
    }
}
