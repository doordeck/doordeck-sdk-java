package com.doordeck.sdk.http.service;

import com.doordeck.sdk.dto.site.Site;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface SiteService {

    @GET("site")
    Call<List<Site>> getSites();

}
