package ren.nicholas.actor

import java.io.{File, PrintWriter}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import org.apache.http.client.fluent.{Form, Request}
import org.json4s.jackson.Serialization.write
import org.json4s.native.JsonMethods._
import org.json4s.{DefaultFormats, _}
import ren.nicholas.model.{Announcement, SearchResponse}

import scala.io.Source

class AnnouncementFinder extends Actor with ActorLogging {

  implicit val formats = DefaultFormats

  val url = "http://www.cninfo.com.cn/cninfo-new/announcement/query"

  override def receive: Receive = {
    case Find(stockNumber) => {
      log.debug(s"finding annual announcements for $stockNumber")
      val announcements: List[Announcement] = announcementFor(stockNumber)
      log.debug(s"found announcements for $stockNumber")

      fireDownloadFor(stockNumber, announcements)
      context.become(ack(announcements.map(_.adjunctUrl)))
    }
  }

  def announcementFor(stockNumber: String): List[Announcement] = {
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

  def fireDownloadFor(stockNumber: String, announcements: List[Announcement]): Unit = {
    announcements.foreach {
      announcement => {
        val year: String = announcement.yearOfPublished
        val downloader: ActorRef = context.actorOf(Props[Downloader], s"$stockNumber-$year-downloader")
        downloader ! Download(stockNumber, announcement)
      }
    }
  }

  def ack(uris: List[String]): Receive = {
    case DownloadCompleted(stockNumber, announcement) => {
      val remains: List[String] = uris.filterNot(_ == announcement.adjunctUrl)
      if (remains.isEmpty) {
        log.debug(s"Download completed for $stockNumber")
        inputLoader ! FindCompleted(stockNumber)
      } else {
        context.become(ack(remains))
      }
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
