package io.avaje.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class CoreConfigurationTest {

  private final CoreConfiguration data = createSample();

  private Properties basicProperties() {
    Properties properties = new Properties();
    properties.setProperty("a", "1");
    properties.setProperty("foo.bar", "42");
    properties.setProperty("foo.t", "true");
    properties.setProperty("foo.f", "false");
    properties.setProperty("modify", "me");
    properties.setProperty("someValues", "13,42,55");
    properties.setProperty("1someValues", "13,42,55");
    properties.setProperty("myEnum", "TWO");
    return properties;
  }

  private CoreConfiguration createSample() {
    return new CoreConfiguration(new DefaultEventLog(), basicProperties());
  }

  @Test
  void asProperties() {
    final Properties properties = basicProperties();
    System.setProperty("SetViaSystemProperty", "FooBar");

    final CoreConfiguration configuration = new CoreConfiguration(new DefaultEventLog(), properties);
    configuration.initSystemProperties();

    final Properties loaded = configuration.asProperties();
    assertThat(loaded.get("a")).isEqualTo("1");
    assertThat(loaded.get("SetViaSystemProperty")).isNull();

    System.clearProperty("SetViaSystemProperty");
  }

  @Test
  void forPath() {
    CoreConfiguration base = createSample();
    Configuration foo = base.forPath("foo");

    assertThat(foo.size()).isEqualTo(3);
    assertThat(foo.getInt("bar")).isEqualTo(42);
    assertThat(foo.getBool("t")).isTrue();
    assertThat(foo.get("f")).isEqualTo("false");
    assertThat(foo.getOptional("a")).isEmpty();
  }

  @Test
  void forPathUnknown_expect_fullPathInMessage() {
    CoreConfiguration base = createSample();
    Configuration foo = base.forPath("foo");

    assertThatThrownBy(() -> foo.get("iDoNotExistSoIthrow"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("foo.iDoNotExistSoIthrow");
  }

  @Test
  void forPath_nested() {
    Properties properties = new Properties();
    properties.setProperty("one.greet", "hi");
    properties.setProperty("one.nested", "n");
    properties.setProperty("one.nested.num", "42");
    properties.setProperty("one.nested.active", "true");
    properties.setProperty("one.nested.again.more", "other");
    properties.setProperty("one.nested2.b", "b");
    properties.setProperty("one", "spud");
    properties.setProperty("oneNot", "fried");
    properties.setProperty("modify", "me");

    CoreConfiguration base = new CoreConfiguration(new DefaultEventLog(), properties);
    assertThat(base.size()).isEqualTo(9);

    Configuration one = base.forPath("one");
    assertThat(one.size()).isEqualTo(7);
    assertThat(one.asProperties()).containsOnlyKeys("greet", "nested", "nested.num", "nested.active", "nested.again.more", "", "nested2.b");
    assertThat(one.get("")).isEqualTo("spud");

    Configuration nested = one.forPath("nested");
    assertThat(nested.size()).isEqualTo(4);
    assertThat(nested.asProperties()).containsOnlyKeys("", "num", "active", "again.more");
    assertThat(nested.get("")).isEqualTo("n");

    Configuration nested2 = base.forPath("one.nested");
    assertThat(nested2.size()).isEqualTo(4);
    assertThat(nested2.asProperties()).containsOnlyKeys("", "num", "active", "again.more");
  }

  @Test
  void test_toString() {
    assertThat(data.toString()).isNotEmpty();
    data.setWatcher(new FileWatch( new CoreConfiguration(new DefaultEventLog(), new Properties()) , Collections.emptyList(), null));
    data.loadIntoSystemProperties();
  }

  @Test
  void get() {
    assertEquals(data.get("a", "something"), "1");
    assertEquals(data.get("doesNotExist", "something"), "something");
  }

  @Test
  void getBool() {
    assertTrue(data.getBool("foo.t", true));
    assertTrue(data.getBool("foo.t", false));

    assertFalse(data.getBool("foo.f", true));
    assertFalse(data.getBool("foo.f", false));
  }

  @Test
  void getInt() {
    assertThat(data.getInt("a", 99)).isEqualTo(1);
    assertThat(data.getInt("foo.bar", 99)).isEqualTo(42);
    assertThat(data.getInt("doesNotExist", 99)).isEqualTo(99);
  }

  @Test
  void getLong() {
    assertThat(data.getLong("a", 99)).isEqualTo(1);
    assertThat(data.getLong("foo.bar", 99)).isEqualTo(42);
    assertThat(data.getLong("doesNotExist", 99)).isEqualTo(99);
  }

  @Test
  void getDecimal_doesNotExist() {
    assertThrows(IllegalStateException.class, () -> data.getDecimal("myTestDecimal.doesNotExist"));
  }

  @Test
  void getDecimal_default() {
    assertThat(data.getDecimal("myTestDecimal.doesNotExist", "10.4")).isEqualByComparingTo("10.4");
    data.setProperty("myTestDecimal.doesNotExist", null);
  }

  @Test
  void getDecimal() {
    data.setProperty("myTestDecimal", "14.3");
    assertThat(data.getDecimal("myTestDecimal")).isEqualByComparingTo("14.3");
    assertThat(data.getDecimal("myTestDecimal", "10.4")).isEqualByComparingTo("14.3");
    data.setProperty("myTestDecimal", null);
  }

  @Test
  void getURL_doesNotExist() {
    assertThrows(IllegalStateException.class, () -> data.getURL("myUrl.doesNotExist"));
  }

  @Test
  void getURL_doesNotExist_malformed() {
    assertThrows(IllegalStateException.class, () -> data.getURL("myUrl.doesNotExist", "junk"));
  }

  @Test
  void getURL_doesNotExist_malformed2() {
    data.setProperty("myUrl.invalid", "junk");
    assertThrows(IllegalStateException.class, () -> data.getURL("myUrl.invalid"));
  }

  @Test
  void getURL_default() throws MalformedURLException {
    assertThat(data.getURL("myUrl.doesNotExist", "http://foo")).isEqualTo(new URL("http://foo"));
    data.setProperty("myUrl.doesNotExist", null);
  }

  @Test
  void getURL() throws MalformedURLException {
    data.setProperty("myUrl", "http://bar");
    assertThat(data.getURL("myUrl")).isEqualTo(new URL("http://bar"));
    assertThat(data.getURL("myUrl", "http://baz")).isEqualTo(new URL("http://bar"));
    data.setProperty("myUrl", null);
  }

  @Test
  void getList() {
    assertThat(data.list().of("someValues")).contains("13", "42", "55");
    assertThat(data.list().of("someValues", "a", "b")).contains("13", "42", "55");
    assertThat(data.list().of("list.notThere", "a", "b")).contains("a", "b");
    assertThat(data.list().of("list.notThere2")).isEmpty();

    assertThat(data.list().ofInt("someValues")).contains(13, 42, 55);
    assertThat(data.list().ofInt("someValues", 22)).contains(13, 42, 55);
    assertThat(data.list().ofInt("list.int.notThere", 51, 52)).contains(51, 52);
    assertThat(data.list().ofInt("list.int.notThere2")).isEmpty();

    assertThat(data.list().ofLong("someValues")).contains(13L, 42L, 55L);
    assertThat(data.list().ofLong("someValues", 22L)).contains(13L, 42L, 55L);
    assertThat(data.list().ofLong("list.long.notThere", 51L, 52L)).contains(51L, 52L);
    assertThat(data.list().ofLong("list.long.notThere2")).isEmpty();
  }

  @Test
  void getSet() {
    assertThat(data.set().of("1someValues")).contains("13", "42", "55");
    assertThat(data.set().of("1someValues", "a", "b")).contains("13", "42", "55");
    assertThat(data.set().of("1set.notThere", "a", "b")).contains("a", "b");
    assertThat(data.set().of("1set.notThere2")).isEmpty();

    assertThat(data.set().ofInt("1someValues")).contains(13, 42, 55);
    assertThat(data.set().ofInt("1someValues", 22)).contains(13, 42, 55);
    assertThat(data.set().ofInt("1set.int.notThere", 51, 52)).contains(51, 52);
    assertThat(data.set().ofInt("1set.int.notThere2")).isEmpty();

    assertThat(data.set().ofLong("1someValues")).contains(13L, 42L, 55L);
    assertThat(data.set().ofLong("1someValues", 22L)).contains(13L, 42L, 55L);
    assertThat(data.set().ofLong("1set.long.notThere", 51L, 52L)).contains(51L, 52L);
    assertThat(data.set().ofLong("1set.long.notThere2")).isEmpty();
  }

  enum MyEnum {
    ONE, TWO, THREE
  }

  @Test
  void getEnum() {
    assertThat(data.getEnum(MyEnum.class, "myEnum")).isEqualTo(MyEnum.TWO);
    assertThat(data.getEnum(MyEnum.class, "myEnum2", MyEnum.ONE)).isEqualTo(MyEnum.ONE);
    assertThat(data.getEnum(MyEnum.class, "myEnum2", MyEnum.THREE)).isEqualTo(MyEnum.ONE);
  }

  @Test
  void getEnum_doesNotExist() {
    assertThrows(IllegalStateException.class, () -> data.getEnum(MyEnum.class, "myEnum.doesNotExist"));
  }

  @Test
  void onChange() {
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
  void setProperty_withEval() {
    assertThat(data.get("ThisIsNotSet", null)).isNull();
    data.setProperty("ThisIsNotSet", "A|${user.home}|B");
    String expected = "A|" + System.getProperty("user.home") + "|B";
    assertThat(data.get("ThisIsNotSet", null)).isEqualTo(expected);
  }

  @Test
  void onChangeInt() {
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
  void onChangeLong() {
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
  void onChangeBool() {
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
  void evalProperties() {
    final Properties properties = basicProperties();
    properties.setProperty("someA", "before-${foo.bar}-after");

    final CoreConfiguration config = new CoreConfiguration(new DefaultEventLog(), new Properties());
    final Properties copy = config.eval(properties);

    assertThat(copy.getProperty("someA")).isEqualTo("before-42-after");
  }

  @Test
  void evalModify() {
    final Properties properties = basicProperties();
    properties.setProperty("someA", "before-${foo.bar}-after");
    properties.setProperty("yeahNah", "before-${no-eval-for-this}-after");

    String beforeYeahNahValue = properties.getProperty("yeahNah");

    final CoreConfiguration config = new CoreConfiguration(new DefaultEventLog(), new Properties());
    config.evalModify(properties);

    String someAValue = properties.getProperty("someA");
    assertThat(someAValue).isEqualTo("before-42-after");

    String afterYeahNahValue = properties.getProperty("yeahNah");
    assertThat(beforeYeahNahValue).isSameAs(afterYeahNahValue);
  }
}
