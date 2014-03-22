package org.qedeq.mathparser;

import java.math.BigInteger;

/**
 * Represents a fraction with arbitrary precision.
 */
public class Fraction {
	
	/** Has value 1 if fraction is positive or zero and -1 otherwise. */
	private int sign;
	
	/** Numerator of fraction */
	private BigInteger numerator;
	
	/** Denominator of fraction */
	private BigInteger denominator;

	/**
	 * Constructs a fraction equivalent to 0.
	 */
	public Fraction() {
		this.sign = 1;
		this.numerator = BigInteger.ZERO;
		this.denominator = BigInteger.ONE;
	}
	
	/**
	 * Constructs a fraction equivalent to given value.
	 *
	 * @param	value	Value.
	 */
	public Fraction(final long value) {
		this.sign = 1;
		this.numerator = BigInteger.valueOf(value);
		this.denominator = BigInteger.ONE;
		normalize();
	}

	/**
	 * Constructs a fraction from given numerator.
	 * If the denominator is 0 a RuntimeException is thrown.
	 *
	 * @param	numerator	Numerator.
	 * @param 	denominator	Denominator, must not be 0.
	 */
	public Fraction(final long numerator, final long denominator) {
		this.sign = 1;
		this.numerator = BigInteger.valueOf(numerator);
		this.denominator = BigInteger.valueOf(denominator);
		normalize();
	}

	/**
	 * Constructs a fraction equivalent to given value. Translates the decimal String
	 * representation of an integer into a fraction. The String representation consists of an
	 * optional minus sign followed by a sequence of one or more decimal digits. The
     * character-to-digit mapping is provided by {@code Character.digit}. The String may not
     * contain any extraneous characters (whitespace, for example).
     * If the value string has an invalid format a RuntimeException is thrown.
	 *
	 * @param	value	Value.
	 */
	public Fraction(final String value) {
		this.sign = 1;
		this.numerator = new BigInteger(value);
		this.denominator = BigInteger.ONE;
		normalize();
	}

	/**
	 * Add given fraction to this instance.
	 * 
	 * @param	f	Add this fraction.
	 * @return	Reference to this instance. Contains the result.
	 */
	public Fraction plus(final Fraction f) {
		final BigInteger gcd = this.denominator.gcd(f.denominator);
		final BigInteger reduce1 = f.denominator.divide(gcd);
		final BigInteger reduce2 = this.denominator.divide(gcd);
		this.numerator = this.numerator.multiply(reduce1);
		if (this.sign == f.sign) {
			this.numerator = this.numerator.add(f.numerator.multiply(reduce2));
		} else {
			this.numerator = this.numerator.subtract(f.numerator.multiply(reduce2));
		}
		this.denominator = this.denominator.multiply(reduce1);
		normalize();
		return this;
	}
	
	/**
	 * Subtract given fraction to this instance.
	 * 
	 * @param	f	Subtract this fraction.
	 * @return	Reference to this instance. Contains the result.
	 */
	public Fraction subtract(final Fraction f) {
		final BigInteger gcd = this.denominator.gcd(f.denominator);
		final BigInteger reduce1 = f.denominator.divide(gcd);
		final BigInteger reduce2 = this.denominator.divide(gcd);
		this.numerator = this.numerator.multiply(reduce1);
		if (this.sign == f.sign) {
			this.numerator = this.numerator.subtract(f.numerator.multiply(reduce2));
		} else {
			this.numerator = this.numerator.add(f.numerator.multiply(reduce2));
		}
		this.denominator = this.denominator.multiply(reduce1);
		normalize();
		return this;
	}
	
	/**
	 * Multiply given fraction to this instance.
	 * 
	 * @param	f	Multiply this fraction.
	 * @return	Reference to this instance. Contains the result.
	 */
	public Fraction multiply(final Fraction f) {
		this.numerator = this.numerator.multiply(f.numerator);
		this.denominator = this.denominator.multiply(f.denominator);
		this.sign = this.sign * f.sign;
		normalize();
		return this;
	}

	/**
	 * Divide by given fraction.
	 * Throws RuntimeException if a division by zero occurs.
	 * 
	 * @param	f	Divide by this fraction.
	 * @return	Reference to this instance. Contains the result.
	 */
	public Fraction divide(final Fraction f) {
		this.numerator = this.numerator.multiply(f.denominator);
		this.denominator = this.denominator.multiply(f.numerator);
		this.sign = this.sign * f.sign;
		normalize();
		return this;
	}

	/**
	 * Normalize fraction representation.
	 */
	private void normalize() {
		int s = this.denominator.signum();
		if (s == 0) {
			throw new NumberFormatException("Division by Zero");
		}
		if (s < 0) {
			this.sign = -this.sign;
			this.denominator = this.denominator.negate();
		}
		if (this.numerator.signum() < 0) {
			this.sign = -this.sign;
			this.numerator = this.numerator.negate();
		}
		if (BigInteger.ZERO.equals(this.numerator)) {
			this.sign = 1;
			this.denominator = BigInteger.ONE;
		}
		final BigInteger gcd = this.denominator.gcd(numerator);
		if (BigInteger.ONE.compareTo(gcd) < 0) {
			this.numerator = this.numerator.divide(gcd);
			this.denominator = this.denominator.divide(gcd);
		}
	}

	/**
	 * Return negation of this fraction. Changes this instance.
	 *
	 * @return	Negation of this fraction.
	 */
	public Fraction negate() {
		this.sign = -this.sign;
		return this;
	}

	public String toString() {
		return (sign < 0 ? "-" : "" ) + numerator
			+ (BigInteger.ONE.equals(denominator) ? "" : "/" + denominator);
	}

	public double getDouble() {
		return sign * numerator.doubleValue() / denominator.doubleValue();
	}


}