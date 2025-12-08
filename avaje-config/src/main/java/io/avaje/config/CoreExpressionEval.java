package io.avaje.config;

import org.jspecify.annotations.Nullable;

import java.util.Properties;

import static java.util.Objects.requireNonNull;

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
  private static final String DOCKER_HOST = "docker.host";

  private CoreEntry.CoreMap sourceMap;
  private Properties sourceProperties;

  /**
   * Create with source map that can use used to eval expressions.
   */
  CoreExpressionEval(CoreEntry.CoreMap sourceMap) {
    this.sourceMap = sourceMap;
  }

  /**
   * Create with source properties that can be used to eval expressions.
   */
  CoreExpressionEval(Properties sourceProperties) {
    this.sourceProperties = sourceProperties;
  }

  /**
   * Evaluate all the entries until no more can be resolved.
   *
   * @param map The source map which is copied and resolved
   * @return A copy of the source map will all entries resolved if possible
   */
  static CoreEntry.CoreMap evalFor(CoreEntry.CoreMap map) {
    final var copy = CoreEntry.newMap(map);
    return new CoreExpressionEval(copy).evalAll();
  }

  private CoreEntry.CoreMap evalAll() {
    sourceMap.forEach((key, entry) -> {
      if (entry.needsEvaluation()) {
        sourceMap.put(key, eval(entry.value()), requireNonNull(entry.source()));
      }
    });
    return sourceMap;
  }

  @Override
  @Nullable
  public String eval(@Nullable String val) {
    return val == null ? null : evalRecurse(val);
  }

  private String evalRecurse(String input) {
    final String resolved = evalInput(input);
    if (resolved.contains(START) && !resolved.equals(input)) {
      return evalRecurse(resolved);
    } else {
      return resolved;
    }
  }

  private String evalInput(String input) {
    final int start = input.indexOf(START);
    if (start == -1) {
      return input;
    }
    final int end = input.indexOf(END, start + 1);
    return end == -1 ? input : eval(input, start, end);
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

  private String eval(String val, int start, int end) {
    return new EvalBuffer(val, start, end).process();
  }

  private final class EvalBuffer {

    private final StringBuilder buf = new StringBuilder();
    private final String original;
    private int position;
    private int start;
    private int end;
    private String expression;
    private String defaultValue;

    EvalBuffer(String val, int start, int end) {
      this.original = val;
      this.start = start;
      this.end = end;
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
        } else if (DOCKER_HOST.equals(expression)) {
          final String dockerHost = DockerHost.host();
          buf.append(dockerHost);
          System.setProperty("docker.host", dockerHost);
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
