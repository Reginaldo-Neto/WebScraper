import LinkUtils._

import scala.io.Source
import scala.io.StdIn
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.PrintWriter

import scala.concurrent.Await

object ScrapRotten {
  val searchBase = "https://www.rottentomatoes.com/search?search="

  def scrapRotten(searchTerm: String): List[(String, String, String, String, String, String, String, String, String)] = {
    val searchExp = searchTerm.replace(" ", "%20")
    val searchUrl = searchBase + searchExp

    // Faz a requisição HTTP e obtém o documento HTML
    val doc = Jsoup.connect(searchUrl).get()

    // Extrai os links da página de busca
    val links = extractLinks(doc)

    // Filtra os links para obter apenas os links de filmes
    val movieLinks = links.filter(_.startsWith("https://www.rottentomatoes.com/m/"))

    // Filtra os links que levam para a página de avaliações de filmes
    val filteredLinks = movieLinks.filterNot(_.endsWith("/reviews?intcmp=rt-scorecard_tomatometer-reviews"))

    // Remove links duplicados
    val uniqueLinks = filteredLinks.distinct

    // Filtra os links inválidos
    val validLinks = filterInvalidLinks(uniqueLinks)

    // Processa as páginas dos filmes e extrai os dados
    processRottenPages(validLinks)
  }

  def processRottenPages(links: List[String]): List[(String, String, String, String, String, String, String, String, String)] = {
    // Cria uma lista de futuros, onde cada futuro representa o processamento assíncrono de uma página
    val extractedDataFutures = links.map { link =>
      Future {
        // Faz a requisição HTTP para obter o documento HTML da página do filme
        val doc = Jsoup.connect(link).get()

        // Extrai as informações do filme da página
        val title = Option(doc.select("h1.title[data-qa=score-panel-title]").text()).getOrElse("N/A")
        val tomatometer = Option(doc.select("score-board[data-qa=score-panel]").attr("tomatometerscore")).getOrElse("N/A")
        val audience = Option(doc.select("score-board[data-qa=score-panel]").attr("audiencescore")).getOrElse("N/A")
        val description = Option(doc.select("p[data-qa=movie-info-synopsis]").text()).getOrElse("N/A")
        val director = Option(doc.select("a[data-qa=movie-info-director]").first()).map(_.text()).getOrElse("N/A")
        val genres = Option(doc.select("ul#info li[data-qa=movie-info-item] b[data-qa=movie-info-item-label]:contains(Genre) + span").first()).map(_.text().trim()).getOrElse("N/A")
        val runtime = Option(doc.select("p:has(b[data-qa=movie-info-item-label]:contains(Runtime:)) + span.info-item-value time").first()).map(_.text().trim()).filter(_.nonEmpty).getOrElse("N/A")
        val releaseDate = Option(doc.select("span.info-item-value time").first()).map(_.text().trim()).getOrElse("N/A")

        // Retorna uma tupla contendo as informações do filme e o link da página
        (title, tomatometer, audience, description, director, genres, runtime, releaseDate, link)
      }
    }

    // Combina todos os futuros em um único futuro que produz uma lista de resultados
    val extractedDataFuture = Future.sequence(extractedDataFutures)

    // Espera pelos resultados finais com um tempo limite de 90 segundos
    val extractedData = Await.result(extractedDataFuture, 90.seconds)

    // Retorna a lista de dados extraídos das páginas dos filmes
    extractedData
  }
}