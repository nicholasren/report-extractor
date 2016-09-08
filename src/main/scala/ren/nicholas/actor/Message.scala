package ren.nicholas.actor

import ren.nicholas.model.Announcement

sealed trait Message

case class Find(stockNumber: String) extends Message

case class FindCompleted(stockNumber: String) extends Message

case class Download(stockNumber: String, announcement: Announcement) extends Message

case class DownloadCompleted(stockNumber: String, announcement: Announcement) extends Message

case object Start

