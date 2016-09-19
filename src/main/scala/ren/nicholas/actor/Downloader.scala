package ren.nicholas.actor

import java.io.{File, FileOutputStream}
import java.net.URL
import java.nio.channels.Channels

import akka.actor.{Actor, ActorLogging}
import ren.nicholas.model.Announcement

import scala.util.{Failure, Success, Try}

class Downloader extends Actor with ActorLogging {
  val prefix = "http://www.cninfo.com.cn"

  override def receive: Receive = {
    case Download(stockNumber, announcement) => {
      val filename = s"$stockNumber-${announcement.announcementTitle}"
      val targetFile = new File(s"./data/annual-announcements/$filename.pdf")

      if (targetFile.exists()) {
        log.debug(s"Skip already downloaded $filename")
      } else {
        log.debug(s"Downloading announcement for $filename")
        val transferred: Try[Long] = Try(download(downloadUrlOf(announcement), targetFile))

        transferred match {
          case Success(_) => log.debug(s"Completed download of $filename")
          case Failure(e) => log.error(s"Download failed announcement for $filename, ${e.getMessage}")
        }
      }
      sender() ! DownloadCompleted(stockNumber, announcement)
    }
  }

  def download(url: URL, file: File): Long = {
    val in = Channels.newChannel(url.openStream())
    val out = new FileOutputStream(file)
    try {
      out.getChannel.transferFrom(in, 0, Long.MaxValue)
    } finally {
      in.close()
      out.close()
    }
  }

  def downloadUrlOf(announcement: Announcement): URL = new URL(s"$prefix/${announcement.adjunctUrl}")
}
