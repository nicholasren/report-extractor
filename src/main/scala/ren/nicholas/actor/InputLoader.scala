package ren.nicholas.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.google.common.io.Resources

import scala.io.Source

class InputLoader extends Actor with ActorLogging {
  val inputFileName: String = "trd_co.csv"

  override def receive: Receive = {
    case Start => {
      val lines: List[String] = Source.fromFile(Resources.getResource(inputFileName).toURI).getLines().drop(1).toList
      val stockNumbers: List[String] = lines.map(_.split(",")(1)).sorted

      stockNumbers.foreach {
        stockNumber => {
          context.actorOf(Props[AnnouncementFinder]) ! Find(stockNumber)
        }
      }

      context.become(ack(stockNumbers))
    }
  }

  def ack(stockNumbers: List[String]): Receive = {
    case FindCompleted(stockNumber) => {
      val remains: List[String] = stockNumbers.filterNot(_ == stockNumber)
      if (remains.isEmpty) {
        log.debug("[===============All Completed!!!===============]")
        context.system.terminate();
      } else {
        context.become(ack(remains))
      }
    }
  }
}
