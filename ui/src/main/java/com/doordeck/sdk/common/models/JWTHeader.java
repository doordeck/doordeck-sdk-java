package com.doordeck.sdk.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;
import java.util.Locale;
import java.util.TimeZone;

//  JWT header
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JWTHeader {

    private String phone_number;
    private Boolean telephone_verified = false;
    private String email;
    private int exp;
    private String sub;
    private Boolean email_verified = false;
    private String session;
    private String name;
    private String given_name;
    private String family_name;
    private String middle_name;
    private String iss;
    private String aud;
    private boolean refresh;
    private int iat;
    private String sid;
    private Locale locale;
    private TimeZone zoneinfo;
    private long auth_time;
    private URI picture;


    public JWTHeader()
    {

    }

    public JWTHeader(String telephone, Boolean telephone_verified, String email, int exp, String sub, Boolean email_verified, String session, String name, String given_name, String family_name, String middle_name, String iss, String aud, boolean refresh, int iat, String sid, Locale locale, TimeZone zoneinfo, long auth_time, URI picture) {
        this.phone_number = telephone;
        this.telephone_verified = telephone_verified;
        this.email = email;
        this.exp = exp;
        this.sub = sub;
        this.email_verified = email_verified;
        this.session = session;
        this.name = name;
        this.given_name = given_name;
        this.family_name = family_name;
        this.middle_name = middle_name;
        this.iss = iss;
        this.aud = aud;
        this.refresh = refresh;
        this.iat = iat;
        this.sid = sid;
        this.locale = locale;
        this.zoneinfo = zoneinfo;
        this.auth_time = auth_time;
        this.picture = picture;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public Boolean getTelephone_verified() {
        return telephone_verified;
    }

    public void setTelephone_verified(Boolean telephone_verified) {
        this.telephone_verified = telephone_verified;
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

    public Boolean getEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(Boolean email_verified) {
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

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
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

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public TimeZone getZoneinfo() {
        return zoneinfo;
    }

    public void setZoneinfo(TimeZone zoneinfo) {
        this.zoneinfo = zoneinfo;
    }

    public long getAuth_time() {
        return auth_time;
    }

    public void setAuth_time(long auth_time) {
        this.auth_time = auth_time;
    }

    public URI getPicture() {
        return picture;
    }

    public void setPicture(URI picture) {
        this.picture = picture;
    }
}
