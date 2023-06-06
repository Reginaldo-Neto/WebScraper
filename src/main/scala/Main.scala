import ScrapRotten._
import ScrapIMDB._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import scala.io.StdIn
import java.io.PrintWriter

object Main {
  

  def main(args: Array[String]): Unit = {
    println("Digite o termo de busca:")
    val searchTerm = StdIn.readLine()
    
    val imdbData = scrapIMDB(searchTerm)
    
    val rottenData = scrapRotten(searchTerm)
    
    // fazer algo mais legal com as infos coletadas
    imdbData.foreach(println)
    println("---------------------------------------------")
    println("---------------------------------------------")
    rottenData.foreach(println)
  }
}
