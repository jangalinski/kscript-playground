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

fun Choice.beats(): Array<Choice> = when (this) {
    Choice.STONE -> arrayOf(Choice.SCISSORS)
    Choice.SCISSORS -> arrayOf(Choice.PAPER)
    Choice.PAPER -> arrayOf(Choice.STONE)
}

infix fun Choice.against(computer: Choice): Result = when (computer) {
    this -> Result.DRAW
    in beats() -> Result.WIN
    else -> Result.LOOSE
}

sealed class Message() {
  abstract val choice:Choice

  data class PlayerChoice(override val choice:Choice) : Message()
  data class ComputerChoice(override val choice:Choice = Choice.values().toList().shuffled().last())  : Message()
}

fun play(player:Message.PlayerChoice, computer:Message.ComputerChoice) = Match(player.choice, computer.choice, player.choice against computer.choice)

val matches = mutableListOf<Match>()

embeddedServer(Netty, 8888) {
  install(ContentNegotiation) {
      register(ContentType.Application.Json, GsonConverter())
  }
  routing {
    post("/matches") {
        val playerChoice  = call.receive<Message.PlayerChoice>()
        val match = play(playerChoice, Message.ComputerChoice())
        matches += match
        call.respond(HttpStatusCode.OK, match)
    }
    get("/matches") {
      call.respond(matches)
    }
  }
}.start(wait = true)
