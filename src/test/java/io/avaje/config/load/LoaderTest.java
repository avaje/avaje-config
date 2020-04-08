package io.avaje.config.load;

import org.junit.Test;

import java.util.Properties;

import static io.avaje.config.load.Loader.Source.RESOURCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class LoaderTest {

  @Test
  public void load() {

    String userName = System.getProperty("user.name");
    String userHome = System.getProperty("user.home");

    Loader loader = new Loader();
    loader.loadProperties("test-properties/application.properties", RESOURCE);
    loader.loadYaml("test-properties/application.yaml", RESOURCE);

    loader.loadProperties("test-properties/one.properties", RESOURCE);
    loader.loadYaml("test-properties/foo.yml", RESOURCE);

    Properties properties = loader.eval();

    assertEquals("fromProperties", properties.getProperty("app.fromProperties"));
    assertEquals("Two", properties.getProperty("app.two"));

    assertEquals("bart", properties.getProperty("eval.withDefault"));
    assertEquals(userName, properties.getProperty("eval.name"));
    assertEquals(userHome + "/after", properties.getProperty("eval.home"));

    assertEquals("before|Beta|after", properties.getProperty("someOne"));
    assertEquals("before|Two|after", properties.getProperty("someTwo"));
  }

  @Test
  public void loadWithExtensionCheck() {

    Loader loader = new Loader();
    loader.loadFileWithExtensionCheck("test-dummy.properties");
    loader.loadFileWithExtensionCheck("test-dummy.yml");
    loader.loadFileWithExtensionCheck("test-dummy2.yaml");

    Properties properties = loader.eval();
    assertThat(properties.getProperty("dummy.yaml.bar")).isEqualTo("baz");
    assertThat(properties.getProperty("dummy.yml.foo")).isEqualTo("bar");
    assertThat(properties.getProperty("dummy.properties.foo")).isEqualTo("bar");
  }


  @Test
  public void loadYaml() {

    Loader loader = new Loader();
    loader.loadYaml("test-properties/foo.yml", RESOURCE);
    Properties properties = loader.eval();

    assertThat(properties.getProperty("Some.Other.pass")).isEqualTo("someDefault");
  }

  @Test
  public void loadProperties() {

    System.setProperty("eureka.instance.hostname", "host1");
    System.setProperty("server.port","9876");

    Loader loader = new Loader();
    loader.loadProperties("test-properties/one.properties", RESOURCE);
    Properties properties = loader.eval();

    assertThat(properties.getProperty("hello")).isEqualTo("there");
    assertThat(properties.getProperty("name")).isEqualTo("Rob");
    assertThat(properties.getProperty("statusPageUrl")).isEqualTo("https://host1:9876/status");
    assertThat(properties.getProperty("statusPageUrl2")).isEqualTo("https://aaa:9876/status2");
    assertThat(properties.getProperty("statusPageUrl3")).isEqualTo("https://aaa:89/status3");
    assertThat(properties.getProperty("statusPageUrl4")).isEqualTo("https://there:9876/name/Rob");
  }

  @Test
  public void splitPaths() {
    Loader loader = new Loader();
    assertThat(loader.splitPaths("one two three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one,two,three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one;two;three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one two,three;four,five six")).contains("one", "two", "three", "four", "five", "six");
  }
}
