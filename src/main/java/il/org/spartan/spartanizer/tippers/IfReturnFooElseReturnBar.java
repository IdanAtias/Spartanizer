package il.org.spartan.spartanizer.tippers;

import org.eclipse.jdt.core.dom.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.factory.*;
import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.tipping.*;

/** convert
 *
 * <pre>
 * if (x)
 *   return b;
 * else
 *   return c;
 * </pre>
 *
 * into
 *
 * <pre>
 * return x? b : c
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-07-29 */
public final class IfReturnFooElseReturnBar extends ReplaceCurrentNode<IfStatement> implements TipperCategory.Ternarization {
  @Override public String description(@SuppressWarnings("unused") final IfStatement __) {
    return "Replace if with a return of a conditional statement";
  }

  @Override public boolean prerequisite(final IfStatement ¢) {
    return ¢ != null && extract.returnExpression(then(¢)) != null && extract.returnExpression(elze(¢)) != null;
  }

  @Override public Statement replacement(final IfStatement s) {
    final Expression condition = s.getExpression();
    final Expression then = extract.returnExpression(then(s));
    final Expression elze = extract.returnExpression(elze(s));
    return then == null || elze == null ? null : subject.operand(subject.pair(then, elze).toCondition(condition)).toReturn();
  }
}
