package ren.nicholas.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, PoisonPill}
import net.java.truecommons.shed.ResourceLoan._
import org.apache.pdfbox.pdmodel.PDDocument.load
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.apache.pdfbox.text.PDFTextStripper

import scala.util.{Failure, Success, Try}

object DocumentStripper {

  case class Strip(file: File)

  case class NotMatchedFor(file: File)

  case class StripCompleted(file: File)

}

class DocumentStripper extends Actor with ActorLogging {
  val stripper = new PDFTextStripper()

  override def receive: Receive = {
    case DocumentStripper.Strip(file) => {
      tryOf(load(file)) match {
        case Success(document) =>
          strip(document, file) match {
            case Some(f) => sender ! DocumentStripper.StripCompleted(file)
            case None => sender ! DocumentStripper.NotMatchedFor(file)
          }
        case Failure(e) =>
          log.error(s"Strip failed for ${file.getName}, ${e}")
          sender ! DocumentStripper.NotMatchedFor(file)
      }
      self ! PoisonPill
    }
  }

  def strip(document: PDDocument, source: File): Option[File] = {
    log.debug(s"Strip for ${source.getName}")
    loan(document).to {
      document => {
        val matchedPages: Set[PDPage] = matchedPageOf(document)
        if (matchedPages.isEmpty) {
          None
        } else {
          Some(extractedFrom(source, matchedPages))
        }
      }
    }
  }

  def extractedFrom(source: File, matchedPages: Set[PDPage]): File = {
    val target: File = targetFileFor(source)
    loan(new PDDocument()).to {
      out => {
        matchedPages.foreach(out.importPage)
        out.save(target)
      }
    }
    target
  }

  def matchedPageOf(in: PDDocument): Set[PDPage] = {
    (1 to in.getNumberOfPages)
      .filter(matchedIn(in))
      .flatMap(adjacent)
      .toSet
      .map { p: Int => in.getPage(p - 1) }

  }

  def targetFileFor(file: File): File = {
    val outputPath: String = file.getAbsolutePath.replace("annual-announcements", "stripped-annual-announcements")
    val outfile: File = new File(outputPath)
    outfile
  }

  def adjacent(i: Int): List[Int] = List(i, i + 1, i + 2, i + 3)

  def matchedIn(document: PDDocument)(page: Int): Boolean = {
    stripper.setStartPage(page)
    stripper.setEndPage(page)
    val text: String = stripper.getText(document)
    text.contains("调研") && text.contains("采访")
  }

  private def tryOf[T](t: => T): Try[T] = {
    try Success(t) catch {
      case e: Throwable => Failure(e)
    }
  }
}
