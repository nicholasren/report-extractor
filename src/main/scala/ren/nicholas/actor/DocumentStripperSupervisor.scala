package ren.nicholas.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, Props}


class DocumentStripperSupervisor extends Actor with ActorLogging {
  val annualAnnouncementsDir: File = new File("./data/annual-announcements")

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

  override def receive: Receive = {
    case Strip => {
      val files: Array[File] = annualAnnouncementsDir.listFiles()

      files.foreach(f => {
        context.actorOf(Props[DocumentStripper]) ! Strip(f)
      })
      context.become(ack(files))
    }
  }

}
