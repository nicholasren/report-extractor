package ren.nicholas.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, PoisonPill}
import net.java.truecommons.shed.ResourceLoan._
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.apache.pdfbox.text.PDFTextStripper

import scala.util.{Failure, Success, Try}


class DocumentStripper extends Actor with ActorLogging {

  override def receive: Receive = {
    case Strip(file) => {
      val target: File = targetFileFor(file)
      if (!target.exists()) {
        tryOf(PDDocument.load(file)) match {
          case Success(document) =>
            strip(document, target) match {
              case Some(f) => sender ! StripCompleted(file)
              case None => sender ! NotMatchedFor(file)
            }
          case Failure(e) =>
            log.error(s"Strip failed for ${file.getName}, ${e}")
            sender ! StripCompleted(file)
        }
      }
      self ! PoisonPill
    }
  }

  def strip(document: PDDocument, target: File): Option[File] = {

    loan(document).to {
      in => {
        val matchedPages: Set[PDPage] = findContainedPages(in)
        if (matchedPages.isEmpty) {
          None
        } else {
          loan(new PDDocument()).to {
            out => {
              matchedPages.foreach(out.importPage)
              out.save(target)
            }
          }
          Some(target)
        }
      }
    }
  }


  def findContainedPages(in: PDDocument): Set[PDPage] = {
    (1 to in.getNumberOfPages)
      .filter(matchedIn(in))
      .flatMap(adjacent)
      .toSet
      .map { p: Int => in.getPage(p - 1)}

  }

  def targetFileFor(file: File): File = {
    val outputPath: String = file.getAbsolutePath.replace("annual-announcements", "stripped-annual-announcements")
    val outfile: File = new File(outputPath)
    outfile
  }

  def adjacent(i: Int): List[Int] = List(i, i + 1)

  def matchedIn(document: PDDocument)(page: Int): Boolean = {
    val stripper = new PDFTextStripper()
    stripper.setStartPage(page)
    stripper.setEndPage(page)
    val text: String = stripper.getText(document)
    text.contains("调研") && text.contains("采访")
  }

  def tryOf[T](t: => T): Try[T] = {

    try Success(t) catch {
      case e: Throwable => Failure(e)
    }

  }
}
