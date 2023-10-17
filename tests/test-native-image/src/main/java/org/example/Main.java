package org.example;

import io.avaje.config.Config;

public class Main {

    public static void main(String[] args) {
        String val = Config.get("hello.world", "not-set");
        System.out.println("Hello - " + val);
    }
}
