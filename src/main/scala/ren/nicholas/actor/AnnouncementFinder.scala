package ren.nicholas.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.http.client.fluent.{Form, Request}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import ren.nicholas.model.{Announcement, SearchResponse}

class AnnouncementFinder extends Actor with ActorLogging {

  implicit val formats = DefaultFormats

  val url = "http://www.cninfo.com.cn/cninfo-new/announcement/query"

  override def receive: Receive = {
    case Find(stockNumber) => {
      val announcements: List[Announcement] = annualAnnouncement(stockNumber)
      log.debug(s"found announcements for $stockNumber")
      fireDownloadFor(stockNumber, announcements)
      context.become(ack(announcements.map(_.adjunctUrl)))
    }
  }

  def fireDownloadFor(stockNumber: String, announcements: List[Announcement]): Unit = {
    announcements.foreach {
      announcement => {
        val year: String = announcement.yearOfPublished
        val downloader: ActorRef = context.actorOf(Props[Downloader], s"$year-downloader")
        downloader ! Download(stockNumber, announcement)
      }
    }
  }

  def ack(uris: List[String]): Receive = {
    case DownloadCompleted(uri) => {
      val remains: List[String] = uris.filterNot(_ == uri)
      if (remains.isEmpty) {
        log.debug(s"Terminating... ")
        context.system.terminate()
      } else {
        context.become(ack(remains))
      }
    }
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
