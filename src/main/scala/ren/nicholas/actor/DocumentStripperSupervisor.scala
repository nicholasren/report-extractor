package ren.nicholas.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, Props}
import ren.nicholas.actor.DocumentStripper.{NotMatchedFor, Strip, StripCompleted}


class DocumentStripperSupervisor extends Actor with ActorLogging {
  val annualAnnouncementsDir: File = new File("./data/annual-announcements")

  override def receive: Receive = {
    case DocumentStripper.Strip => {
      log.debug("Start striping")
      val files: Array[File] = annualAnnouncementsDir.listFiles()

      files.foreach(file => {
        context.actorOf(Props[DocumentStripper]) ! Strip(file)
      })

      context.become(ack(files))
    }
  }

  def ack(files: Array[File]): Receive = {
    case StripCompleted(file) => {
      log.debug(s"Strip completed for ${file.getName}")

      val remained: Array[File] = files.filterNot(_ == file)
      if (remained.isEmpty) {
        log.debug(s"All Strip completed")
        context.system.terminate()
      } else {
        context.become(ack(remained))
      }
    }
    case NotMatchedFor(file) => {
      log.warning(s"Not Matched for ${file.getName}")

      val remained: Array[File] = files.filterNot(_ == file)
      if (remained.isEmpty) {
        log.debug(s"All Strip completed")
        context.system.terminate()
      } else {
        context.become(ack(remained))
      }
    }
  }
}
