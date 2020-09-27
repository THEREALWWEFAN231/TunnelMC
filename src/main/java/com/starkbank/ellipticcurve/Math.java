package com.starkbank.ellipticcurve;
import java.math.BigInteger;


public final class Math {

    /**
     * Fast way to multiply point and scalar in elliptic curves
     *
     * @param p First Point to multiply
     * @param n Scalar to multiply
     * @param N Order of the elliptic curve
     * @param P Prime number in the module of the equation Y^2 = X^3 + A*X + B (mod P)
     * @param A Coefficient of the first-order term of the equation Y^2 = X^3 + A*X + B (mod P)
     * @return Point that represents the sum of First and Second Point
     */
    public static Point multiply(Point p, BigInteger n, BigInteger N, BigInteger A, BigInteger P) {
        return fromJacobian(jacobianMultiply(toJacobian(p), n, N, A, P), P);
    }

    /**
     * Fast way to add two points in elliptic curves
     *
     * @param p First Point you want to add
     * @param q Second Point you want to add
     * @param A Coefficient of the first-order term of the equation Y^2 = X^3 + A*X + B (mod P)
     * @param P Prime number in the module of the equation Y^2 = X^3 + A*X + B (mod P)
     * @return Point that represents the sum of First and Second Point
     */

    public static Point add(Point p, Point q, BigInteger A, BigInteger P) {
        return fromJacobian(jacobianAdd(toJacobian(p), toJacobian(q), A, P), P);
    }

    /**
     * Extended Euclidean Algorithm. It's the 'division' in elliptic curves
     *
     * @param x Divisor
     * @param n Mod for division
     * @return Value representing the division
     */
    public static BigInteger inv(BigInteger x, BigInteger n) {
        if (x.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ZERO;
        }
        BigInteger lm = BigInteger.ONE;
        BigInteger hm = BigInteger.ZERO;
        BigInteger high = n;
        BigInteger low = x.mod(n);
        BigInteger r, nm, nw;
        while (low.compareTo(BigInteger.ONE) > 0) {
            r = high.divide(low);
            nm = hm.subtract(lm.multiply(r));
            nw = high.subtract(low.multiply(r));
            high = low;
            hm = lm;
            low = nw;
            lm = nm;
        }
        return lm.mod(n);
    }

    /**
     * Convert point to Jacobian coordinates
     *
     * @param p the point you want to transform
     * @return Point in Jacobian coordinates
     */
    public static Point toJacobian(Point p) {

        return new Point(p.x, p.y, BigInteger.ONE);
    }

    /**
     * Convert point back from Jacobian coordinates
     *
     * @param p the point you want to transform
     * @param P Prime number in the module of the equation Y^2 = X^3 + A*X + B (mod P)
     * @return Point in default coordinates
     */
    public static Point fromJacobian(Point p, BigInteger P) {
        BigInteger z = inv(p.z, P);
        BigInteger x = p.x.multiply(z.pow(2)).mod(P);
        BigInteger y = p.y.multiply(z.pow(3)).mod(P);
        return new Point(x, y, BigInteger.ZERO);
    }

    /**
     * Double a point in elliptic curves
     *
     * @param p the point you want to transform
     * @param A Coefficient of the first-order term of the equation Y^2 = X^3 + A*X + B (mod P)
     * @param P Prime number in the module of the equation Y^2 = X^3 + A*X + B (mod P)
     * @return the result point doubled in elliptic curves
     */
    public static Point jacobianDouble(Point p, BigInteger A, BigInteger P) {
        if (p.y == null || p.y.compareTo(BigInteger.ZERO) == 0) {
            return new Point(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        }
        BigInteger ysq = p.y.pow(2).mod(P);
        BigInteger S = BigInteger.valueOf(4).multiply(p.x).multiply(ysq).mod(P);
        BigInteger M = BigInteger.valueOf(3).multiply(p.x.pow(2)).add(A.multiply(p.z.pow(4))).mod(P);
        BigInteger nx = M.pow(2).subtract(BigInteger.valueOf(2).multiply(S)).mod(P);
        BigInteger ny = M.multiply(S.subtract(nx)).subtract(BigInteger.valueOf(8).multiply(ysq.pow(2))).mod(P);
        BigInteger nz = BigInteger.valueOf(2).multiply(p.y).multiply(p.z).mod(P);
        return new Point(nx, ny, nz);
    }

    /**
     * Add two points in elliptic curves
     *
     * @param p First Point you want to add
     * @param q Second Point you want to add
     * @param A Coefficient of the first-order term of the equation Y^2 = X^3 + A*X + B (mod P)
     * @param P Prime number in the module of the equation Y^2 = X^3 + A*X + B (mod P)
     * @return Point that represents the sum of First and Second Point
     */
    public static Point jacobianAdd(Point p, Point q, BigInteger A, BigInteger P) {
        if (p.y == null || p.y.compareTo(BigInteger.ZERO) == 0) {
            return q;
        }
        if (q.y == null || q.y.compareTo(BigInteger.ZERO) == 0) {
            return p;
        }
        BigInteger U1 = p.x.multiply(q.z.pow(2)).mod(P);
        BigInteger U2 = q.x.multiply(p.z.pow(2)).mod(P);
        BigInteger S1 = p.y.multiply(q.z.pow(3)).mod(P);
        BigInteger S2 = q.y.multiply(p.z.pow(3)).mod(P);
        if (U1.compareTo(U2) == 0) {
            if (S1.compareTo(S2) != 0) {
                return new Point(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE);
            }
            return jacobianDouble(p, A, P);
        }
        BigInteger H = U2.subtract(U1);
        BigInteger R = S2.subtract(S1);
        BigInteger H2 = H.multiply(H).mod(P);
        BigInteger H3 = H.multiply(H2).mod(P);
        BigInteger U1H2 = U1.multiply(H2).mod(P);
        BigInteger nx = R.pow(2).subtract(H3).subtract(BigInteger.valueOf(2).multiply(U1H2)).mod(P);
        BigInteger ny = R.multiply(U1H2.subtract(nx)).subtract(S1.multiply(H3)).mod(P);
        BigInteger nz = H.multiply(p.z).multiply(q.z).mod(P);
        return new Point(nx, ny, nz);
    }

    /**
     * Multiply point and scalar in elliptic curves
     *
     * @param p First Point to multiply
     * @param n Scalar to multiply
     * @param N Order of the elliptic curve
     * @param A Coefficient of the first-order term of the equation Y^2 = X^3 + A*X + B (mod P)
     * @param P Prime number in the module of the equation Y^2 = X^3 + A*X + B (mod P)
     * @return Point that represents the product of First Point and scalar
     */
    public static Point jacobianMultiply(Point p, BigInteger n, BigInteger N, BigInteger A, BigInteger P) {
        if (BigInteger.ZERO.compareTo(p.y) == 0 || BigInteger.ZERO.compareTo(n) == 0) {
            return new Point(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE);
        }
        if (BigInteger.ONE.compareTo(n) == 0) {
            return p;
        }
        if (n.compareTo(BigInteger.ZERO) < 0 || n.compareTo(N) >= 0) {
            return jacobianMultiply(p, n.mod(N), N, A, P);
        }
        if (n.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
            return jacobianDouble(jacobianMultiply(p, n.divide(BigInteger.valueOf(2)), N, A, P), A, P);
        }
        if (n.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ONE) == 0) {
            return jacobianAdd(jacobianDouble(jacobianMultiply(p, n.divide(BigInteger.valueOf(2)), N, A, P), A, P), p, A, P);
        }
        return null;
    }
}
