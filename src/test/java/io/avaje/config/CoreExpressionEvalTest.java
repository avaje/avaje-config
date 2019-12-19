package io.avaje.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CoreExpressionEvalTest {

	@Test
	public void eval_null() {
		assertNull(CoreExpressionEval.eval(null));
	}

	@Test
	public void eval_empty() {
		assertEquals("", CoreExpressionEval.eval(""));
	}

	@Test
	public void eval_noExpressions() {
		assertEquals("basic", CoreExpressionEval.eval("basic"));
		assertEquals("{basic}", CoreExpressionEval.eval("{basic}"));
	}

	@Test
	public void eval_singleExpression() {
		System.setProperty("foo", "Hello");
		assertEquals("Hello", CoreExpressionEval.eval("${foo}"));
		assertEquals("preHello", CoreExpressionEval.eval("pre${foo}"));
		assertEquals("HelloPost", CoreExpressionEval.eval("${foo}Post"));
		assertEquals("beforeHelloAfter", CoreExpressionEval.eval("before${foo}After"));
		System.clearProperty("foo");
	}

	@Test
	public void eval_singleExpression_withDefault() {

		System.setProperty("foo", "Hello");
		assertEquals("Hello", CoreExpressionEval.eval("${foo:bart}"));
		assertEquals("beforeHelloAfter", CoreExpressionEval.eval("before${foo:bart}After"));
		assertEquals("preHello", CoreExpressionEval.eval("pre${foo:bart}"));
		assertEquals("HelloPost", CoreExpressionEval.eval("${foo:bart}Post"));

		System.clearProperty("foo");
		assertEquals("bart", CoreExpressionEval.eval("${foo:bart}"));
		assertEquals("before-bart-after", CoreExpressionEval.eval("before-${foo:bart}-after"));
		assertEquals("pre-bart", CoreExpressionEval.eval("pre-${foo:bart}"));
		assertEquals("bart-post", CoreExpressionEval.eval("${foo:bart}-post"));

	}

	@Test
	public void eval_multiExpression_withDefault() {

		assertEquals("num1num2", CoreExpressionEval.eval("${one:num1}${two:num2}"));
		assertEquals("num1-num2", CoreExpressionEval.eval("${one:num1}-${two:num2}"));
		assertEquals("num1abnum2", CoreExpressionEval.eval("${one:num1}ab${two:num2}"));
		assertEquals("anum1bcnum2d", CoreExpressionEval.eval("a${one:num1}bc${two:num2}d"));

		System.setProperty("one", "first");
		System.setProperty("two", "second");

		assertEquals("firstsecond", CoreExpressionEval.eval("${one:num1}${two:num2}"));
		assertEquals("first-second", CoreExpressionEval.eval("${one:num1}-${two:num2}"));
		assertEquals("pre-first-second-post", CoreExpressionEval.eval("pre-${one:num1}-${two:num2}-post"));
		assertEquals("AfirstBCsecondD", CoreExpressionEval.eval("A${one:num1}BC${two:num2}D"));


		System.clearProperty("one");
		System.clearProperty("two");
	}
}
