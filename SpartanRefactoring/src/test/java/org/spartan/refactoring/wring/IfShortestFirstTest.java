package org.spartan.refactoring.wring;

import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.wring.AbstractWringTest.OutOfScope;
import org.spartan.refactoring.wring.AbstractWringTest.Wringed;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings("javadoc") //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public enum IfShortestFirstTest {
  ;
  static final Wring WRING = new IfShortestFirst();

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Expression vs. Expression", " 6 - 7 < 2 + 1   "), //
        Utils.asArray("Return only on one side", "if (a) return b; else c;"), //
        Utils.asArray("Simple if return", "if (a) return b; else return c;"), //
        Utils.asArray("Simply nested if return", "{if (a)  return b; else return c;}"), //
        Utils.asArray("Nested if return", "if (a) {;{{;;return b; }}} else {{{;return c;};;};}"), //
        Utils.asArray("No else", "if (a) {;{{;;return b; }}}"), //
        new String[] { "Two statemens are greater than one", //
            "if (a) {i++;j++;} else b(asdf,as,as,asdf,adfasd,adadfadf,asfasdfasdf);", //
        }, //
        null);
    /**
     * Generate test cases for this parameterized class.
     *
     * @return a collection of cases, where each case is an array of three
     *         objects, the test case name, the input, and the file.
     */
    @Parameters(name = DESCRIPTION) //
    public static Collection<Object[]> cases() {
      return collect(cases);
    }
    /** Instantiates the enclosing class ({@link OutOfScope}) */
    public OutOfScope() {
      super(WRING);
    }
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.WringedIfStatement {
    private static String[][] cases = Utils.asArray(//
        Utils.asArray("Vanilla", "if (a) a(x,y,z,w); else b();", "if (!a) b(); else a(x,y,z,w);"), //
        new String[] { "Two statemens are greater than one", //
            "if (a) {i++;j++;} else b(asdf,as,as,asdf,adfasd,adadfadf,asfasdfasdf);", //
            "if (!a) b(asdf,as,as,asdf,adfasd,adadfadf,asfasdfasdf); else {i++;j++;} ", //
        }, //
        null);
    /**
     * Generate test cases for this parameterized class.
     *
     * @return a collection of cases, where each case is an array of three
     *         objects, the test case name, the input, and the file.
     */
    @Parameters(name = DESCRIPTION) //
    public static Collection<Object[]> cases() {
      return collect(cases);
    }
    /**
     * Instantiates the enclosing class ({@link Wringed})
     */
    public Wringed() {
      super(WRING);
    }
  }
}
