package ren.nicholas

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import ren.nicholas.actor.{DocLinkFinder, Find}

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends App {
  implicit val timeout = akka.util.Timeout(1, TimeUnit.SECONDS)

  val system = ActorSystem("extractor")
  val finder: ActorRef = system.actorOf(Props[DocLinkFinder])

  (finder ? Find).foreach(print)
}
