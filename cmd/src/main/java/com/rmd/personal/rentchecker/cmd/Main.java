package com.rmd.personal.rentchecker.cmd;

import com.rmd.personal.rentchecker.common.Connector;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public final class Main {

    private static final String USERNAME_KEY = "-u";
    private static final String PASSWORD_KEY = "-p";

    private Main() {
    }

    public static void main(String[] args) {
        String username = null;
        String password = null;
        for (int i = 0; i < args.length; i++) {
            if (USERNAME_KEY.equals(args[i])) {
                if (i == args.length - 1 || args[i + 1] == null || args[i + 1].isEmpty()) {
                    throw new IllegalArgumentException("username cannot be empty if " + USERNAME_KEY + " is provided");
                } else {
                    username = args[i + 1];
                    i++;
                }
            }
            if (PASSWORD_KEY.equals(args[i])) {
                if (i == args.length - 1 || args[i + 1] == null || args[i + 1].isEmpty()) {
                    throw new IllegalArgumentException("password cannot be empty if " + PASSWORD_KEY + " is provided");
                } else {
                    password = args[i + 1];
                    i++;
                }
            }
        }
        new Main().run(username, password);
    }


    // This is probably bad as the password is a String.
    // Not sure what we can do about this though as we need it in string form to add to the headers map.
    private void run(String username, String password) {
        Connector connector = new Connector();

        if (username == null) {
            System.out.print("Enter username: ");
            username = System.console().readLine();
        }

        if (password == null) {
            System.out.print("Enter password: ");
            password = String.valueOf(System.console().readPassword());
        }

        System.out.print("Creating initial HTTP entity... ");
        HttpEntity<?> entity = connector.createInitialHttpEntity(username, password);
        System.out.println("ok");

        System.out.print("Performing login... ");
        ResponseEntity<String> pageEntity = connector.performLogin(entity);
        System.out.println("ok");

        System.out.print("Adding login cookie and 'referer' to HTTP entity... ");
        HttpHeaders headers = connector.addRefererToCopyOfHttpHeaders(entity.getHeaders());
        entity = connector.addCookiesToPageEntityWithCopyOfHttpHeaders(pageEntity, headers);
        System.out.println("ok");

        System.out.print("Getting homepage... ");
        pageEntity = connector.getHomepage(entity);
        System.out.println("ok");

        System.out.print("Scraping homepage for rent... ");
        float owed = connector.scrapePageEntityForRent(pageEntity);
        System.out.println("ok");

        System.out.println("Amount owed: $" + owed);
    }
}
