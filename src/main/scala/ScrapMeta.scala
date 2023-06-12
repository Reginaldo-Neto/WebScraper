import LinkUtils._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

object ScrapMeta {
  val searchBase = "https://www.metacritic.com/search/movie/"
  val searchEnd = "/results"

  def scrapMeta(searchTerm: String): List[(String, String, String, String, String, String, String, String, String)] = {
    val searchExp = searchTerm.replace(" ", "%20")
    val searchUrl = searchBase + searchExp + searchEnd

    // println("AQUi")
    // println(searchUrl)
    // println("________________________________________________________________________________")

    // Realiza a requisição HTTP e obtém o conteúdo da página de resultados
    val doc = Jsoup.connect(searchUrl).get()

    // Extrai todos os links da página de resultados
    val links = extractLinksMeta(doc)

    // Filtra os links para obter apenas os links de filmes
    val filtered = links.filter(_.startsWith("/movie/"))

    val fullLinks = filtered.map(link => "https://www.metacritic.com" + link)

    // Remove links duplicados
    val uniqueLinks = fullLinks.distinct

    // Filtra os links inválidos
    val validLinks = filterInvalidLinks(uniqueLinks)

    // validLinks.foreach(println)
    // Processa as páginas dos filmes e extrai os dados
    processMetaPages(validLinks)
  }

  // def extractLinksMeta(doc: Document): List[String] = {
  //   // Utiliza o seletor CSS para encontrar os elementos <a> que contêm os links, excluindo os que estão dentro do componente <nav>
  //   val linkElements = doc.select("a:not(#primary_nav_item_movies a)")

  //   // Extrai o atributo "href" de cada elemento <a> e converte para uma lista de strings
  //   val links = linkElements.asScala.map(_.attr("href")).toList

  //   links
  // }

  def extractLinksMeta(doc: Document): List[String] = {
    val linkElements = doc.select("a:not(#primary_nav_item_movies a)")

    val links = linkElements.asScala.map(_.attr("href")).toList

    val nextPageLink = doc.select("a.action[rel=next]").first()

    if (nextPageLink != null) {
      val nextPageUrl = nextPageLink.attr("abs:href")
      val nextPageDoc = Jsoup.connect(nextPageUrl).get()
      val nextPageLinks = extractLinksMeta(nextPageDoc)

      links ++ nextPageLinks
    } else {
      links
    }
  }

  def processMetaPages(links: List[String]): List[(String, String, String, String, String, String, String, String, String)] = {
    // Cria uma lista de futuros, onde cada futuro representa o processamento assíncrono de uma página
    val extractedDataFutures = links.map { link =>
      Future {
        // Faz a requisição HTTP para obter o documento HTML da página do filme
        val doc = Jsoup.connect(link).get()

        // Extrai as informações do filme da página

        val title = Option(doc.select("div.product_page_title h1").text().trim()).getOrElse("N/A")
        val metaScore = Option(doc.select("span.metascore_w").first()).map(_.text()).getOrElse("N/A")
        val userScore = Option(doc.select("span.metascore_w.user").first()).map(_.text()).getOrElse("N/A")    
        val summaryText = Option(doc.select("div.summary_deck.details_section > span:not(.label)").first()).map(_.text()).getOrElse("N/A")
        val director = Option(doc.select("div.director > a > span").first()).fold("N/A")(_.text())
        val runtime = Option(doc.select("div.runtime > span:not(.label)").first()).fold("N/A")(_.text())
        val genres = Option(doc.select("div.genres > span:not(.label)").first()).fold("N/A")(_.text())
        val releaseDate = Option(doc.select("span.release_date > span:not(.label)").first()).map(_.text()).getOrElse("N/A")

        // Retorna uma tupla contendo as informações do filme e o link da página
        (title, metaScore, userScore, summaryText, director, genres, runtime, releaseDate, link)
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
