package io.avaje.config;

import java.util.Enumeration;
import java.util.Properties;

public class PropertyExpression {

  /**
   * Hide constructor.
   */
  private PropertyExpression() {
  }

  /**
   * Return the property value evaluating and replacing any expressions
   * such as <code>${user.home}</code>.
   */
  public static String eval(String val) {
    return CoreExpressionEval.eval(val);
  }

  /**
   * Return a copy of the properties with 'eval' run on all the values.
   */
  public static Properties eval(Properties properties) {
    Properties evalCopy = new Properties();

    Enumeration<?> names = properties.propertyNames();
    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();
      String value = eval(properties.getProperty(name));
      evalCopy.setProperty(name, value);
    }
    return evalCopy;
  }

}
