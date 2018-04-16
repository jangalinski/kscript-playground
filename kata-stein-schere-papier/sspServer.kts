#!/usr/bin/env kscript

@file:MavenRepository( "ktor", "http://dl.bintray.com/kotlin/ktor" )
@file:MavenRepository( "kotlinx", "http://dl.bintray.com/kotlin/kotlinx")
@file:DependsOnMaven( "io.ktor:ktor-server-netty:0.9.1")
@file:DependsOnMaven( "io.ktor:ktor-gson:0.9.1")
@file:DependsOnMaven( "org.slf4j:slf4j-simple:1.7.25")

import io.ktor.server.netty.*
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.server.engine.*
import io.ktor.gson.GsonConverter
import io.ktor.features.ContentNegotiation

enum class Choice { STONE, PAPER, SCISSORS }
enum class Result { WIN, DRAW, LOOSE }
data class Match(val player:Choice, val computer:Choice, val result:Result)

sealed class Message {
  data class PlayerChoice(val choice:Choice)
  data class ComputerChoice(val choice:Choice)
}

val matches = mutableListOf<Match>()

embeddedServer(Netty, 8888) {
  install(ContentNegotiation) {
      register(ContentType.Application.Json, GsonConverter())
  }
  routing {
    post("/matches") {
        val playerChoice  = call.receive<Message.PlayerChoice>()
        val computerChoice = Message.ComputerChoice(Choice.STONE)
        val match = Match(playerChoice.choice, computerChoice.choice, Result.WIN)
        matches += match
        call.respond(HttpStatusCode.OK, match)
    }
    get("/matches") {
      call.respond(matches)
    }
  }
}.start(wait = true)
