package com.example.destinyitemrandomizer.destinywrapper;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.File;
import java.io.IOException;

public class ManifestExtractor {

    public static void reduceManifest(File source) {
        // Do some Json parsing here
        try {
            JsonFactory jsonfactory = new JsonFactory();

            JsonParser parser = jsonfactory.createJsonParser(source);

            // starting parsing of JSON String
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String token = parser.getCurrentName();

                if ("DestinyInventoryBucketDefinition".equals(token)) {
                    parser.nextToken();//next token contains value
                    String fname = parser.getText();//getting text field
                    System.out.println("firstname : " + fname);
                }

/*                    if ("lastname".equals(token)) {
                        parser.nextToken();
                        String lname = parser.getText();
                        System.out.println("lastname : " + lname);
                    }

                    if ("phone".equals(token)) {
                        parser.nextToken();
                        int phone = parser.getIntValue();// getting numeric field System.out.println("phone : " + phone);
                    }

                    if ("address".equals(token)) {
                        System.out.println("address :");
                        parser.nextToken(); // next token will be '[' which means JSON array
                        // parse tokens until you find ']'
                        while (parser.nextToken() != org.codehaus.jackson.JsonToken.END_ARRAY) {
                            System.out.println(parser.getText());
                        }
                    }*/
            }

            parser.close();
        }
        catch (JsonGenerationException jge) {
            jge.printStackTrace();
        } catch (JsonMappingException jme) {
            jme.printStackTrace();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }

    }
}
