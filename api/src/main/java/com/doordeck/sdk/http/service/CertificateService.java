package com.doordeck.sdk.http.service;

import com.doordeck.sdk.dto.certificate.CertificateChain;
import com.doordeck.sdk.dto.certificate.RegisterEphemeralKey;
import com.doordeck.sdk.dto.certificate.VerificationMethod;
import com.doordeck.sdk.dto.certificate.VerificationRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CertificateService {

    @POST("auth/certificate")
    Call<CertificateChain> registerEphemeralKey(@Body RegisterEphemeralKey ephemeralKey);

    @POST("auth/certificate/verify")
    Call<CertificateChain> initVerification(@Body RegisterEphemeralKey ephemeralKey, @Query("method") VerificationMethod method);

    @POST("auth/certificate/check")
    Call<CertificateChain> attemptVerification(@Body VerificationRequest verificationRequest);

}
