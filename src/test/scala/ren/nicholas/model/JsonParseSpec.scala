package ren.nicholas.model

import argonaut.Argonaut._
import org.scalatest.{Matchers, WordSpecLike}
import ren.nicholas.support.Files._

import scalaz.\/

class JsonParseSpec extends WordSpecLike with Matchers {

  "Announcement" must {
    "be decoded from json" in {
      val announcement: Option[Announcement] = asString("announcement.json").decodeOption[Announcement]
      announcement should !==(None)
    }
  }

  "SearchResponse" must {
    "be decoded from json" in {
      val string: String = asString("response.json")
      val searchResponse: \/[String, SearchResponse] = string.decodeEither[SearchResponse]

      println(searchResponse)
      searchResponse should !==(None)
    }
  }
}
