package com.uanitteiru.http

import com.uanitteiru.http.responses.StringResponse
import io.quarkus.rest.client.reactive.NotBody
import jakarta.ws.rs.GET

import jakarta.ws.rs.Path
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient
interface Sso2Service {
    @GET
    @Path("/internal-api/get-long-living-token")
    @ClientHeaderParam(name = "Authorization", value = ["JWT {token}"])
    fun getLongLivingToken(@NotBody token: String): StringResponse
}