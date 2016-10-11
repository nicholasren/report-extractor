package ren.nicholas.format.docx

import java.io.FileInputStream
import java.util

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.xwpf.usermodel.{IBodyElement, XWPFDocument, XWPFTable}

import scala.collection.JavaConversions._

object TableExtractor {

  def tableTextFrom(docPath: String): List[List[String]] = {
    val fis = new FileInputStream(docPath)
    val doc = new XWPFDocument(OPCPackage.open(fis))
    val iterator: util.Iterator[IBodyElement] = doc.getBodyElementsIterator

    val element: IBodyElement = iterator.next()
    val table: XWPFTable = element.getBody.getTables.get(0)

    toText(table)
  }

  def toText(table: XWPFTable): List[List[String]] = {
    table.getRows.toList.map(row => row.getTableCells.toList.map(_.getText))
  }
}
