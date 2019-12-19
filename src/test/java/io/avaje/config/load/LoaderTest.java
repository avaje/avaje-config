package io.avaje.config.load;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class LoaderTest {

  @Test
  public void load() {

    String userName = System.getProperty("user.name");
    String userHome = System.getProperty("user.home");

    Loader loader = new Loader();
    loader.loadProperties("test-properties/application.properties", Loader.Source.RESOURCE);
    loader.loadYaml("test-properties/application.yaml", Loader.Source.RESOURCE);

    loader.loadProperties("test-properties/one.properties", Loader.Source.RESOURCE);
    loader.loadYaml("test-properties/foo.yml", Loader.Source.RESOURCE);

    Properties properties = loader.eval();

    assertEquals("fromProperties", properties.getProperty("app.fromProperties"));
    assertEquals("Two", properties.getProperty("app.two"));

    assertEquals("bart", properties.getProperty("eval.withDefault"));
    assertEquals(userName, properties.getProperty("eval.name"));
    assertEquals(userHome + "/after", properties.getProperty("eval.home"));
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
    loader.loadYaml("test-properties/foo.yml", Loader.Source.RESOURCE);
    Properties properties = loader.eval();

    assertThat(properties.getProperty("Some.Other.pass")).isEqualTo("someDefault");
  }

  @Test
  public void loadProperties() {

    Loader loader = new Loader();
    loader.loadProperties("test-properties/one.properties", Loader.Source.RESOURCE);
    Properties properties = loader.eval();

    assertThat(properties.getProperty("hello")).isEqualTo("there");
    assertThat(properties.getProperty("name")).isEqualTo("Rob");
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
