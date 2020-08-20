package io.avaje.config;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest {

  @Test
  public void fallbackToSystemProperty_initial() {
    System.setProperty("MySystemProp0", "bar");
    assertThat(Config.get("MySystemProp0", "foo")).isEqualTo("bar");

    // cached the initial value, still bar even when system property changed
    System.setProperty("MySystemProp0", "bazz");
    assertThat(Config.get("MySystemProp0", null)).isEqualTo("bar");

    // mutate via Config.setProperty()
    Config.setProperty("MySystemProp0", "caz");
    assertThat(Config.get("MySystemProp0")).isEqualTo("caz");

    assertThat(Config.getList().of("MySystemProp0")).contains("caz");
    assertThat(Config.getSet().of("MySystemProp0")).contains("caz");
  }

  @Test
  public void fallbackToSystemProperty_cacheInitialNullValue() {
    assertThat(Config.get("MySystemProp", null)).isNull();
    System.setProperty("MySystemProp", "hello");
    // cached the initial null so still null
    assertThat(Config.get("MySystemProp", null)).isNull();
  }

  @Test
  public void fallbackToSystemProperty_cacheInitialValue() {
    assertThat(Config.get("MySystemProp2", "foo")).isEqualTo("foo");
    System.setProperty("MySystemProp2", "notFoo");
    // cached the initial value foo so still foo
    assertThat(Config.get("MySystemProp2", null)).isEqualTo("foo");
    Config.setProperty("MySystemProp2", null);
  }

  @Test
  public void setProperty() {
    assertThat(Config.get("MySystemProp3", null)).isNull();
    Config.setProperty("MySystemProp3", "hello2");
    assertThat(Config.get("MySystemProp3")).isEqualTo("hello2");
  }

  @Test
  public void asProperties() {
    String home = System.getProperty("user.home");

    final Properties properties = Config.asProperties();
    assertThat(properties.getProperty("myapp.fooName")).isEqualTo("Hello");
    assertThat(properties.getProperty("myapp.fooHome")).isEqualTo(home + "/config");
    assertThat(properties).hasSize(5);
  }

  @Test
  public void asConfiguration() {
    String home = System.getProperty("user.home");

    final Configuration configuration = Config.asConfiguration();
    assertThat(configuration.get("myapp.fooName")).isEqualTo("Hello");
    assertThat(configuration.get("myapp.fooHome")).isEqualTo(home + "/config");
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
    assertThat(Config.get("myapp.doesNotExist2", "MyDefault")).isEqualTo("MyDefault");
    assertThat(Config.get("myapp.doesNotExist2", null)).isEqualTo("MyDefault");
  }

  @Test
  public void get_optional() {
    assertThat(Config.getOptional("myapp.doesNotExist")).isEmpty();
    assertThat(Config.getOptional("myapp.fooName")).isNotEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void getBool_required_missing() {
    Config.setProperty("myapp.doesNotExist", null);
    assertThat(Config.getBool("myapp.doesNotExist")).isTrue();
  }

  @Test(expected = IllegalStateException.class)
  public void enabled_required_missing() {
    Config.setProperty("myapp.doesNotExist", null);
    assertThat(Config.enabled("myapp.doesNotExist")).isTrue();
  }

  @Test
  public void getBool_required_set() {
    assertThat(Config.getBool("myapp.activateFoo")).isTrue();
    assertThat(Config.enabled("myapp.activateFoo")).isTrue();
  }

  @Test
  public void getBool() {
    assertThat(Config.getBool("myapp.activateFoo", false)).isTrue();
    assertThat(Config.enabled("myapp.activateFoo", false)).isTrue();
  }

  @Test
  public void getBool_default() {
    assertThat(Config.getBool("myapp.doesNotExist", false)).isFalse();
    // default value is cached, still false
    assertThat(Config.getBool("myapp.doesNotExist", true)).isFalse();
    // can dynamically change
    Config.setProperty("myapp.doesNotExist", "true");
    assertThat(Config.getBool("myapp.doesNotExist", true)).isTrue();
  }

  @Test
  public void enabled_default() {
    assertThat(Config.enabled("myapp.en.doesNotExist", false)).isFalse();
    // default value is cached, still false
    assertThat(Config.enabled("myapp.en.doesNotExist", true)).isFalse();
    // can dynamically change
    Config.setProperty("myapp.en.doesNotExist", "true");
    assertThat(Config.enabled("myapp.en.doesNotExist", true)).isTrue();
    Config.setProperty("myapp.en.doesNotExist", null);
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

  @Test(expected = IllegalStateException.class)
  public void getEnum_doesNotExist() {
    Config.getEnum(MyTestEnum.class, "myTestEnum.doesNotExist");
  }

  @Test
  public void getEnum_default() {
    assertThat(Config.getEnum(MyTestEnum.class, "myTestEnum.doesNotExist", MyTestEnum.C)).isEqualTo(MyTestEnum.C);
  }

  @Test
  public void getEnum() {
    Config.setProperty("myTestEnum", "B");
    assertThat(Config.getEnum(MyTestEnum.class, "myTestEnum")).isEqualTo(MyTestEnum.B);
    assertThat(Config.getEnum(MyTestEnum.class, "myTestEnum", MyTestEnum.C)).isEqualTo(MyTestEnum.B);
    Config.setProperty("myTestEnum", null);
  }

  enum MyTestEnum {
    A, B, C
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
