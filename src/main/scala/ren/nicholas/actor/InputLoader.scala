package ren.nicholas.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import com.google.common.io.Resources
import ren.nicholas.actor.AnnouncementFinder.{Find, FindCompleted, NoAnnouncement}

import scala.io.Source

object InputLoader {

  case object Start

  case object Inspect

}


class InputLoader extends Actor with ActorLogging {
  val inputFileName: String = "trd_co.csv"

  override def receive: Receive = {
    case InputLoader.Start => {
      val lines: List[String] = Source.fromFile(Resources.getResource(inputFileName).toURI).getLines().drop(1).toList
      val stockNumbers: List[String] = lines.map(_.split(",")(1)).sorted

      for (stockNumber <- stockNumbers) {
        finderFor(stockNumber) ! Find
      }

      context.become(ack(stockNumbers))
    }
  }

  def finderFor(stockNumber: String): ActorRef = {
    val props: Props = Props(classOf[AnnouncementFinder], stockNumber)
    context.actorOf(props, finderNameFor(stockNumber))
  }

  def finderRefOf(stockNumber: String): ActorSelection = {
    context.actorSelection(s"akka://extractor/user/input-loader/${finderNameFor(stockNumber)}")
  }

  def finderNameFor(stockNumber: String): String = {
    s"finder-$stockNumber"
  }

  def ack(remains: List[String]): Receive = {
    case FindCompleted(stockNumber) => {
      handleFindCompleted(remains, stockNumber)
    }
    case NoAnnouncement(stockNumber) => {
      log.info(s"=======no announcement found for $stockNumber")
      handleFindCompleted(remains, stockNumber)
    }
    case InputLoader.Inspect => {
      for (stockNumber <- remains) {
        finderRefOf(stockNumber) ! InputLoader.Inspect
      }
    }
  }

  def handleFindCompleted(stockNumbers: List[String], stockNumber: String): Unit = {
    val remains: List[String] = stockNumbers.filterNot(_ == stockNumber)
    if (remains.isEmpty) {
      log.info("[===============All Completed!!!===============]")
      context.system.terminate()
    } else {
      context.become(ack(remains))
    }
  }
}
