package io.avaje.config;

import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreConfigurationTest {

  private final CoreConfiguration data = createSample();

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
    assertThat(data.getInt("a", 99)).isEqualTo(1);
    assertThat(data.getInt("foo.bar", 99)).isEqualTo(42);
    assertThat(data.getInt("doesNotExist", 99)).isEqualTo(99);
  }

  @Test
  public void getLong() {
    assertThat(data.getLong("a", 99)).isEqualTo(1);
    assertThat(data.getLong("foo.bar", 99)).isEqualTo(42);
    assertThat(data.getLong("doesNotExist", 99)).isEqualTo(99);
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
  public void setProperty_withEval() {
    assertThat(data.get("ThisIsNotSet", null)).isNull();
    data.setProperty("ThisIsNotSet", "A|${user.home}|B");
    String expected = "A|" + System.getProperty("user.home") + "|B";
    assertThat(data.get("ThisIsNotSet", null)).isEqualTo(expected);
  }

  @Test
  public void onChangeInt() {

    AtomicReference<Integer> ref = new AtomicReference<>();
    ref.set(1);

    data.onChangeInt("some.intKey", ref::set);

    assertThat(ref.get()).isEqualTo(1);

    data.setProperty("some.intKey", "2");
    assertThat(ref.get()).isEqualTo(2);

    data.setProperty("some.intKey", "42");
    assertThat(ref.get()).isEqualTo(42);
  }

  @Test
  public void onChangeLong() {

    AtomicReference<Long> ref = new AtomicReference<>();
    ref.set(1L);

    data.onChangeLong("some.longKey", ref::set);

    assertThat(ref.get()).isEqualTo(1);

    data.setProperty("some.longKey", "2");
    assertThat(ref.get()).isEqualTo(2);

    data.setProperty("some.longKey", "42");
    assertThat(ref.get()).isEqualTo(42);
  }

  @Test
  public void onChangeBool() {

    AtomicReference<Boolean> ref = new AtomicReference<>();
    ref.set(false);

    data.onChangeBool("some.boolKey", ref::set);

    assertThat(ref.get()).isFalse();

    data.setProperty("some.boolKey", "true");
    assertThat(ref.get()).isTrue();

    data.setProperty("some.boolKey", "false");
    assertThat(ref.get()).isFalse();
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
