package com.uanitteiru

import com.uanitteiru.commands.BundleDeployer
import com.uanitteiru.commands.KubeDeployer
import com.uanitteiru.commands.TokenExchanger
import io.quarkus.picocli.runtime.annotations.TopCommand
import picocli.CommandLine.Command


@TopCommand
@Command(mixinStandardHelpOptions = true, subcommands = [KubeDeployer::class, BundleDeployer::class, TokenExchanger::class])
class EntryCommand
