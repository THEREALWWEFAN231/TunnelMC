package com.starkbank.ellipticcurve.utils;

import java.io.IOException;
import java.nio.file.*;

/*
 *This class handles the fileinput types as filename string
 *If using bytearray file as "signatureBinary.txt"
 *Use the readByte method
 **/
public class File {

    /**
     *
     * @param fileName
     * @return
     */
    public static String read(String fileName)
    {
        String content = "";
        try
        {
            content = new String(Files.readAllBytes(Paths.get(fileName)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return content;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static byte[] readBytes(String fileName)
    {
        byte[] content = null;
        try
        {
            content = Files.readAllBytes(Paths.get(fileName));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return content;
    }

}