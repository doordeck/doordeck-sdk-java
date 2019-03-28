package com.doordeck.sdk.http.service;

import com.doordeck.sdk.dto.certificate.CertificateChain;
import com.doordeck.sdk.dto.certificate.RegisterEphemeralKey;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CertificateService {

    @POST("auth/certificate")
    Call<CertificateChain> registerEphemeralKey(@Body RegisterEphemeralKey ephemeralKey);

}
