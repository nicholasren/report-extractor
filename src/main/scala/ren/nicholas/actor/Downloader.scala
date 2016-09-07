package ren.nicholas.actor

import java.io.{File, FileOutputStream}
import java.net.URL
import java.nio.channels.Channels

import akka.actor.{Actor, ActorLogging}
import ren.nicholas.model.Announcement

class Downloader extends Actor with ActorLogging {
  val prefix = "http://www.cninfo.com.cn"

  override def receive: Receive = {
    case Download(stockNumber, announcement) => {
      log.debug(s"Downloading announcement for ${announcement.announcementTitle}")

      val url = new URL(downloadUrlOf(announcement))
      val file = new File(targetFilePathOf(stockNumber, announcement))
      val in = Channels.newChannel(url.openStream())
      val out = new FileOutputStream(file)

      try {
        out.getChannel.transferFrom(in, 0, Long.MaxValue)
      } finally {
        in.close()
        out.close()
      }

      log.debug(s"Completed download of ${announcement.announcementTitle}")
      sender() ! DownloadCompleted(announcement.adjunctUrl)
    }
  }

  def downloadUrlOf(announcement: Announcement): String = s"$prefix/${announcement.adjunctUrl}"

  def targetFilePathOf(stockNumber: String, announcement: Announcement): String = s"./data/output/$stockNumber-${announcement.announcementTitle}.pdf"

}
