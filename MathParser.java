package mparser;

public class MathParser {
	
	private String text = "";

	private int position = 0;

	private boolean debug = false;

	
	public MathParser() {
	}
	
	public void printPosition() {
		System.out.println(text.substring(0, position) + ">>>" + text.substring(position));
	}
	
	private void d(final String message) {
		if (debug) {
			System.out.println(message);
		}
	}

	protected boolean kkkeot() {
		return position >= text.length();
	}

	protected String readToken(boolean modify) {
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
		final String t = result.trim();
		d((modify ? "read" : " get") + "token: " + t + " pos: " + position);
		if (!allowedToken(t)) {
			throw new RuntimeException("this is no allowed token: " + t);
		}
		return t;
	}

	protected boolean allowedToken(final String token) {
		return token.length() != 1 || "+-*/()0123456789".indexOf(token) >= 0;
	}

	protected String getToken() {
		return readToken(false);
	}

	protected String readToken() {
		return readToken(true);
	}
	
		
    /**
     * Reads (maximal possible) Term from input.
     *
     * @return  Read term.
     * @throws  ParserException Parsing failed.
     */
    public Fraction readTerm(final String t) {
    	text = t;
    	position = 0;
        final Fraction term = readMaximalTerm(0);
        if (getToken().length() > 0) {
        	throw new RuntimeException("end expected");
        }
//        if (eot()) {
//            readToken();
//        }
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
    protected Fraction readMaximalTerm(final int priority) {
    	d(">readMaximalTerm");
        Fraction term = new Fraction();
//        if (eot()) {
//            return term;
//        }
        term = readPrefixTerm();
        term = addNextInfixTerms(priority, term);
    	d("<readMaximalTerm: " + term);
        return term;
    }


    protected Fraction addNextInfixTerms(final int priority, final Fraction initialTerm) {
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
    protected String getOperator() {
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
    protected Fraction readPrefixOperator(final String operator) {
    	d(">readPrefixOperator");
        Fraction term = new Fraction();
        if (!isPrefixOperator(operator)) {
            throw new RuntimeException("is no prefix operator: " + operator);
        }
        readToken();	// read operator
        term = readMaximalTerm(getPrefixPriority(operator));
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
    protected Fraction readPrefixTerm() {
    	d(">readPrefixTerm");
        Fraction term = new Fraction();
        final String operator = getOperator(); 
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
                final String lastToken = getToken();
                if (!")".equals(lastToken)) {
                    throw new RuntimeException("closing ) is missing");
                }
                readToken();
            } else {
                // atom: token
                term = new Fraction(token);
                readToken();
            }
        }
    	d("<readPrefixTerm: " + term);
        return term;
    }

    protected boolean isPrefixOperator(final String operator) {
    	return "+".equals(operator) || "-".equals(operator);
    }

    protected boolean isInfixOperator(final String operator) {
    	return "+".equals(operator) || "-".equals(operator) || "*".equals(operator) || "/".equals(operator);
    }

    protected boolean isOperator(final String operator) {
    	return "+".equals(operator) || "-".equals(operator) || "*".equals(operator) || "/".equals(operator);
    }

    protected Fraction applyPrefixOperator(final String operator, final Fraction term) {
    	if ("+".equals(operator)) {
    		return term;
    	} else if ("-".equals(operator)) {
    		return term.negate();
    	} else {
    		throw new RuntimeException("this is no prefix operator: " + operator);
    	}
    }
    
    protected Fraction applyInfixOperator(final String operator, final Fraction term1, final Fraction term2) {
    	if ("+".equals(operator)) {
    		return term1.plus(term2);
    	} else if ("-".equals(operator)) {
    		return term1.subtract(term2);
    	} else if ("*".equals(operator)) {
    		return term1.multiply(term2);
    	} else if ("/".equals(operator)) {
    		return term1.divide(term2);
    	} else {
    		throw new RuntimeException("this is no infix operator: " + operator);
    	}
    }
    
    private int getPriority(final String operator) {
    	if ("+".equals(operator) || "-".equals(operator)) {
    		return 1;
    	} else if ("*".equals(operator) || "/".equals(operator)) {
    		return 2;
    	} else if ("(".equals(operator) || ")".equals(operator)) {
    		return 4;
    	} else {
    		throw new RuntimeException("this an unknown operator: " + operator);
    	}
    }
    
    private int getPrefixPriority(final String operator) {
    	if ("+".equals(operator) || "-".equals(operator)) {
    		return 3;
    	} else {
    		throw new RuntimeException("this an unknown prefix operator: " + operator);
    	}
    }
}
