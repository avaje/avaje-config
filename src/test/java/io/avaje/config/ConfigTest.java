package io.avaje.config;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest {

  @Test
  public void fallbackToSystemProperty() {

    assertThat(Config.get("MySystemProp", null)).isNull();

    System.setProperty("MySystemProp", "hello");
    assertThat(Config.get("MySystemProp")).isEqualTo("hello");
    System.clearProperty("MySystemProp");
  }

  @Test
  public void asProperties() {
    String home = System.getProperty("user.home");

    final Properties properties = Config.asProperties();
    assertThat(properties.getProperty("myapp.fooName")).isEqualTo("Hello");
    assertThat(properties.getProperty("myapp.fooHome")).isEqualTo(home + "/config");
    assertThat(properties).hasSize(5);
  }

  @Ignore
  @Test
  public void load() {
    assertThat(System.getProperty("myapp.fooName")).isNull();

    assertThat(Config.get("myapp.fooName")).isEqualTo("Hello");
    assertThat(System.getProperty("myapp.fooName")).isNull();

    Config.loadIntoSystemProperties();
    assertThat(System.getProperty("myapp.fooName")).isEqualTo("Hello");
  }

  @Test
  public void get() {
    assertThat(Config.get("myapp.fooName", "junk")).isEqualTo("Hello");
  }

  @Test
  public void get_withEval() {
    String home = System.getProperty("user.home");
    assertThat(Config.get("myapp.fooHome")).isEqualTo(home + "/config");
  }

  @Test
  public void get_default() {
    assertThat(Config.get("myapp.doesNotExist", "MyDefault")).isEqualTo("MyDefault");
    assertThat(Config.get("myapp.doesNotExist", null)).isNull();
  }

  @Test
  public void get_optional() {
    assertThat(Config.getOptional("myapp.doesNotExist")).isEmpty();
    assertThat(Config.getOptional("myapp.fooName")).isNotEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void getBool_required_missing() {
    assertThat(Config.getBool("myapp.doesNotExist")).isTrue();
  }

  @Test
  public void getBool_required_set() {
    assertThat(Config.getBool("myapp.activateFoo")).isTrue();
  }

  @Test
  public void getBool() {
    assertThat(Config.getBool("myapp.activateFoo", false)).isTrue();
  }

  @Test
  public void getBool_default() {
    assertThat(Config.getBool("myapp.doesNotExist", false)).isFalse();
    assertThat(Config.getBool("myapp.doesNotExist", true)).isTrue();
  }

  @Test(expected = IllegalStateException.class)
  public void getInt_required_missing() {
    assertThat(Config.getInt("myapp.doesNotExist")).isEqualTo(42);
  }

  @Test
  public void getInt_required_set() {
    assertThat(Config.getInt("myapp.bar.barRules")).isEqualTo(42);
  }

  @Test
  public void getInt() {
    assertThat(Config.getInt("myapp.bar.barRules", 99)).isEqualTo(42);
  }

  @Test
  public void getInt_default() {
    assertThat(Config.getInt("myapp.bar.doesNotExist", 99)).isEqualTo(99);
  }

  @Test(expected = IllegalStateException.class)
  public void getLong_required_missing() {
    assertThat(Config.getLong("myapp.bar.doesNotExist")).isEqualTo(42L);
  }

  @Test
  public void getLong_required_set() {
    assertThat(Config.getLong("myapp.bar.barRules")).isEqualTo(42L);
  }

  @Test
  public void getLong() {
    assertThat(Config.getLong("myapp.bar.barRules", 99)).isEqualTo(42L);
  }

  @Test
  public void getLong_default() {
    assertThat(Config.getLong("myapp.bar.doesNotExist", 99)).isEqualTo(99L);
  }

  @Test
  public void onChange() {

    AtomicReference<String> ref = new AtomicReference<>();
    ref.set("initialValue");

    Config.onChange("some.key", ref::set);

    assertThat(ref.get()).isEqualTo("initialValue");
    Config.setProperty("some.key", "val1");
    assertThat(ref.get()).isEqualTo("val1");

    Config.setProperty("some.key", "val2");
    assertThat(ref.get()).isEqualTo("val2");
  }

  @Test
  public void onChangeInt() {

    AtomicReference<Integer> ref = new AtomicReference<>();
    ref.set(1);

    Config.onChangeInt("some.intKey", ref::set);

    assertThat(ref.get()).isEqualTo(1);

    Config.setProperty("some.intKey", "2");
    assertThat(ref.get()).isEqualTo(2);

    Config.setProperty("some.intKey", "42");
    assertThat(ref.get()).isEqualTo(42);
  }

  @Test
  public void onChangeLong() {

    AtomicReference<Long> ref = new AtomicReference<>();
    ref.set(1L);

    Config.onChangeLong("some.longKey", ref::set);

    assertThat(ref.get()).isEqualTo(1);

    Config.setProperty("some.longKey", "2");
    assertThat(ref.get()).isEqualTo(2);

    Config.setProperty("some.longKey", "42");
    assertThat(ref.get()).isEqualTo(42);
  }

  @Test
  public void onChangeBool() {

    AtomicReference<Boolean> ref = new AtomicReference<>();
    ref.set(false);

    Config.onChangeBool("some.boolKey", ref::set);

    assertThat(ref.get()).isFalse();

    Config.setProperty("some.boolKey", "true");
    assertThat(ref.get()).isTrue();

    Config.setProperty("some.boolKey", "false");
    assertThat(ref.get()).isFalse();
  }
}
