package il.org.spartan.spartanizer.tippers;

import org.eclipse.jdt.core.dom.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.factory.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tipping.*;

/** reorder comparisons so that the specific value is placed on the right.
 * Specific value means a literal, or any of the two keywords
 *
 * <pre>
 * <b>this</b>
 * </pre>
 *
 * or
 *
 * <pre>
 * <b>null</b>
 * </pre>
 *
 * .
 * @author Yossi Gil
 * @since 2015-07-17 */
public final class InfixComparisonSpecific extends ReplaceCurrentNode<InfixExpression> implements TipperCategory.Idiomatic {
  private static final specificity specifity = new specificity();

  @Override public String description(@SuppressWarnings("unused") final InfixExpression __) {
    return "Exchange left and right operands of comparison";
  }

  @Override public boolean prerequisite(final InfixExpression ¢) {
    return specifity.compare(left(¢), right(¢)) < 0 && !¢.hasExtendedOperands() && iz.comparison(¢)
        && (specificity.defined(left(¢)) || specificity.defined(right(¢)));
  }

  @Override public Expression replacement(final InfixExpression ¢) {
    return make.conjugate(¢);
  }
}
