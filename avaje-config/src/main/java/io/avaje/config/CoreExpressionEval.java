package io.avaje.config;

import java.util.Properties;

/**
 * Helper used to evaluate expressions such as ${CATALINA_HOME}.
 * <p>
 * The expressions can contain environment variables or system properties.
 */
final class CoreExpressionEval implements Configuration.ExpressionEval {

  /**
   * Used to detect the start of an expression.
   */
  private static final String START = "${";

  /**
   * Used to detect the end of an expression.
   */
  private static final String END = "}";

  private CoreEntry.Map sourceMap;
  private Properties sourceProperties;

  /**
   * Create with source map that can use used to eval expressions.
   */
  CoreExpressionEval(CoreEntry.Map sourceMap) {
    this.sourceMap = sourceMap;
  }

  /**
   * Create with source properties that can be used to eval expressions.
   */
  CoreExpressionEval(Properties sourceProperties) {
    this.sourceProperties = sourceProperties;
  }

  @Override
  public String eval(String val) {
    if (val == null) {
      return null;
    }
    int sp = val.indexOf(START);
    if (sp > -1) {
      int ep = val.indexOf(END, sp + 1);
      if (ep > -1) {
        return eval(val, sp, ep);
      }
    }
    return val;
  }

  /**
   * Convert the expression usingEnvironment variables, System Properties or an existing property.
   */
  private String evaluateExpression(String exp) {
    String val = System.getProperty(exp);
    if (val == null) {
      val = System.getenv(exp);
      if (val == null) {
        val = localLookup(exp);
      }
    }
    return val;
  }

  private String localLookup(String exp) {
    if (sourceMap != null) {
      return sourceMap.raw(exp);
    } else if (sourceProperties != null) {
      return sourceProperties.getProperty(exp);
    }
    return null;
  }

  private String eval(String val, int sp, int ep) {
    return new EvalBuffer(val, sp, ep).process();
  }

  private class EvalBuffer {

    private final StringBuilder buf = new StringBuilder();
    private final String original;
    private int position;
    private int start;
    private int end;
    private String expression;
    private String defaultValue;

    EvalBuffer(String val, int start, int ep) {
      this.original = val;
      this.start = start;
      this.end = ep;
      this.position = 0;
      moveToStart();
    }

    void moveToStart() {
      if (start > position) {
        buf.append(original, position, start);
        position = start;
      }
    }

    void parseForDefault() {
      int colonPos = original.indexOf(':', start);
      if (colonPos > start && colonPos < end) {
        expression = original.substring(start + START.length(), colonPos);
        defaultValue = original.substring(colonPos + 1, end);
      } else {
        expression = original.substring(start + START.length(), end);
      }
    }

    void evaluate() {
      String eval = evaluateExpression(expression);
      if (eval != null) {
        buf.append(eval);
      } else {
        if (defaultValue != null) {
          buf.append(defaultValue);
        } else {
          buf.append(START).append(expression).append(END);
        }
      }
    }

    String end() {
      if (end < original.length() - 1) {
        buf.append(original.substring(end + 1));
      }
      return buf.toString();
    }

    boolean next() {
      if (end < original.length()) {
        int startPos = original.indexOf(START, end + 1);
        if (startPos > -1) {
          int endPos = original.indexOf(END, startPos + 1);
          if (endPos > -1) {
            if (startPos > end + 1) {
              buf.append(original, end + 1, startPos);
            }
            this.start = startPos;
            this.end = endPos;
            return true;
          }
        }
      }
      return false;
    }

    private void evalNext() {
      parseForDefault();
      evaluate();
    }

    String process() {
      evalNext();
      while (next()) {
        evalNext();
      }
      return end();
    }

  }

}
