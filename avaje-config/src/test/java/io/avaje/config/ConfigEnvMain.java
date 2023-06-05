package io.avaje.config;

public class ConfigEnvMain {

  public static void main(String[] args) {

//    // initialise it
//    Config.init();
//
//    final String appName = System.getProperty("appName");
//    final String appInstanceId = System.getProperty("appInstanceId");

    System.out.println("--- 2");
    System.out.println("appName=" + Config.getNullable("appName"));
    System.out.println("appInstanceId=" + Config.getNullable("appInstanceId"));
    System.out.println("appEnvironment=" + Config.getNullable("appEnvironment"));
    System.out.println("appVersion=" + Config.getNullable("appVersion"));
    System.out.println("appIp=" + Config.getNullable("appIp"));
  }
}
