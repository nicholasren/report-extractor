package ren.nicholas.actor

import java.io.File

import ren.nicholas.model.Announcement

sealed trait Message

case object Find extends Message

case class FindCompleted(stockNumber: String) extends Message

case class NoAnnouncement(stockNumber: String) extends Message

case class Download(stockNumber: String, announcement: Announcement) extends Message

case class DownloadCompleted(stockNumber: String, announcement: Announcement) extends Message

case class Strip(file: File) extends Message
case class StripCompleted(file: File) extends Message
case class NotMatchedFor(file: File) extends Message

case object Strip extends Message

case object Start

case object Inspect
