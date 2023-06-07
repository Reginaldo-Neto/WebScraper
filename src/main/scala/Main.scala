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

    val imdbFuture: Future[List[(String, String, String, String, String)]] = Future {
      scrapIMDB(searchTerm)
    }

    val rottenFuture: Future[List[(String, String, String, String, String)]] = Future {
      scrapRotten(searchTerm)
    }

    val metaFuture: Future[List[(String, String, String, String, String)]] = Future {
      scrapMeta(searchTerm)
    }

    val combinedFuture: Future[(List[(String, String, String, String, String)],
                                List[(String, String, String, String, String)],
                                List[(String, String, String, String, String)])] =
      for {
        imdbData <- imdbFuture
        rottenData <- rottenFuture
        metaData <- metaFuture
      } yield (imdbData, rottenData, metaData)

    val processedFuture: Future[Unit] = combinedFuture.map { case (imdbData, rottenData, metaData) =>

      println("Aqui")
      // imdbData.foreach(println)
      // rottenData.foreach(println)
      // metaData.foreach(println)

      // Criar um novo livro de trabalho do Excel
      val workbook = new XSSFWorkbook()
      val sheet = workbook.createSheet("Dados")

      // Escrever os cabeçalhos das colunas
      val headers = Array("Título", "MetaScore", "UserScore", "Descrição", "Link")
      val headerRow = sheet.createRow(0)
      headers.indices.foreach(i => {
        val cell = headerRow.createCell(i)
        cell.setCellValue(headers(i))
      })

      // Escrever os dados nas células
      metaData.zipWithIndex.foreach { case (data, rowIndex) =>
        val row = sheet.createRow(rowIndex + 1)
        data.productIterator.zipWithIndex.foreach { case (value, columnIndex) =>
          val cell = row.createCell(columnIndex)
          cell.setCellValue(value.toString)
        }
      }

      // Salvar o arquivo da planilha
      val outputStream = new FileOutputStream("dados.xlsx")
      workbook.write(outputStream)
      outputStream.close()

    }

    // Esperar pela conclusão do processamento
    Await.result(processedFuture, 300.seconds)
    println("Terminou")
  }
}