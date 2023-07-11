package io.avaje.config;

import org.junit.jupiter.api.Test;

import static io.avaje.config.InitialLoader.Source.RESOURCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InitialLoaderTest {

  private static InitialLoader newInitialLoader() {
    return new InitialLoader(new DefaultConfigurationLog(), new DefaultResourceLoader());
  }

  @Test
  void load() {
    String userName = System.getProperty("user.name");
    String userHome = System.getProperty("user.home");

    InitialLoader loader = newInitialLoader();
    loader.loadProperties("test-properties/application.properties", RESOURCE);
    loader.loadYamlPath("test-properties/application.yaml", RESOURCE);

    loader.loadProperties("test-properties/one.properties", RESOURCE);
    loader.loadYamlPath("test-properties/foo.yml", RESOURCE);

    var properties = loader.eval();

    assertEquals("fromProperties", properties.get("app.fromProperties").value());
    assertEquals("Two", properties.get("app.two").value());

    assertEquals("bart", properties.get("eval.withDefault").value());
    assertEquals(userName, properties.get("eval.name").value());
    assertEquals(userHome + "/after", properties.get("eval.home").value());

    assertEquals("before|Beta|after", properties.get("someOne").value());
    assertEquals("before|Two|after", properties.get("someTwo").value());
  }

  @Test
  void loadWithExtensionCheck() {
    InitialLoader loader = newInitialLoader();
    loader.loadWithExtensionCheck("test-dummy.properties");
    loader.loadWithExtensionCheck("test-dummy.yml");
    loader.loadWithExtensionCheck("test-dummy2.yaml");

    var properties = loader.eval();
    assertThat(properties.get("dummy.yaml.bar").value()).isEqualTo("baz");
    assertThat(properties.get("dummy.yml.foo").value()).isEqualTo("bar");
    assertThat(properties.get("dummy.properties.foo").value()).isEqualTo("bar");
  }

  @Test
  void loadYaml() {
    InitialLoader loader = newInitialLoader();
    loader.loadYamlPath("test-properties/foo.yml", RESOURCE);
    var properties = loader.eval();

    assertThat(properties.get("Some.Other.pass").value()).isEqualTo("someDefault");
  }

  @Test
  void loadProperties() {
    System.setProperty("eureka.instance.hostname", "host1");
    System.setProperty("server.port", "9876");

    InitialLoader loader = newInitialLoader();
    loader.loadProperties("test-properties/one.properties", RESOURCE);
    var properties = loader.eval();

    assertThat(properties.get("hello").source()).isEqualTo("resource:test-properties/one.properties");
    assertThat(properties.get("hello").value()).isEqualTo("there");
    assertThat(properties.get("name").value()).isEqualTo("Rob");
    assertThat(properties.get("statusPageUrl").value()).isEqualTo("https://host1:9876/status");
    assertThat(properties.get("statusPageUrl2").value()).isEqualTo("https://aaa:9876/status2");
    assertThat(properties.get("statusPageUrl3").value()).isEqualTo("https://aaa:89/status3");
    assertThat(properties.get("statusPageUrl4").value()).isEqualTo("https://there:9876/name/Rob");
    assertThat(properties.get("sameFileEval.4").value()).isEqualTo("somethin1-2-3-4");
    assertThat(properties.get("sameFileEval.3").value()).isEqualTo("somethin1-2-3");
    assertThat(properties.get("sameFileEval.2").value()).isEqualTo("somethin1-2");
    assertThat(properties.get("sameFileEval.1").value()).isEqualTo("somethin1");
    assertThat(properties.get("asameFileEval.0").value()).isEqualTo("somethin1-2-3-afour");
    assertThat(properties.get("zsameFileEval.0").value()).isEqualTo("somethin1-2-3-zfour");
    assertThat(properties.get("zsameFileCombo").value()).isEqualTo("A|somethin1-2|somethin1-2-3|B");
    assertThat(properties.get("someOne").value()).isEqualTo("before|${app.one}|after");
    assertThat(properties.get("someOne2").value()).isEqualTo("Bef|before|${app.one}|after|Aft");
  }

  @Test
  void splitPaths() {
    InitialLoader loader = newInitialLoader();
    assertThat(loader.splitPaths("one two three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one,two,three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one;two;three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one two,three;four,five six")).contains("one", "two", "three", "four", "five", "six");
  }

  @Test
  void loadViaCommandLine_whenNotValid() {
    InitialLoader loader = newInitialLoader();
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
    InitialLoader loader = newInitialLoader();
    loader.loadViaCommandLine(new String[]{"-p", "test-dummy2.yaml"});
    assertEquals(1, loader.size());
  }

  @Test
  void load_withSuppressTestResource() {
    //application-test.yaml is loaded when suppressTestResource is not set to true
    try {
      System.setProperty("suppressTestResource", "");
      InitialLoader loader = newInitialLoader();
      var properties = loader.load();
      assertThat(properties.get("myapp.activateFoo").value()).isEqualTo("true");

      //application-test.yaml is not loaded when suppressTestResource is set to true
      System.setProperty("suppressTestResource", "true");
      InitialLoader loaderWithSuppressTestResource = newInitialLoader();
      var propertiesWithoutTestResource = loaderWithSuppressTestResource.load();
      assertThat(propertiesWithoutTestResource.get("myapp.activateFoo")).isNull();
    } finally {
      System.clearProperty("suppressTestResource");
    }
  }
}
