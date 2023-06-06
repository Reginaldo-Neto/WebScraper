import LinkUtils._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import java.io.PrintWriter

object LinkUtils {
 
  def extractLinks(doc: Document): List[String] = {
    // Utiliza o seletor CSS para encontrar os elementos <a> que contêm os links
    val linkElements = doc.select("a")

    // Extrai o atributo "href" de cada elemento <a> e converte para uma lista de strings
    val links = linkElements.asScala.map(_.attr("href")).toList

    links
  }

  def filterLinks(links: List[String], searchTerm: String): List[String] = {
    // Filtra os links para manter apenas aqueles que contenham menções ao termo pesquisado
    val filteredLinks = links.filter(link => link.contains(searchTerm))

    filteredLinks
  }

  def filterInvalidLinks(links: List[String]): List[String] = {
    val validLinks = links.filter { link =>
      try {
        val connection = Jsoup.connect(link).execute()
        connection.statusCode() == 200
      } catch {
        case _: Exception => false
      }
    }

    validLinks
  }

  def processRottenLinks(links: List[String]): List[(String, String, String, String)] = {
    // var counter = 1
    val extractedData = links.flatMap { link =>
      val doc = Jsoup.connect(link).get()

      // val htmlContent = doc.html()
      // val filepath = s"output$counter.html"
      // counter += 1
      // val writer = new PrintWriter(filepath)
      // writer.println(htmlContent)
      // writer.close()
      // println(s"Conteudo salvo em $filepath")

      // Extrai o título, o preço e a descrição do item da página
      val title = doc.select("h1.title[data-qa=score-panel-title]").text()
      val tomatometer = doc.select("score-board[data-qa=score-panel]").attr("tomatometerscore")
      val audience = doc.select("score-board[data-qa=score-panel]").attr("audiencescore")
      val description = doc.select("p[data-qa=movie-info-synopsis]").text()

      // Retorna uma tupla contendo o título, o preço e a descrição do item
      List((title, tomatometer, audience, description))
    }

    extractedData
  }

  def processIMDBLinks(links: List[String]): List[(String, String, String, String)] = {
    // var counter = 1
    val extractedData = links.flatMap { link =>
      val doc = Jsoup.connect(link).get()

      // val htmlContent = doc.html()
      // val filepath = s"output$counter.html"
      // counter += 1
      // val writer = new PrintWriter(filepath)
      // writer.println(htmlContent)
      // writer.close()
      // println(s"Conteudo salvo em $filepath")

      // Extrai o título, o preço e a descrição do item da página
      val title = doc.select("span.sc-afe43def-1.fDTGTb").text()
      val ratingElement = Option(doc.select("span.sc-bde20123-1.iZlgcd").first())
      val rating = ratingElement.map(_.text()).getOrElse("N/A")
      val popularityElement = Option(doc.select("div[data-testid=hero-rating-bar__popularity__score]").first())
      val popularity = popularityElement.map(_.text()).getOrElse("N/A")
      val description = doc.select("span[data-testid=plot-xs_to_m]").text()

      // Retorna uma tupla contendo o título, o preço e a descrição do item
      List((title, rating, popularity, description))
    }

    extractedData
  }
}
