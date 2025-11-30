package com.guilhermejose;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Config config;
        try {
            config = Config.load();
            System.out.println("Loaded configuration: " + config);
        } catch (Exception e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            return;
        }

    }
}
