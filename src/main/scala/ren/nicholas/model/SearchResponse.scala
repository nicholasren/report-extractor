package ren.nicholas.model

import argonaut.Argonaut._
import argonaut._
import org.json4s.DefaultFormats

case class SearchResponse(classifiedAnnouncements: Option[String],
                          totalSecurities: Int,
                          totalAnnouncement: Int,
                          totalRecordNum: Int,
                          announcements: List[Announcement],
                          categoryList: Option[String],
                          hasMore: Boolean
                         ) {}

object SearchResponse {
  implicit val formats = DefaultFormats
}