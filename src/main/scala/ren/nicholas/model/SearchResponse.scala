package ren.nicholas.model

import argonaut.Argonaut._
import argonaut._

case class SearchResponse(classifiedAnnouncements: Option[String],
                          totalSecurities: Int,
                          totalAnnouncement: Int,
                          totalRecordNum: Int,
                          announcements: List[Announcement],
                          categoryList: Option[String],
                          hasMore: Boolean
                         ) {}

object SearchResponse {
  implicit def SearchResponseCodecJson: CodecJson[SearchResponse] =
    casecodec7(SearchResponse.apply, SearchResponse.unapply)("classifiedAnnouncements",
      "totalSecurities",
      "totalAnnouncement",
      "totalRecordNum",
      "announcements",
      "categoryList",
      "hasMore"
    )

}