package ren.nicholas.model

import org.json4s.DefaultFormats

case class Announcement(id: Option[String],
                        secCode: String,
                        secName: String,
                        orgId: String,
                        announcementId: String,
                        announcementTitle: String,
                        announcementTime: String,
                        adjunctUrl: String,
                        adjunctSize: Int,
                        adjunctType: String,
                        storageTime: String,
                        columnId: String,
                        pageColumn: String,
                        announcementType: String,
                        associateAnnouncement: Option[String],
                        important: Option[String],
                        batchNum: Option[String],
                        announcementContent: Option[String],
                        announcementTypeName: Option[String]) {

  val titlePattern = ".*(\\d{4}).*".r

  def yearOfPublished: Option[String] = {
    titlePattern.findFirstMatchIn(announcementTitle).map(_.group(1))
  }

}


object Announcement {
  implicit val formats = DefaultFormats
}