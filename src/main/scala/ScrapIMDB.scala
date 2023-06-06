import LinkUtils._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import scala.io.StdIn
import java.io.PrintWriter

object ScrapIMDB {
    val searchBase = "https://www.imdb.com/find/?q=" // Insira o link de busca aqui
    val searchEnd = "&s=tt&exact=true&ref_=fn_tt_ex"
    val baseLink = "https://www.imdb.com"
    
    def scrapIMDB(searchTerm: String): List[(String, String, String, String)] = {
        val searchUrl = searchBase + searchTerm + searchEnd
        //println(searchUrl)
        // Realiza a requisição HTTP e obtém o conteúdo da página de resultados
        val doc = Jsoup.connect(searchUrl).get()

        // Extrai todos os links da página de resultados
        val links = extractLinks(doc)
        val filtered = links.filter(_.startsWith("/title/"))

        val fullLinks = filtered.map(link => baseLink + link)

        // fullLinks.foreach(println)

        // Filtra os links para manter apenas aqueles que contenham menções ao termo pesquisado
        //val filteredLinks = fullLinks.filter(_.startsWith("https://www.imdb.com/title/"))

        val uniqueLinks = fullLinks.distinct

        val validLinks = filterInvalidLinks(uniqueLinks)

        // Processa os links e extrai informações das páginas
        val extractedData = processIMDBLinks(validLinks)

        extractedData
    }
}