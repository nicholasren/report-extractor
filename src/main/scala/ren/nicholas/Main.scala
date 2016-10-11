package ren.nicholas

import ren.nicholas.format.docx.TableExtractor



object Main extends App {
  val path: String = "./data/stage2/word-input.docx"

  private val text: List[List[String]] = TableExtractor.tableTextFrom(path)
  text.foreach(println)
}


