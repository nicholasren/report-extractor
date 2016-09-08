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
      val url = new URL(downloadUrlOf(announcement))
      val file = new File(s"./data/output/$filename.pdf")

      if (file.exists()) {
        log.debug(s"Skip already downloaded $filename")
      } else {
        log.debug(s"Downloading announcement for $filename")
        val transferred: Try[Long] = Try(download(url, file))

        transferred match {
          case Success(length) => log.debug(s"Completed download of $filename")
          case Failure(e) => log.error(e, s"Download failed announcement for $filename")
        }

        sender() ! DownloadCompleted(stockNumber, announcement)
      }
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

  def downloadUrlOf(announcement: Announcement): String = s"$prefix/${announcement.adjunctUrl}"
}
