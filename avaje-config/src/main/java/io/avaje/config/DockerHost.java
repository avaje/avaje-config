package io.avaje.config;

import java.io.File;
import java.util.Locale;

/**
 * Support automatic translation of ${docker.host} detecting Docker-In-Docker and OS.
 */
final class DockerHost {

  static String host() {
    return !dind() ? "localhost" : dockerInDockerHost();
  }

  private static boolean dind() {
    return (new File("/.dockerenv")).exists();
  }

  private static String dockerInDockerHost() {
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return !os.contains("mac") && !os.contains("darwin") && !os.contains("win") ? "172.17.0.1" : "host.docker.internal";
  }

}
