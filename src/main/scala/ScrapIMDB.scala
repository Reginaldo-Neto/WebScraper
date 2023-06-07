import LinkUtils._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import scala.io.StdIn
import java.io.PrintWriter

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.concurrent.Await

object ScrapIMDB {
  val searchBase = "https://www.imdb.com/find/?q="
  val searchEnd = "&s=tt&ttype=ft&ref_=fn_ft"
  val baseLink = "https://www.imdb.com"

  def scrapIMDB(searchTerm: String): List[(String, String, String, String, String)] = {
    val searchExp = searchTerm.replace(" ", "%20")
    val searchUrl = searchBase + searchExp + searchEnd

    // Faz a requisição HTTP e obtém o documento HTML
    val doc = Jsoup.connect(searchUrl).get()

    // Extrai os links da página de busca
    val links = extractLinks(doc)

    // Filtra os links para obter apenas os links de filmes
    val filtered = links.filter(_.startsWith("/title/"))

    // Cria os links completos adicionando o prefixo base
    val fullLinks = filtered.map(link => baseLink + link)

    // Remove links duplicados
    val uniqueLinks = fullLinks.distinct

    // Filtra os links inválidos
    val validLinks = filterInvalidLinks(uniqueLinks)

    // Processa as páginas do IMDB e extrai os dados
    processIMDBPages(validLinks)
  }

  def processIMDBPages(links: List[String]): List[(String, String, String, String, String)] = {
    // Cria uma lista de futuros, onde cada futuro representa o processamento assíncrono de uma página
    val extractedDataFutures = links.map { link =>
      Future {
        // Faz a requisição HTTP para obter o documento HTML da página do IMDB
        val doc = Jsoup.connect(link).get()

        // Extrai as informações do filme da página
        val title = doc.select("span.sc-afe43def-1.fDTGTb").text()
        val ratingElement = Option(doc.select("span.sc-bde20123-1.iZlgcd").first())
        val rating = ratingElement.map(_.text()).getOrElse("N/A")
        val popularityElement = Option(doc.select("div[data-testid=hero-rating-bar__popularity__score]").first())
        val popularity = popularityElement.map(_.text()).getOrElse("N/A")
        val description = doc.select("span[data-testid=plot-xs_to_m]").text()

        // Retorna uma tupla contendo as informações do filme e o link da página
        (title, rating, popularity, description, link)
      }
    }

    // Combina todos os futuros em um único futuro que produz uma lista de resultados
    val extractedDataFuture = Future.sequence(extractedDataFutures)

    // Espera pelos resultados finais com um tempo limite de 90 segundos
    val extractedData = Await.result(extractedDataFuture, 90.seconds)

    // Retorna a lista de dados extraídos das páginas do IMDB
    extractedData
  }
}
