package org.example;

import io.avaje.config.Config;

public class Main {

    public static void main(String[] args) {
      // System.getProperties().list(System.out);
      // System.out.println("---");
      String profiles = System.getProperty("avaje.profiles");
      System.out.println("avaje.profiles=" + profiles);
      System.out.println("hello.world - " + Config.get("hello.world", "not-set"));
      System.out.println("admin - " + Config.get("admin", "not-set"));
      System.out.println("common - " + Config.get("common", "not-set"));
    }
}
