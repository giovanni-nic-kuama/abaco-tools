package com.uanitteiru.commands

import com.uanitteiru.http.Sso2Service
import com.uanitteiru.utils.PrettyLogger
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder
import java.net.URI
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(name = "token-exchanger", mixinStandardHelpOptions = true)
class TokenExchanger : Runnable {
    @CommandLine.Option(names = ["-t", "--token"], description = ["Short Lived token"], defaultValue = "")
    private var token: String? = null

    private val prettyLogger = PrettyLogger()

    override fun run() {
        if (token.isNullOrEmpty()) {
            prettyLogger.printErrorAndExitMessages("Empty or null token")
            return
        }

        prettyLogger.printInfoMessage("Token found. Performing exchange")
        prettyLogger.printInfoMessage("")

        val sso2Service = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create("https://farm-coll.avepa.it/sso2"))
            .build(Sso2Service::class.java)

        val longLivingToken = sso2Service.getLongLivingToken(token!!)
        prettyLogger.printInfoMessage("New long living token retrieved. Here's your long living token: ")
        prettyLogger.printInfoMessage("")

        println(longLivingToken.value)

        prettyLogger.printSuccessMessage("")
    }
}