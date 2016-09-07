package ren.nicholas.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.google.common.io.Resources

import scala.io.Source

class InputLoader extends Actor with ActorLogging {
  val inputFileName: String = "trd_co.csv"

  override def receive: Receive = {
    case Start =>
      val lines: Iterator[String] = Source.fromFile(Resources.getResource(inputFileName).toURI).getLines().drop(1)
      val stockNumbers: Iterator[String] = lines.map(_.split(",")(1))
      stockNumbers.foreach {
        stockNumber => {
          log.debug(s"finding annual announcement for $stockNumber")
          context.actorOf(Props[AnnouncementFinder]) ! Find(stockNumber)
        }
      }
  }
}
