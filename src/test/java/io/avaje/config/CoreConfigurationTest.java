package io.avaje.config;

import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class CoreConfigurationTest {

  private CoreConfiguration data = createSample();

  private Properties basicProperties() {
    Properties properties = new Properties();
    properties.setProperty("a", "1");
    properties.setProperty("foo.bar", "42");
    properties.setProperty("foo.t", "true");
    properties.setProperty("foo.f", "false");
    properties.setProperty("modify", "me");
    return properties;
  }

  private CoreConfiguration createSample() {
    return new CoreConfiguration(basicProperties());
  }

  @Test
  public void get() {
    assertEquals(data.get("a", "something"), "1");
    assertEquals(data.get("doesNotExist", "something"), "something");
  }

  @Test
  public void getBool() {
    assertTrue(data.getBool("foo.t", true));
    assertTrue(data.getBool("foo.t", false));

    assertFalse(data.getBool("foo.f", true));
    assertFalse(data.getBool("foo.f", false));
  }

  @Test
  public void getInt() {
  }

  @Test
  public void getLong() {
  }

  @Test
  public void onChange() {
    AtomicInteger count = new AtomicInteger();
    StringBuilder sb = new StringBuilder();
    data.onChange("modify", newValue -> {
      count.incrementAndGet();
      sb.append(newValue).append(",");
    });

    data.setProperty("modify", "me");
    assertThat(count.get()).isEqualTo(0);

    data.setProperty("modify", "change");
    assertThat(count.get()).isEqualTo(1);

    data.setProperty("modify", "change");
    assertThat(count.get()).isEqualTo(1);

    data.setProperty("modify", null);
    assertThat(count.get()).isEqualTo(2);
    assertThat(sb.toString()).isEqualTo("change,null,");

    data.setProperty("modify", "change");
    assertThat(count.get()).isEqualTo(3);
    assertThat(sb.toString()).isEqualTo("change,null,change,");
  }

  @Test
  public void onChangeInt() {
  }

  @Test
  public void onChangeLong() {
  }

  @Test
  public void onChangeBool() {
  }

  @Test
  public void evalProperties() {

    final Properties properties = basicProperties();
    properties.setProperty("someA", "before-${foo.bar}-after");

    final CoreConfiguration config = new CoreConfiguration(new Properties());
    final Properties copy = config.eval(properties);

    assertThat(copy.getProperty("someA")).isEqualTo("before-42-after");
  }
}
