package ren.nicholas.model

import argonaut.Argonaut._
import argonaut.CodecJson

case class Announcement(id: Option[String],
                        secCode: String,
                        secName: String,
                        orgId: String,
                        announcementId: Long,
                        announcementTitle: String,
                        announcementTime: Long,
                        adjunctUrl: String,
                        adjunctSize: Int,
                        adjunctType: String,
                        storageTime: Long,
                        columnId: String,
                        pageColumn: String,
                        announcementType: String,
                        associateAnnouncement: Option[String],
                        important: Option[String],
                        batchNum: Option[String],
                        announcementContent: Option[String],
                        announcementTypeName: Option[String])


object Announcement {
  implicit def AnnouncementCodecJson: CodecJson[Announcement] =
    casecodec19(Announcement.apply, Announcement.unapply)(
      "id",
      "secCode",
      "secName",
      "orgId",
      "announcementId",
      "announcementTitle",
      "announcementTime",
      "adjunctUrl",
      "adjunctSize",
      "adjunctType",
      "storageTime",
      "columnId",
      "pageColumn",
      "announcementType",
      "associateAnnouncement",
      "important",
      "batchNum",
      "announcementContent",
      "announcementTypeName"
    )
}