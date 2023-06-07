import ScrapRotten._
import ScrapIMDB._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import scala.io.StdIn
import java.io.PrintWriter

import scala.concurrent.{Await, Future}
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

      val combinedFuture: Future[(List[(String, String, String, String, String)], List[(String, String, String, String, String)])] =
          for {
              imdbData <- imdbFuture
              rottenData <- rottenFuture
          } yield (imdbData, rottenData)

      val combinedResult = Await.result(combinedFuture, 90.seconds)

      val imdbResult = combinedResult._1
      val rottenResult = combinedResult._2

      // Fazer algo mais interessante com as informações coletadas
      imdbResult.foreach(println)
      println("---------------------------------------------")
      println("---------------------------------------------")
      rottenResult.foreach(println)
    }
}