package ren.nicholas.actor

import akka.actor.Actor
import argonaut.Argonaut._
import org.apache.http.client.fluent.{Form, Request}
import ren.nicholas.model.SearchResponse

case object Find

class DocLinkFinder extends Actor {

  val url = "http://www.cninfo.com.cn/cninfo-new/announcement/query"

  override def receive: Receive = {
    case Find => sender() ! linksIn()
  }

  def linksIn(): List[String] = {
    val data = Form.form()
      .add("stock", "000002")
      .add("category", "category_ndbg_szsh")
      .add("pageNum", "1")
      .add("column", "szse_main")
      .add("tabName", "fulltext")
      .build()

    val response = Request.Post(url)
      .bodyForm(data)
      .execute().returnContent().asString()

    announcementsFrom(response)

    Nil
  }

  def announcementsFrom(response: String) = {
    response.decodeOption[SearchResponse]
  }
}
