import ScrapRotten._
import ScrapIMDB._
import ScrapMeta._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import scala.io.StdIn
import java.io.PrintWriter

import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFRow, XSSFWorkbook}
import java.io.FileOutputStream

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    println("Digite o termo de busca:")
    val searchTerm = scala.io.StdIn.readLine()

    val imdbFuture: Future[List[(String, String, String, String, String, String, String, String, String)]] = Future {
      scrapIMDB(searchTerm)
    }

    val rottenFuture: Future[List[(String, String, String, String, String, String, String, String, String)]] = Future {
      scrapRotten(searchTerm)
    }

    val metaFuture: Future[List[(String, String, String, String, String, String, String, String, String)]] = Future {
      scrapMeta(searchTerm)
    }

    val combinedFuture: Future[(List[(String, String, String, String, String, String, String, String, String)],
                                List[(String, String, String, String, String, String, String, String, String)],
                                List[(String, String, String, String, String, String, String, String, String)])] =
      for {
        imdbData <- imdbFuture
        rottenData <- rottenFuture
        metaData <- metaFuture
      } yield (imdbData, rottenData, metaData)

    val processedFuture: Future[Unit] = combinedFuture.map { case (imdbData, rottenData, metaData) =>
      println("Registrando informações no arquivo de saída.")

      val workbook = new XSSFWorkbook()
      val sheet = workbook.createSheet("Dados")

      val headers = Array("Title", "Rating", "Popularity", "Description", "Director", "Genres", "Durantion", "Release", "Link")
      val headerRow = sheet.createRow(0)
      headers.indices.foreach(i => {
        val cell = headerRow.createCell(i)
        cell.setCellValue(headers(i))
      })

      var rowIndex = 1

      // Escrever os dados da fonte MetaCritic
      metaData.foreach { data =>
        val row = sheet.getRow(rowIndex)
        if (row == null) {
          val newRow = sheet.createRow(rowIndex)
          data.productIterator.zipWithIndex.foreach { case (value, columnIndex) =>
            val cell = newRow.createCell(columnIndex)
            cell.setCellValue(value.toString)
          }
        } else {
          data.productIterator.zipWithIndex.foreach { case (value, columnIndex) =>
            val cell = row.createCell(columnIndex)
            cell.setCellValue(value.toString)
          }
        }
        rowIndex += 1
      }

      // Escrever os dados da fonte IMDB
      imdbData.foreach { data =>
        val row = sheet.getRow(rowIndex)
        if (row == null) {
          val newRow = sheet.createRow(rowIndex)
          data.productIterator.zipWithIndex.foreach { case (value, columnIndex) =>
            val cell = newRow.createCell(columnIndex)
            cell.setCellValue(value.toString)
          }
        } else {
          data.productIterator.zipWithIndex.foreach { case (value, columnIndex) =>
            val cell = row.createCell(columnIndex)
            cell.setCellValue(value.toString)
          }
        }
        rowIndex += 1
      }

      // Escrever os dados da fonte Rotten Tomatoes
      rottenData.foreach { data =>
        val row = sheet.createRow(rowIndex)
        data.productIterator.zipWithIndex.foreach { case (value, columnIndex) =>
          val cell = row.createCell(columnIndex)
          cell.setCellValue(value.toString)
        }
        rowIndex += 1
      }

      val outputFileName = searchTerm.replace(" ", "_") + "_search_data.xlsx"
      val outputStream = new FileOutputStream(outputFileName)
      workbook.write(outputStream)
      outputStream.close()
      println("Fim da execução.")
    }

    Await.result(processedFuture, 300.seconds)
  }
}


