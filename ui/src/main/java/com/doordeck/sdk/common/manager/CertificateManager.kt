package com.doordeck.sdk.common.manager

import com.doordeck.sdk.dto.certificate.CertificateChain
import com.doordeck.sdk.dto.certificate.ImmutableRegisterEphemeralKey
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.PublicKey


/**
 * Singleton responsible to get the certificate chains of the user and verify it's auth status
 */
object CertificateManager {

    /**
     * Call the server with a generated eph key associated to the user get it's certificate chain
     * The user might needs to verify its device.
     * If the server responds with a 423, the user needs to verify its device with a 2 FA
     * @param publicKey public key of the user
     */
    fun getCertificatesAsync(publicKey: PublicKey) {
        val ephKey = ImmutableRegisterEphemeralKey.builder().ephemeralKey(publicKey).build()
        val request = Doordeck.client!!.certificateService().registerEphemeralKey(ephKey)
        request.enqueue(object : Callback<CertificateChain> {
            override fun onResponse(call: Call<CertificateChain>, response: Response<CertificateChain>) {
                Doordeck.certificateChain = response.body()
                Doordeck.storeCertificates(Doordeck.certificateChain!!)
                if (response.code() == 423)
                    Doordeck.status = AuthStatus.TWO_FACTOR_AUTH_NEEDED
                else
                    Doordeck.status = AuthStatus.AUTHORIZED
            }

            override fun onFailure(call: Call<CertificateChain>, t: Throwable) {
                Doordeck.status = AuthStatus.UNAUTHORIZED
            }
        })
    }

}