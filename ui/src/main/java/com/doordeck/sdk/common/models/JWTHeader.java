package com.doordeck.sdk.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;

//  JWT header
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JWTHeader {

    private String phone_number;
    private String email;
    private int exp;
    private String sub;
    private String email_verified;
    private String session;
    private String name;
    private String iss;
    private String aud;
    private boolean refresh;
    private int iat;
    private String sid;

    public JWTHeader()
    {

    }

    public JWTHeader(String phone_number, String email, int exp, String sub, String email_verified, String session, String name, String iss, boolean refresh, int iat, String sid, String aud) {
        this.phone_number = phone_number;
        this.email = email;
        this.exp = exp;
        this.sub = sub;
        this.email_verified = email_verified;
        this.session = session;
        this.name = name;
        this.iss = iss;
        this.refresh = refresh;
        this.iat = iat;
        this.sid = sid;
        this.aud = aud;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(String email_verified) {
        this.email_verified = email_verified;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public int getIat() {
        return iat;
    }

    public void setIat(int iat) {
        this.iat = iat;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getAud() { return aud; }

    public void setAud(String aud) { this.aud = aud; }
}
