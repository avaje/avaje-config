package io.avaje.config.load;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CoreExpressionEvalTest {

  @Test
  public void eval_null() {
    assertNull(eval(null));
  }

  @Test
  public void eval_empty() {
    assertEquals("", eval(""));
  }

  @Test
  public void eval_noExpressions() {
    assertEquals("basic", eval("basic"));
    assertEquals("{basic}", eval("{basic}"));
  }

  @Test
  public void eval_singleExpression() {
    System.setProperty("foo", "Hello");
    assertEquals("Hello", eval("${foo}"));
    assertEquals("preHello", eval("pre${foo}"));
    assertEquals("HelloPost", eval("${foo}Post"));
    assertEquals("beforeHelloAfter", eval("before${foo}After"));
    System.clearProperty("foo");
  }

  @Test
  public void eval_singleExpression_withDefault() {

    System.setProperty("foo", "Hello");
    assertEquals("Hello", eval("${foo:bart}"));
    assertEquals("beforeHelloAfter", eval("before${foo:bart}After"));
    assertEquals("preHello", eval("pre${foo:bart}"));
    assertEquals("HelloPost", eval("${foo:bart}Post"));

    System.clearProperty("foo");
    assertEquals("bart", eval("${foo:bart}"));
    assertEquals("before-bart-after", eval("before-${foo:bart}-after"));
    assertEquals("pre-bart", eval("pre-${foo:bart}"));
    assertEquals("bart-post", eval("${foo:bart}-post"));

  }

  @Test
  public void eval_multiExpression_withDefault() {

    assertEquals("num1num2", eval("${one:num1}${two:num2}"));
    assertEquals("num1-num2", eval("${one:num1}-${two:num2}"));
    assertEquals("num1abnum2", eval("${one:num1}ab${two:num2}"));
    assertEquals("anum1bcnum2d", eval("a${one:num1}bc${two:num2}d"));

    System.setProperty("one", "first");
    System.setProperty("two", "second");

    assertEquals("firstsecond", eval("${one:num1}${two:num2}"));
    assertEquals("first-second", eval("${one:num1}-${two:num2}"));
    assertEquals("pre-first-second-post", eval("pre-${one:num1}-${two:num2}-post"));
    assertEquals("AfirstBCsecondD", eval("A${one:num1}BC${two:num2}D"));


    System.clearProperty("one");
    System.clearProperty("two");
  }

  private String eval(String key) {
    return new CoreExpressionEval().eval(key);
  }
}
