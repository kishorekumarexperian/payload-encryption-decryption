package com.sample.crypto;

import java.nio.file.Files;
import java.nio.file.Paths;

public class EncDec {

    public static void main(String[] args) throws Exception {
        if ( args.length < 2 ) {
            System.out.println("Must have 2 arguments");
            showArgs();
            return;
        }
        if ( !( args[0].equalsIgnoreCase("c2s") ||  args[0].equalsIgnoreCase("s2c") || args[1].length() < 1  ) ) {
            showArgs ();
        }

        String content = new String ( Files.readAllBytes( Paths.get(args[1]) ) );
        String encryptOrDecrypt = args[0];


        if ( encryptOrDecrypt.equalsIgnoreCase("c2s") ) {
            System.out.println("client encrypting content " + content);
            String encrypted = Util.encrypt(content, "client");
            System.out.println("\nServer sees encrypted payload as " + encrypted);
            String decrypted = Util.decrypt(encrypted, "server");
            System.out.println("\nServer sees decrypted payload as " + decrypted);

        }
        else {
            System.out.println("server encrypting content " + content );
            String encrypted = Util.encrypt(content, "server");
            System.out.println(encrypted);
            System.out.println("\nClient sees encrypted payload as " + encrypted);
            String decrypted = Util.decrypt(encrypted, "client");
            System.out.println("\nClient sees decrypted payload as " + decrypted);
        }
    }

    private static void showArgs () {
        String usage = " java -jar enc-dec.jar <c2s|s2c>  <File path to be encrypted> c2s will simulate client to server leg and s2c will simulate server to client leg ";
        System.out.println(usage);
    }
}
