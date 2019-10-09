package com.fish.bin.test;

public class TestReg {
    public static void main(String[] args) {
        String arg = "R.layout.activity_main";
        String regex = "^R\\.layout\\.(\\w+)$";
        boolean matches = arg.matches(regex);
        System.out.println(matches);
    }
}
