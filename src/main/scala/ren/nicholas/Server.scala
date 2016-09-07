package ren.nicholas

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import ren.nicholas.actor.{AnnouncementFinder, Find, InputLoader, Start}

object Server extends App {
  implicit val timeout = akka.util.Timeout(1, TimeUnit.SECONDS)

  val system = ActorSystem("extractor")

  val loader: ActorRef = system.actorOf(Props[InputLoader])
  loader ! Start
}
