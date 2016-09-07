package ren.nicholas.model

import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.{Matchers, WordSpecLike}
import ren.nicholas.support.Files._

class JsonParseSpec extends WordSpecLike with Matchers {
  implicit val formats = DefaultFormats

  "Announcement" must {
    "be decoded from json" in {
      val string: JsonInput = asString("announcement.json")

      val announcement = parse(string).extract[Announcement]
      println(announcement)

      announcement should !==(None)
    }
  }

  "SearchResponse" must {
    "be decoded from json" in {
      val string: String = asString("response.json")


      val response: SearchResponse = parse(string).extract[SearchResponse]
      println(response)

      response should !==(None)
    }
  }
}
