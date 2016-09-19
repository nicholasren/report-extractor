package ren.nicholas.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, PoisonPill}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import scala.util.{Failure, Success, Try}


class DocumentStripper extends Actor with ActorLogging {

  override def receive: Receive = {
    case Strip(file) => {
      val triedDocument: Try[PDDocument] = tryOf(PDDocument.load(file))

      triedDocument match {
        case Success(in) => {
          strip(file, in) match {
            case Some(f) => sender ! StripCompleted(file)
            case None => sender ! NotMatchedFor(file)
          }

        }
        case Failure(e) => {
          log.error(s"Strip failed for ${file.getName}, ${e}")
          sender ! StripCompleted(file)
        }
      }

      self ! PoisonPill
    }
  }

  def strip(file: File, in: PDDocument): Option[File] = {
    val matchedPages: Set[Int] = (1 to in.getNumberOfPages)
      .filter(matchedIn(in))
      .flatMap(adjacent)
      .toSet

    if (matchedPages.isEmpty) {
      None
    } else {
      val out: PDDocument = new PDDocument()
      val target: File = targetFileFor(file)
      matchedPages.foreach(i => out.importPage(in.getPage(i - 1)))

      out.save(target)

      in.close()
      out.close()

      Some(target)
    }
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
