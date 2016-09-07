package il.org.spartan.spartanizer.engine;

import static il.org.spartan.Utils.*;
import static il.org.spartan.spartanizer.ast.step.*;
import static il.org.spartan.spartanizer.engine.type.Odd.Types.*;
import static il.org.spartan.spartanizer.engine.type.Primitive.Certain.*;
import static il.org.spartan.spartanizer.engine.type.Primitive.Uncertain.*;
import static org.eclipse.jdt.core.dom.ASTNode.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.*;
import il.org.spartan.spartanizer.ast.*;
import il.org.spartan.spartanizer.utils.*;

/** @author Yossi Gil
 ** @author Dor Maayan
 * @author Niv Shalmon
 * @since 2016 */
public interface type {
  static inner.implementation baptize(final String name) {
    return baptize(name, "anonymously born");
  }

  static inner.implementation baptize(final String name, final String description) {
    return have(name) ? bring(name) : new inner.implementation() {
      @Override public String description() {
        return description;
      }

      @Override public String key() {
        return name;
      }
    }.join();
  }

  @SuppressWarnings("synthetic-access") static inner.implementation bring(final String name) {
    return inner.types.get(name);
  }

  // TODO: Matteo. Nano-pattern of values: not implemented
  @SuppressWarnings("synthetic-access") static type get(final Expression ¢) {
    return inner.setType(¢, inner.lookUp(¢, inner.lookDown(¢)));
  }

  @SuppressWarnings("synthetic-access") static boolean have(final String name) {
    return inner.types.containsKey(name);
  }

  default Primitive.Certain asPrimitiveCertain() {
    return null;
  }

  default type.Primitive.Uncertain asPrimitiveUncertain() {
    return null;
  }

  default boolean canB(@SuppressWarnings("unused") final Primitive.Certain __) {
    return false;
  }

  String description();

  default String fullName() {
    return this + "=" + key() + " (" + description() + ")";
  }

  /** @return true if one of {@link #INT} , {@link #LONG} , {@link #CHAR} ,
   *         {@link BYTE} , {@link SHORT} , {@link FLOAT} , {@link #DOUBLE} ,
   *         {@link #INTEGRAL} or {@link #NUMERIC} , {@link #STRING} ,
   *         {@link #ALPHANUMERIC} or false otherwise */
  default boolean isAlphaNumeric() {
    return in(this, INT, LONG, CHAR, BYTE, SHORT, FLOAT, DOUBLE, INTEGRAL, NUMERIC, STRING, ALPHANUMERIC);
  }

  /** @return true if either a Primitive.Certain, Primitive.Odd.NULL or a
   *         baptized type */
  default boolean isCertain() {
    return this == NULL || have(key()) || asPrimitiveCertain() != null;
  }

  /** @return true if one of {@link #INT} , {@link #LONG} , {@link #CHAR} ,
   *         {@link BYTE} , {@link SHORT} , {@link #INTEGRAL} or false
   *         otherwise */
  default boolean isIntegral() {
    return in(this, LONG, INT, CHAR, BYTE, SHORT, INTEGRAL);
  }

  /** @return true if one of {@link #INT} , {@link #LONG} , {@link #CHAR} ,
   *         {@link BYTE} , {@link SHORT} , {@link FLOAT} , {@link #DOUBLE} ,
   *         {@link #INTEGRAL} , {@link #NUMERIC} or false otherwise */
  default boolean isNumeric() {
    return in(this, INT, LONG, CHAR, BYTE, SHORT, FLOAT, DOUBLE, INTEGRAL, NUMERIC);
  }

  /** @return the formal name of this type, the key under which it is stored in
   *         {@link #types}, e.g., "Object", "int", "String", etc. */
  String key();

  /** An interface with one method- type, overloaded for many different
   * parameter types. Can be used to find the type of an expression thats known
   * at compile time by using overloading. Only use for testing, mainly for
   * testing of type.
   * @author Shalmon Niv
   * @year 2016 */
  @SuppressWarnings("unused") interface Axiom {
    static type.Primitive.Certain type(final boolean x) {
      return type.Primitive.Certain.BOOLEAN;
    }

    static type.Primitive.Certain type(final byte x) {
      return BYTE;
    }

    static type.Primitive.Certain type(final char x) {
      return CHAR;
    }

    static type.Primitive.Certain type(final double x) {
      return DOUBLE;
    }

    static type.Primitive.Certain type(final float x) {
      return FLOAT;
    }

    static type.Primitive.Certain type(final int x) {
      return INT;
    }

    static type.Primitive.Certain type(final long x) {
      return LONG;
    }

    static type type(final Object o) {
      return baptize("Object");
    }

    static type.Primitive.Certain type(final short x) {
      return SHORT;
    }

    static type.Primitive.Certain type(final String x) {
      return STRING;
    }
  }

  static class inner {
    private static String propertyName = "spartan type";
    /** All type that were ever born */
    private static Map<String, implementation> types = new LinkedHashMap<>();

    private static implementation conditionalWithNoInfo(final implementation i) {
      return in(i, BYTE, SHORT, CHAR, INT, INTEGRAL, LONG, FLOAT, NUMERIC) //
          ? NUMERIC //
          : !in(i, DOUBLE, STRING, BOOLEAN, BOOLEAN) //
              ? NOTHING : i;
    }

    /** @param n JD/
     * @return the type information stored inside the node n, or null if there
     *         is none */
    private static implementation getType(final ASTNode n) {
      return (implementation) n.getProperty(propertyName);
    }

    /** @param n JD
     * @return true if n has a type property and false otherwise */
    private static boolean hasType(final ASTNode n) {
      return getType(n) != null;
    }

    private static implementation lookDown(final Assignment x) {
      final implementation $ = lookDown(x.getLeftHandSide());
      return !$.isNoInfo() ? $ : lookDown(x.getRightHandSide()).isNumeric() ? NUMERIC : lookDown(x.getRightHandSide());
    }

    private static implementation lookDown(final CastExpression e) {
      return typeSwitch("" + step.type(e));
    }

    private static implementation lookDown(final ClassInstanceCreation c) {
      return typeSwitch("" + c.getType());
    }

    private static implementation lookDown(final ConditionalExpression e) {
      final implementation $ = lookDown(e.getThenExpression());
      final implementation ¢ = lookDown(e.getElseExpression());
      // If we don't know much about one operand but do know enough about the
      // other, we can still learn something
      return $ == ¢ ? $
          : $.isNoInfo() || ¢.isNoInfo() ? conditionalWithNoInfo($.isNoInfo() ? ¢ : $) //
              : $.isIntegral() && ¢.isIntegral() ? $.underIntegersOnlyOperator(¢) //
                  : $.isNumeric() && ¢.isNumeric() ? $.underNumericOnlyOperator(¢)//
                      : NOTHING; //
    }

    /** @param e JD
     * @return The most specific Type information that can be deduced about the
     *         expression from it's structure, or {@link #NOTHING} if it cannot
     *         decide. Will never return null */
    private static implementation lookDown(final Expression e) {
      if (hasType(e))
        return getType(e);
      switch (e.getNodeType()) {
        case NULL_LITERAL:
          return NULL;
        case CHARACTER_LITERAL:
          return CHAR;
        case STRING_LITERAL:
          return STRING;
        case BOOLEAN_LITERAL:
          return BOOLEAN;
        case NUMBER_LITERAL:
          return lookDown((NumberLiteral) e);
        case CAST_EXPRESSION:
          return lookDown((CastExpression) e);
        case PREFIX_EXPRESSION:
          return lookDown((PrefixExpression) e);
        case INFIX_EXPRESSION:
          return lookDown((InfixExpression) e);
        case POSTFIX_EXPRESSION:
          return lookDown((PostfixExpression) e);
        case PARENTHESIZED_EXPRESSION:
          return lookDown((ParenthesizedExpression) e);
        case CLASS_INSTANCE_CREATION:
          return lookDown((ClassInstanceCreation) e);
        case METHOD_INVOCATION:
          return lookDown((MethodInvocation) e);
        case CONDITIONAL_EXPRESSION:
          return lookDown((ConditionalExpression) e);
        case ASSIGNMENT:
          return lookDown((Assignment) e);
        default:
          return NOTHING;
      }
    }

    private static implementation lookDown(final InfixExpression e) {
      final InfixExpression.Operator o = e.getOperator();
      final List<Expression> es = extract.allOperands(e);
      assert es.size() >= 2;
      implementation $ = lookDown(lisp.first(es)).underBinaryOperator(o, lookDown(lisp.second(es)));
      lisp.chop(lisp.chop(es));
      while (!es.isEmpty()) {
        $ = $.underBinaryOperator(o, lookDown(lisp.first(es)));
        lisp.chop(es);
      }
      return $;
    }

    private static implementation lookDown(final MethodInvocation i) {
      return "toString".equals(i.getName() + "") && i.arguments().isEmpty() ? STRING : NOTHING;
    }

    private static implementation lookDown(final NumberLiteral l) {
      // TODO: Dor use TypeLiteral instead. It is thoroughly tested and very
      // accurate.
      final String ¢ = l.getToken();
      return ¢.matches("[0-9]+") ? INT
          : ¢.matches("[0-9]+[l,L]") ? LONG
              : ¢.matches("[0-9]+\\.[0-9]*[f,F]") || ¢.matches("[0-9]+[f,F]") ? FLOAT
                  : ¢.matches("[0-9]+\\.[0-9]*[d,D]?") || ¢.matches("[0-9]+[d,D]") ? DOUBLE : NUMERIC;
    }

    private static implementation lookDown(final ParenthesizedExpression e) {
      return lookDown(extract.core(e));
    }

    private static implementation lookDown(final PostfixExpression e) {
      return lookDown(e.getOperand()).asNumeric(); // see
                                                   // testInDecreamentSemantics
    }

    private static implementation lookDown(final PrefixExpression e) {
      return lookDown(e.getOperand()).under(e.getOperator());
    }

    private static implementation lookUp(final Expression e, final implementation i) {
      if (i.isCertain())
        return i;
      for (ASTNode context = parent(e); context != null; context = parent(context)) switch (context.getNodeType()) {
        case INFIX_EXPRESSION:
          return i.aboveBinaryOperator(az.infixExpression(context).getOperator());
        case ARRAY_ACCESS:
          return i.asIntegral();
        case PREFIX_EXPRESSION:
          return i.above(az.prefixExpression(context).getOperator());
        case POSTFIX_EXPRESSION:
          return i.asNumeric();
        case IF_STATEMENT:
        case ASSERT_STATEMENT:
        case FOR_STATEMENT:
        case WHILE_STATEMENT:
          return BOOLEAN;
        case PARENTHESIZED_EXPRESSION:
          continue;
        default:
          return i;
      }
      return i;
    }

    /** sets the type property in the ASTNode
     * @param n JD
     * @param i the node's type property
     * @return the type property t */
    private static implementation setType(final ASTNode n, final implementation i) {
      n.setProperty(propertyName, i);
      return i;
    }

    private static implementation typeSwitch(final String s) {
      switch (s) {
        case "byte":
        case "Byte":
          return BYTE;
        case "short":
        case "Short":
          return SHORT;
        case "char":
        case "Character":
          return CHAR;
        case "int":
        case "Integer":
          return INT;
        case "long":
        case "Long":
          return LONG;
        case "float":
        case "Float":
          return FLOAT;
        case "double":
        case "Double":
          return DOUBLE;
        case "boolean":
        case "Boolean":
          return BOOLEAN;
        case "String":
          return STRING;
        default:
          return baptize(s);
      }
    }

    // an interface for inner methods that shouldn't be public
    private interface implementation extends type {
      /** To be used to determine the type of something that o was used on
       * @return one of {@link #BOOLEAN} , {@link #INT} , {@link #LONG} ,
       *         {@link #DOUBLE} , {@link #INTEGRAL} or {@link #NUMERIC} , in
       *         case it cannot decide */
      default implementation above(final PrefixExpression.Operator o) {
        return o == NOT ? BOOLEAN : o != COMPLEMENT ? asNumeric() : asIntegral();
      }

      default implementation aboveBinaryOperator(final InfixExpression.Operator o) {
        return in(o, EQUALS, NOT_EQUALS) ? this
            : o == wizard.PLUS2 ? asAlphaNumeric()
                : wizard.isBitwiseOperator(o) ? asBooleanIntegral() : wizard.isShift(o) ? asIntegral() : asNumeric();
      }

      default implementation asAlphaNumeric() {
        return isAlphaNumeric() ? this : ALPHANUMERIC;
      }

      default implementation asBooleanIntegral() {
        return isIntegral() || this == BOOLEAN ? this : BOOLEANINTEGRAL;
      }

      /** @return one of {@link #INT}, {@link #LONG}, {@link #CHAR},
       *         {@link BYTE}, {@link SHORT} or {@link #INTEGRAL}, in case it
       *         cannot decide */
      default implementation asIntegral() {
        return isIntegral() ? this : INTEGRAL;
      }

      /** @return one of {@link #INT}, {@link #LONG}, or {@link #INTEGRAL}, in
       *         case it cannot decide */
      default implementation asIntegralUnderOperation() {
        return isIntUnderOperation() ? INT : asIntegral();
      }

      /** @return one of {@link #INT}, {@link #LONG},, {@link #CHAR},
       *         {@link BYTE}, {@link SHORT}, {@link FLOAT}, {@link #DOUBLE},
       *         {@link #INTEGRAL} or {@link #NUMERIC}, in case no further
       *         information is available */
      default implementation asNumeric() {
        return isNumeric() ? this : NUMERIC;
      }

      /** @return one of {@link #INT}, {@link #LONG}, {@link #FLOAT},
       *         {@link #DOUBLE}, {@link #INTEGRAL} or {@link #NUMERIC}, in case
       *         no further information is available */
      default implementation asNumericUnderOperation() {
        return !isNumeric() ? NUMERIC : isIntUnderOperation() ? INT : this;
      }

      /** used to determine whether an integral type behaves as itself under
       * operations or as an INT.
       * @return true if one of {@link #CHAR}, {@link BYTE}, {@link SHORT} or
       *         false otherwise. */
      default boolean isIntUnderOperation() {
        return in(this, CHAR, BYTE, SHORT);
      }

      /** @return true if one of {@link #NOTHING}, {@link #BAPTIZED},
       *         {@link #NONNULL}, {@link #VOID}, {@link #NULL} or false
       *         otherwise */
      default boolean isNoInfo() {
        return in(this, NOTHING, NULL);
      }

      @SuppressWarnings("synthetic-access") default implementation join() {
        assert !have(key());
        inner.types.put(key(), this);
        return this;
      }

      /** To be used to determine the type of the result of o being used on the
       * caller
       * @return one of {@link #BOOLEAN} , {@link #INT} , {@link #LONG} ,
       *         {@link #DOUBLE} , {@link #INTEGRAL} or {@link #NUMERIC} , in
       *         case it cannot decide */
      default implementation under(final PrefixExpression.Operator o) {
        assert o != null;
        return o == NOT ? BOOLEAN
            : in(o, DECREMENT, INCREMENT) ? asNumeric() : o != COMPLEMENT ? asNumericUnderOperation() : asIntegralUnderOperation();
      }

      /** @return one of {@link #BOOLEAN} , {@link #INT} , {@link #LONG} ,
       *         {@link #DOUBLE} , {@link #STRING} , {@link #INTEGRAL} ,
       *         {@link BOOLEANINTEGRAL} {@link #NUMERIC} , or
       *         {@link #ALPHANUMERIC} , in case it cannot decide */
      default implementation underBinaryOperator(final InfixExpression.Operator o, final implementation k) {
        if (o == wizard.PLUS2)
          return underPlus(k);
        if (wizard.isComparison(o))
          return BOOLEAN;
        if (wizard.isBitwiseOperator(o))
          return underBitwiseOperation(k);
        if (o == REMAINDER)
          return underIntegersOnlyOperator(k);
        if (in(o, LEFT_SHIFT, RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_UNSIGNED))
          return asIntegralUnderOperation();
        if (!in(o, TIMES, DIVIDE, wizard.MINUS2))
          throw new IllegalArgumentException("o=" + o + " k=" + k.fullName() + "this=" + this);
        return underNumericOnlyOperator(k);
      }

      /** @return one of {@link #BOOLEAN}, {@link #INT}, {@link #LONG},
       *         {@link #INTEGRAL} or {@link BOOLEANINTEGRAL}, in case it cannot
       *         decide */
      default implementation underBitwiseOperation(final implementation k) {
        return k == this ? k
            : isIntegral() && k.isIntegral() ? underIntegersOnlyOperator(k)
                : isNoInfo() ? k.underBitwiseOperationNoInfo() //
                    : k.isNoInfo() ? underBitwiseOperationNoInfo() //
                        : BOOLEANINTEGRAL;
      }

      /** @return one of {@link #BOOLEAN}, {@link #INT}, {@link #LONG},
       *         {@link #INTEGRAL} or {@link BOOLEANINTEGRAL}, in case it cannot
       *         decide */
      default implementation underBitwiseOperationNoInfo() {
        return this == BOOLEAN ? BOOLEAN : !isIntegral() ? BOOLEANINTEGRAL : this == LONG ? LONG : INTEGRAL;
      }

      default implementation underIntegersOnlyOperator(final implementation k) {
        final implementation ¢1 = asIntegralUnderOperation();
        final implementation ¢2 = k.asIntegralUnderOperation();
        return in(LONG, ¢1, ¢2) ? LONG : !in(INTEGRAL, ¢1, ¢2) ? INT : INTEGRAL;
      }

      /** @return one of {@link #INT}, {@link #LONG}, {@link #INTEGRAL},
       *         {@link #DOUBLE}, or {@link #NUMERIC}, in case it cannot
       *         decide */
      default implementation underNumericOnlyOperator(final implementation k) {
        if (!isNumeric())
          return asNumericUnderOperation().underNumericOnlyOperator(k);
        assert k != null;
        assert this != ALPHANUMERIC : "Don't confuse " + NUMERIC + " with " + ALPHANUMERIC;
        assert isNumeric() : this + ": is for some reason not numeric ";
        final implementation $ = k.asNumericUnderOperation();
        assert $ != null;
        assert $.isNumeric() : this + ": is for some reason not numeric ";
        // Double contaminates Numeric
        // Numeric contaminates Float
        // FLOAT contaminates Integral
        // LONG contaminates INTEGRAL
        // INTEGRAL contaminates INT
        // Everything else is INT after an operation
        return in(DOUBLE, $, this) ? DOUBLE
            : in(NUMERIC, $, this) ? NUMERIC //
                : in(FLOAT, $, this) ? FLOAT //
                    : in(LONG, $, this) ? LONG : //
                        !in(INTEGRAL, $, this) ? INT : INTEGRAL;
      }

      /** @return one of {@link #INT}, {@link #LONG}, {@link #DOUBLE},
       *         {@link #STRING}, {@link #INTEGRAL}, {@link #NUMERIC} or
       *         {@link #ALPHANUMERIC}, in case it cannot decide */
      default implementation underPlus(final implementation k) {
        // addition with NULL or String must be a String
        // unless both operands are numeric, the result may be a String
        return in(STRING, this, k) || in(NULL, this, k) ? STRING : !isNumeric() || !k.isNumeric() ? ALPHANUMERIC : underNumericOnlyOperator(k);
      }
    }
  }

  /** Types we do not full understand yet.
   * @author Yossi Gil
   * @author Shalmon Niv
   * @year 2016 */
  interface Odd extends inner.implementation {
    // Those anonymous characters that known little or nothing about
    // themselves
    /** TODO: Not sure we need all these {@link type.Odd.Types} values. */
    enum Types implements Odd {
      NULL("null", "when it is certain to be null: null, (null), ((null)), etc. but nothing else"), //
      NOTHING("none", "when nothing can be said, e.g., f(f(),f(f(f()),f()))"), //
      ;
      private final String description;
      private final String key;

      private Types(final String description, final String key) {
        this.description = description;
        this.key = key;
      }

      @Override public String description() {
        key();
        return description;
      }

      @Override public String key() {
        return key;
      }
    }
  }

  /** Primitive type or a set of primitive types
   * @author Yossi Gil
   * @year 2016 */
  interface Primitive extends inner.implementation {
    /** Primitive types known for certain. {@link String} is also considered
     * {@link Primitive.Certain}
     * @author Yossi Gil
     * @since 2016 */
    public enum Certain implements type.Primitive {
      BOOLEAN("boolean", "must be boolean: !f(), f() || g() "), //
      BYTE("byte", "must be byte: (byte)1, nothing else"), //
      CHAR("char", "must be char: 'a', (char)97, nothing else"), //
      DOUBLE("double", "must be double: 2.0, 2.0*a()-g(), no 2%a(), no 2*f()"), //
      FLOAT("float", "must be float: 2f, 2.3f+1, 2F-f()"), //
      INT("int", "must be int: 2, 2*(int)f(), 2%(int)f(), 'a'*2 , no 2*f()"), //
      LONG("long", "must be long: 2L, 2*(long)f(), 2%(long)f(), no 2*f()"), //
      SHORT("short", "must be short: (short)15, nothing else"), //
      STRING("String", "must be string: \"\"+a, a.toString(), f()+null, not f()+g()"), //
      ;
      final String description;
      final String key;

      Certain(final String key, final String description) {
        this.key = key;
        this.description = description;
      }

      @Override public type.Primitive.Certain asPrimitiveCertain() {
        return this;
      }

      @Override public type.Primitive.Uncertain asPrimitiveUncertain() {
        return isIntegral() ? INTEGRAL //
            : isNumeric() ? NUMERIC //
                : isAlphaNumeric() ? ALPHANUMERIC //
                    : this != BOOLEAN ? null : BOOLEANINTEGRAL;
      }

      @Override public boolean canB(final Certain ¢) {
        return ¢ == this;
      }

      @Override public String description() {
        return description;
      }

      @Override public String key() {
        return key;
      }
    }

    /** <p>
     * Tells how much we know about the type of of a variable, function, or
     * expression. This should be conservative approximation to the real type of
     * the entity, what a rational, but prudent programmer would case about the
     * type
     * <p>
     * Dispatching in this class should emulate the type inference of Java. It
     * is simple to that by hard coding constants.
     * <p>
     * This type should never be <code><b>null</b></code>. Don't bother to check
     * that it is. We want a {@link NullPointerException} thrown if this is the
     * case. or, you may as well write
     *
     * <pre>
     * Kind k = f();
     * assert k != null : //
     * "Implementation of Kind is buggy";
     * </pre>
     *
     * @author Yossi Gil
     * @author Niv Shalmon
     * @since 2016-08-XX */
    public enum Uncertain implements type.Primitive {
      // Doubtful types, from four fold uncertainty down to bilateral
      // schizophrenia" .
      ALPHANUMERIC(as.list(BOOLEAN, BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE, STRING), "only in binary plus: f()+g(), 2 + f(), nor f() + null"), //
      BOOLEANINTEGRAL(as.list(BOOLEAN, BYTE, SHORT, CHAR, INT, LONG), "only in x^y,x&y,x|y"), //
      INTEGER(as.list(INT, LONG), "must be either int or long: f()%g()^h()<<f()|g()&h(), not 2+(long)f() "), //
      INTEGRAL(as.list(BYTE, CHAR, SHORT, INT, LONG), "must be either int or long: f()%g()^h()<<f()|g()&h(), not 2+(long)f() "), //
      NUMERIC(as.list(BOOLEAN, BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE, STRING), "must be either f()*g(), 2L*f(), 2.*a(), not 2 %a(), nor 2"), //
      ;
      final String description;
      final List<Certain> represents;

      private Uncertain(final Iterable<? extends Certain> ts, final String description) {
        represents = new ArrayList<>();
        add(represents, ts);
        this.description = description;
      }

      @Override public String description() {
        return description;
      }

      @Override public String key() {
        return "" + represents;
      }

      /** @return A list of all Primitive.Certain types that an expression of
       *         this type can be */
      public List<Certain> possibleTypes() {
        return represents;
      }
    }
  }
} // end of interface type