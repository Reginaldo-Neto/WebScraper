import LinkUtils._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import scala.io.StdIn
import java.io.PrintWriter

object ScrapRotten {
    val searchBase = "https://www.rottentomatoes.com/search?search=" // Insira o link de busca aqui
    
    def scrapRotten(searchTerm: String): List[(String, String, String, String)] = {
        val searchUrl = searchBase + searchTerm
        // Realiza a requisição HTTP e obtém o conteúdo da página de resultados
        val doc = Jsoup.connect(searchUrl).get()

        // Extrai todos os links da página de resultados
        val links = extractLinks(doc)

        // Filtra os links para manter apenas aqueles que contenham menções ao termo pesquisado
        val filteredLinks = filterLinks(links, searchTerm)

        val uniqueLinks = filteredLinks.distinct

        val validLinks = filterInvalidLinks(uniqueLinks)

        // Processa os links e extrai informações das páginas
        val extractedData = processRottenLinks(validLinks)

        extractedData
    }
}