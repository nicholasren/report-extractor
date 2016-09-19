package ren.nicholas.actor

import java.io.{File, PrintWriter}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import org.apache.http.client.fluent.{Form, Request}
import org.json4s.jackson.Serialization.write
import org.json4s.native.JsonMethods._
import org.json4s.{DefaultFormats, _}
import ren.nicholas.model.{Announcement, SearchResponse}

import scala.io.Source

class AnnouncementFinder(val stockNumber: String) extends Actor with ActorLogging {

  implicit val formats = DefaultFormats

  val url = "http://www.cninfo.com.cn/cninfo-new/announcement/query"

  override def receive: Receive = {
    case Find => {
      log.debug(s"finding annual announcements for $stockNumber")
      val announcements: List[Announcement] = fetchAnnouncements

      if (announcements.isEmpty) {
        sender ! NoAnnouncement(stockNumber)
      } else {
        fireDownloadFor(announcements)
        context.become(ack(announcements))
      }
    }
  }

  def fetchAnnouncements: List[Announcement] = {
    val cacheFile = new File(s"./data/search-response/$stockNumber.json")
    if (cacheFile.exists()) {
      log.debug(s"load announcements from cache file for $stockNumber")
      loadFrom(cacheFile)
    } else {
      log.debug(s"load announcements from remote api for $stockNumber")
      val announcements: List[Announcement] = annualAnnouncement(stockNumber)
      persist(cacheFile, announcements)
      announcements
    }
  }

  def persist(persistedFile: File, announcements: List[Announcement]): Unit = {
    val writer: PrintWriter = new PrintWriter(persistedFile)
    writer.write(write(announcements))
    writer.flush()
    writer.close()
  }

  def loadFrom(announcementFile: File): List[Announcement] = {
    val string: String = Source.fromFile(announcementFile).getLines().mkString
    parse(string).extract[List[Announcement]]
  }

  def fireDownloadFor(announcements: List[Announcement]): Unit = {
    announcements.foreach {
      announcement => {
        val year: Option[String] = announcement.yearOfPublished
        if (year.isEmpty) {
          log.warning(s"Can not find year of published for ${announcement.announcementTitle}")
        }
        val downloader: ActorRef = context.actorOf(Props[Downloader], downloaderNameFor(stockNumber, year, announcement.announcementId))
        downloader ! Download(stockNumber, announcement)
      }
    }
  }

  def downloaderNameFor(stockNumber: String, year: Option[String], announcementId: String): String = {
    s"$stockNumber-${year.getOrElse("unknow")}-$announcementId-downloader"
  }

  def ack(announcements: List[Announcement]): Receive = {
    case DownloadCompleted(stock, announcement) => {
      val remains: List[Announcement] = announcements.filterNot(_ == announcement)
      if (remains.isEmpty) {
        log.info(s"Download completed for $stock")
        inputLoader ! FindCompleted(stock)
      } else {
        context.become(ack(remains))
      }
    }
    case Inspect => {
      if (announcements.isEmpty) {
        sender() ! FindCompleted(stockNumber)
      }
      log.info(s"Remained ${announcements.map(_.announcementTime)}")
    }
  }

  def inputLoader: ActorSelection = {
    context.actorSelection("akka://extractor/user/input-loader")
  }

  def annualAnnouncement(stockNumber: String, pageNumber: String = "1"): List[Announcement] = {
    val searchResponse: SearchResponse = search(stockNumber, pageNumber)
    searchResponse.announcements.filter(isAnnualReport)
  }

  def search(stock: String, pageNum: String): SearchResponse = {
    val data = Form.form()
      .add("stock", stock)
      .add("category", "category_ndbg_szsh")
      .add("pageNum", pageNum)
      .add("column", "szse_main")
      .add("tabName", "fulltext")
      .build()

    val response = Request.Post(url)
      .bodyForm(data)
      .execute().returnContent().asString()

    val searchResponse: SearchResponse = parse(response).extract[SearchResponse]
    searchResponse
  }

  def isAnnualReport: (Announcement) => Boolean = _.announcementTitle.endsWith("年度报告")

}
