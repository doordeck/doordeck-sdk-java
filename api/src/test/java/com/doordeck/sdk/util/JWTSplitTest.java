package com.doordeck.sdk.util;

import org.junit.Test;

public class JWTSplitTest {

    private String jwt = "";

    @Test
    public void splitJWT() {
        try{
            String val = jwt.substring(jwt.indexOf(".")+1, jwt.lastIndexOf("."));
            System.out.println(val);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Incorrect JWT TOKEN");
        }
    }

}
