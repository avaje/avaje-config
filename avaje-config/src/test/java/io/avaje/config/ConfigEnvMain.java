package io.avaje.config;

public class ConfigEnvMain {

  public static void main(String[] args) {

//    // initialise it
//    Config.init();
//
//    final String appName = System.getProperty("appName");
//    final String appInstanceId = System.getProperty("appInstanceId");

    System.out.println("--- 2");
    System.out.println("appName=" + Config.get("appName", null));
    System.out.println("appInstanceId=" + Config.get("appInstanceId", null));
    System.out.println("appEnvironment=" + Config.get("appEnvironment", null));
    System.out.println("appVersion=" + Config.get("appVersion", null));
    System.out.println("appIp=" + Config.get("appIp", null));
  }
}
