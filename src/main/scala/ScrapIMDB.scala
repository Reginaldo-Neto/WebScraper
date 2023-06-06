import LinkUtils._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import scala.io.StdIn
import java.io.PrintWriter

object ScrapIMDB {
    val searchBase = "https://www.imdb.com/find/?q=" // Insira o link de busca aqui
    val searchEnd = "&s=tt&ttype=ft&ref_=fn_ft"
    val baseLink = "https://www.imdb.com"
    
    def scrapIMDB(searchTerm: String): List[(String, String, String, String, String)] = {
        val searchExp = searchTerm.replace(" ", "%20")
        val searchUrl = searchBase + searchExp + searchEnd

        // Realiza a requisição HTTP e obtém o conteúdo da página de resultados
        val doc = Jsoup.connect(searchUrl).get()

        // Extrai todos os links da página de resultados
        val links = extractLinks(doc)
        val filtered = links.filter(_.startsWith("/title/"))

        val fullLinks = filtered.map(link => baseLink + link)

        val uniqueLinks = fullLinks.distinct

        val validLinks = filterInvalidLinks(uniqueLinks)

        // Processa os links e extrai informações das páginas
        val extractedData = processIMDBPages(validLinks)

        extractedData
    }

    def processIMDBPages(links: List[String]): List[(String, String, String, String, String)] = {
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
            List((title, rating, popularity, description, link))
        }

        extractedData
  }
}