package io.avaje.config;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static io.avaje.config.InitialLoader.Source.RESOURCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InitialLoaderTest {

  @Test
  void load() {
    String userName = System.getProperty("user.name");
    String userHome = System.getProperty("user.home");

    InitialLoader loader = new InitialLoader(new DefaultEventLog());
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
  void loadWithExtensionCheck() {
    InitialLoader loader = new InitialLoader(new DefaultEventLog());
    loader.loadWithExtensionCheck("test-dummy.properties");
    loader.loadWithExtensionCheck("test-dummy.yml");
    loader.loadWithExtensionCheck("test-dummy2.yaml");

    Properties properties = loader.eval();
    assertThat(properties.getProperty("dummy.yaml.bar")).isEqualTo("baz");
    assertThat(properties.getProperty("dummy.yml.foo")).isEqualTo("bar");
    assertThat(properties.getProperty("dummy.properties.foo")).isEqualTo("bar");
  }

  @Test
  void loadYaml() {
    InitialLoader loader = new InitialLoader(new DefaultEventLog());
    loader.loadYaml("test-properties/foo.yml", RESOURCE);
    Properties properties = loader.eval();

    assertThat(properties.getProperty("Some.Other.pass")).isEqualTo("someDefault");
  }

  @Test
  void loadProperties() {
    System.setProperty("eureka.instance.hostname", "host1");
    System.setProperty("server.port", "9876");

    InitialLoader loader = new InitialLoader(new DefaultEventLog());
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
  void splitPaths() {
    InitialLoader loader = new InitialLoader(new DefaultEventLog());
    assertThat(loader.splitPaths("one two three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one,two,three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one;two;three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one two,three;four,five six")).contains("one", "two", "three", "four", "five", "six");
  }

  @Test
  void loadViaCommandLine_whenNotValid() {
    InitialLoader loader = new InitialLoader(new DefaultEventLog());
    loader.loadViaCommandLine(new String[]{"-p", "8765"});
    assertEquals(0, loader.size());
    loader.loadViaCommandLine(new String[]{"-port", "8765"});
    assertEquals(0, loader.size());

    loader.loadViaCommandLine(new String[]{"-port"});
    loader.loadViaCommandLine(new String[]{"-p", "ort"});
    assertEquals(0, loader.size());

    loader.loadViaCommandLine(new String[]{"-p", "doesNotExist.yaml"});
    assertEquals(0, loader.size());
  }

  @Test
  void loadViaCommandLine_localFile() {
    InitialLoader loader = new InitialLoader(new DefaultEventLog());
    loader.loadViaCommandLine(new String[]{"-p", "test-dummy2.yaml"});
    assertEquals(1, loader.size());
  }

  @Test
  void load_withSuppressTestResource() {
    //application-test.yaml is loaded when suppressTestResource is not set to true
    System.setProperty("suppressTestResource", "");
    InitialLoader loader = new InitialLoader(new DefaultEventLog());
    Properties properties = loader.load();
    assertThat(properties.getProperty("myapp.activateFoo")).isEqualTo("true");

    //application-test.yaml is not loaded when suppressTestResource is set to true
    System.setProperty("suppressTestResource", "true");
    InitialLoader loaderWithSuppressTestResource = new InitialLoader(new DefaultEventLog());
    Properties propertiesWithoutTestResource = loaderWithSuppressTestResource.load();
    assertThat(propertiesWithoutTestResource.getProperty("myapp.activateFoo")).isNull();
  }
}
