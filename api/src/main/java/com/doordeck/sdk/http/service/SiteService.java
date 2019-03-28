package com.doordeck.sdk.http.service;

import com.doordeck.sdk.dto.site.Site;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;
import java.util.UUID;

public interface SiteService {

    @GET("site")
    Call<List<Site>> getSites();

    @GET("site")
    Call<Site> getSite(@Path("siteId") UUID siteId);

}
