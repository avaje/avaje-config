package org.example;

import io.avaje.config.Configuration;
import io.avaje.config.ConfigurationSource;

public class MyExternalLoader implements ConfigurationSource {

  static boolean refreshCalled;

  @Override
  public void load(Configuration configuration) {

    // we can read properties that have been already
    // loaded from files/resources if desired
    configuration.getOptional("myExternalLoader.location");

    // add set properties (kind of the point)
    configuration.setProperty("myExternalLoader", "wasExecuted");

    // schedule a task if we like
    configuration.schedule(500, 500, () -> System.out.println("MyExternalLoader task .."));
  }

  @Override
  public void refresh() {
    refreshCalled = true;
  }

  public static boolean refreshCalled() {
    return refreshCalled;
  }

  public static void reset() {
    refreshCalled = false;
  }
}
