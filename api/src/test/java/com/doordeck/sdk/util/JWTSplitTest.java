package com.doordeck.sdk.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import org.junit.Test;

import static com.google.common.base.Strings.lenientFormat;

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
