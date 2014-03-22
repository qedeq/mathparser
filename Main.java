package mparser;

public class Main {

	
	private static String text = "";
	private static int position = 0;
	private static boolean debug = false;
	private static int counter;
	
	final private static void d(final String message) {
		if (debug) {
			System.out.println(message);
		}
	}

	public final static void main(final String[] args) throws Exception {
/*		
		System.out.println("0=" + new Fraction(-1, 1).subtract(new Fraction(-1, 1)));
		System.out.println("0=" + new Fraction(1, 1).subtract(new Fraction(1, 1)));
		System.out.println("-2=" + new Fraction(-1, 1).subtract(new Fraction(1, 1)));
		System.out.println("2=" + new Fraction(1, 1).subtract(new Fraction(-1, 1)));
		System.out.println("2=" + new Fraction(5, 5).subtract(new Fraction(8, -8)));
		System.out.println("1=" + new Fraction(-175, -175));
		System.out.println("-1=" + new Fraction(175, -175));
		System.out.println("-13=" + new Fraction(-1001, 77));
		System.out.println("result:   -3=" + readTerm("10 + 2 - (7 +8)"));
		System.out.println("result: 9/28=" + readTerm("-1/4 + 8/7 / 2"));
		System.out.println("result: -401/1995=" + readTerm("-1/6 * 4/5 + 9/19 * (-1/7)"));
		System.out.println("result: -13/4=" + readTerm("-2+3/4*(-1/3)+4*(-1/4)"));
		System.out.println("result: -17/2=" + readTerm("-2*(3-3/4*(-5/3))"));
		System.out.println("result: -128/45=" + readTerm("-3-1/3*((-4/5)-1/2*(-2/3))"));
		System.out.println(readTerm("-3-1/3*((-4/5)-1/2*(-2/3))").getDouble());
*/
		test("-3-1/3*((-4/5)-1/2*(-2/3))", "-128/45", true);
		test("-3-1/3*((-4/5)-1/2*((-2/3))", "closing ) is missing", false);
		test("-3-1/3*((-4/5)))-1/2*((-2/3))", "end expected", false);
		test("-3-1/3*((-4/5)-1.1/2*((-2/3))", "this is no allowed token: .", false);
		test("-3-1/3*((-4/5)-1 , 1/2*((-2/3))", "this is no allowed token: ,", false);
		test("----1", "1", true);
		test("-1/2*3/6", "-1/4", true);
		test("-1/-2*3/6", "1/4", true);
		test("-1/--2*3/6", "-1/4", true);
		return;
	}

	private static void test(final String text, final String result, final boolean ok) throws Exception {
		System.out.println(++counter + ".");
		MathParser parser = new MathParser();
		if (ok) {
			Fraction r = parser.readTerm(text);
			System.out.print("  " + result + "= " + text);
			System.out.println(" ?= " + r + "    (=" + r.getDouble() + ")");
			if (!r.toString().equals(result)) {
				throw new Exception("unexpected result");
			}
		} else {
			try {
				Fraction r = parser.readTerm(text);
				System.out.print("  ");
				parser.printPosition();
				throw new Exception("Exception expected, but we have result " + r);
			} catch (RuntimeException e) {
				System.out.println("  "+ text);
				System.out.print("  ");
				parser.printPosition();
				System.out.println("  " + result + " ?= " + e.getMessage());
				if (!e.getMessage().equals(result)) {
					throw new Exception("unexpected result");
				}
			}
		}
		System.out.println();
	}
	
	final static boolean eot() {
		return position >= text.length();
	}

	final private static String readToken(boolean modify) {
		String result = "";
		int i = position;
		// skip whitespace
		while (i < text.length()) {
			if (Character.isWhitespace(text.charAt(i))) {
				i++;
			} else {
				break;
			}
		}
		if (i < text.length()) {
			if (Character.isDigit(text.charAt(i))) {
				while (i < text.length()) {
					if (Character.isDigit(text.charAt(i))) {
						result += text.charAt(i);
						i++;
					} else {
						break;
					}
				}
			} else {	// no digit string
				result += text.charAt(i);
				i++;
			}
		}
		if (modify) {
			position = i;
		}
		d((modify ? "read" : " get") + "token: " + result.trim() + " pos: " + position);
		return result.trim();
	}

	final static String getToken() {
		return readToken(false);
	}

	final static String readToken() {
		return readToken(true);
	}
	
/*	
	final static int parse_expression () {
	    return parse_expression_1 (getToken(), 0);
	}
	
	final static int parse_expression_1 (String lhs, int min_precedence) {
//	    while the next token is a binary operator whose precedence is >= min_precedence
		while (isBinaryOpWithGreaterPrecedence(getToken(), min_precedence)) {
	        String op = readToken();
	        rhs := parse_primary ()
//	        while the next token is a binary operator whose precedence is greater
//             than op's, or a right-associative operator
//             whose precedence is equal to op's
	        while (!isBinaryOpWithGreaterPrecedence(getToken(), min_precedence)) {
		        String op = readToken();
		       
	        while the next token is a binary operator whose precedence is greater
	                 than op's, or a right-associative operator
	                 whose precedence is equal to op's
	            lookahead := next token
	            rhs := parse_expression_1 (rhs, lookahead's precedence)
	        lhs := the result of applying op with operands lhs and rhs
	    return lhs;
	}
*/
		
        /**
         * Reads (maximal possible) Term from input.
         *
         * @return  Read term.
         * @throws  ParserException Parsing failed.
         */
        public final static Fraction readTerm(final String t) throws Exception {
        	text = t;
        	position = 0;
            final Fraction term = readMaximalTerm(0);
            if (eot()) {
                readToken();
            }
            return term;
        }


        /**
         * Reads next "maximal" term from input. The following input doesn't make the the term more
         * complete. Respects the priority of coming operators by comparing it
         * to the given value.
         *
         * @param priority  next formula will be the last, if
         *                  connecting operator has higher priority
         * @return  Read term.
         * @throws  ParserException Parsing failed.
         */
        private final static Fraction readMaximalTerm(final int priority) throws Exception {
        	d(">readMaximalTerm");
            Fraction term = new Fraction();
            if (eot()) {
                return term;
            }
            term = readPrefixTerm();
            term = addNextInfixTerms(priority, term);
        	d("<readMaximalTerm: " + term);
            return term;
        }


        private final static Fraction addNextInfixTerms(final int priority, final Fraction initialTerm)
                throws Exception {
        	d(">addNextInfixTerms");
            Fraction term = initialTerm;
            String newOperator = "";
            String oldOperator = "";

            do {
                newOperator = getOperator();  // we expect an unique infix operator
                d("   newOperator=" + newOperator);
                if (newOperator.length() == 0 || getPriority(newOperator) <= priority) {
                    //  no newOperator or of less priority");
                	d("<addNextInfixTerms: " + term + " (no newOperator or of less priority)");
                    return term;
                }
                if (!isInfixOperator(newOperator)) {
                	// newOperator is not infix
                    // TODO mime 20060313: try to read further arguments
                	d("<addNextInfixTerms: " + term + " (newOperator is not infix)");
                    return term;
                }
                readToken();	// == newOperator
                Fraction term2 = readMaximalTerm(getPriority(newOperator));
                // old term is first argument of new operator
                term = applyInfixOperator(newOperator, term, term2);
                oldOperator = newOperator;
            } while (true);
        }

        /**
         * Read next operator from input.
         *
         * @return  Found operator, maybe <code>""</code>.
         */
        private final static String getOperator() {
            final String token;
            token = getToken();
            if (isOperator(token)) {
            	return token;
            }
            return ""; 	// no operator found
        }

        
        
    /**
     * Try to parse an prefix operator and its operands from the input. Tries first operator,
     * second operator and so on. If the last one fails an appropriate exception is thrown.
     *
     * @param   operators   Prefix operator list.
     * @return  Resulting term.
     * @throws  ParserException Parsing failed.
     */
    private final static Fraction readPrefixOperator(final String operator) throws Exception {
    	d(">readPrefixOperator");
        Fraction term = new Fraction();
        if (!isPrefixOperator(operator)) {
            throw new Exception("is no prefix operator: " + operator);
        }
        readToken();	// read operator
        term = readMaximalTerm(getPriority(operator));
        term = applyPrefixOperator(operator, term);
    	d("<readPrefixOperator: " + term);
        return term;
    }

    /**
     * Read next following term. This is a complete term but some infix operator
     * or some terms for an infix operator might follow.
     *
     * @return  Read term.
     * @throws  ParserException Parsing failed.
     */
    private final static Fraction readPrefixTerm() throws Exception {
    	d(">readPrefixTerm");
        Fraction term = new Fraction();
        final String operator = getOperator();   // there might be several prefix operators
        if (operator.length() > 0) {
            // operators found
            term = readPrefixOperator(operator);
        } else { // no operator found
            final String token;
            token = getToken();
            if (token.length() == 0) {
                return term;
            }
            if ("(".equals(token)) {
                readToken();
                // start bracket found:  token
                term = readMaximalTerm(0);
                final String lastToken = readToken();
                if (!")".equals(lastToken)) {
                    throw new Exception("closing ) is missing");
                }

            } else {
                readToken();
                // atom: token
                term = new Fraction(token);
            }
        }
    	d("<readPrefixTerm: " + term);
        return term;
    }

    private final static boolean isPrefixOperator(final String operator) {
    	return "+".equals(operator) || "-".equals(operator);
    }

    private final static boolean isInfixOperator(final String operator) {
    	return "+".equals(operator) || "-".equals(operator) || "*".equals(operator) || "/".equals(operator);
    }

    private final static boolean isOperator(final String operator) {
    	return "+".equals(operator) || "-".equals(operator) || "*".equals(operator) || "/".equals(operator);
    }

    private final static Fraction applyPrefixOperator(final String operator, final Fraction term) throws Exception {
    	if ("+".equals(operator)) {
    		return term;
    	} else if ("-".equals(operator)) {
    		return term.negate();
    	} else {
    		throw new Exception("this is no prefix operator: " + operator);
    	}
    }
    
    private final static Fraction applyInfixOperator(final String operator, final Fraction term1, final Fraction term2) throws Exception {
    	if ("+".equals(operator)) {
    		return term1.plus(term2);
    	} else if ("-".equals(operator)) {
    		return term1.subtract(term2);
    	} else if ("*".equals(operator)) {
    		return term1.multiply(term2);
    	} else if ("/".equals(operator)) {
    		return term1.divide(term2);
    	} else {
    		throw new Exception("this is no infix operator: " + operator);
    	}
    }
    
    private final static int getPriority(final String operator) throws Exception {
    	if ("+".equals(operator) || "-".equals(operator)) {
    		return 1;
    	} else if ("*".equals(operator) || "/".equals(operator)) {
    		return 2;
    	} else if ("(".equals(operator) || ")".equals(operator)) {
    		return 3;
    	} else {
    		throw new Exception("this an unknown operator: " + operator);
    	}
    }
    
    
}
