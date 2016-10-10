package ren.nicholas

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import ren.nicholas.actor._

object Server extends App {
  implicit val timeout = akka.util.Timeout(1, TimeUnit.SECONDS)

  val system = ActorSystem("extractor")

//  val loader: ActorRef = system.actorOf(Props[InputLoader], "input-loader")
//
//  loader ! Start

  val extractor: ActorRef = system.actorOf(Props[DocumentStripperSupervisor], "extractor")

  extractor ! Strip
}
