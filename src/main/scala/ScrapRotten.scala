import LinkUtils._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import scala.io.StdIn
import java.io.PrintWriter

object ScrapRotten {
    val searchBase = "https://www.rottentomatoes.com/search?search=" // Insira o link de busca aqui
    
    def scrapRotten(searchTerm: String): List[(String, String, String, String, String)] = {
        // monta o link de pesquisa
        val searchExp = searchTerm.replace(" ", "%20")
        val searchUrl = searchBase + searchExp
       
        // Realiza a requisição HTTP e obtém o conteúdo da página de resultados
        val doc = Jsoup.connect(searchUrl).get()

        // Extrai todos os links da página de resultados
        val links = extractLinks(doc)

        // Filtra os links para manter apenas links de paginas de filme
        val movieLinks = links.filter(_.startsWith("https://www.rottentomatoes.com/m/"))
        
        // remove pagina de review de filme promovido no topo da pagina
        val filteredLinks = movieLinks.filterNot(_.endsWith("/reviews?intcmp=rt-scorecard_tomatometer-reviews"))
        
        val uniqueLinks = filteredLinks.distinct // remove duplicatas
        
        val validLinks = filterInvalidLinks(uniqueLinks) // verifica se link eh valido
        
        // extrai informações das páginas
        val extractedData = processRottenPages(validLinks)

        extractedData // retorna informacoes pra main
    }

    def processRottenPages(links: List[String]): List[(String, String, String, String, String)] = {
        // var counter = 1
        val extractedData = links.flatMap { link =>
            val doc = Jsoup.connect(link).get()

            // --- util pra olhar o que o jsoup ta extraindo e analisar se as expressoes do select estao corretas
            // val htmlContent = doc.html()
            // val filepath = s"output$counter.html"
            // counter += 1
            // val writer = new PrintWriter(filepath)
            // writer.println(htmlContent)
            // writer.close()
            // println(s"Conteudo salvo em $filepath")

            // Extrai as informacoes da pagina
            //coloca N/A no que retornar nulo (nao ta funcionando, se nao conseguir resolver, remove  o Option e .getOrElse)
            val title = Option(doc.select("h1.title[data-qa=score-panel-title]").text()).getOrElse("N/A")
            val tomatometer = Option(doc.select("score-board[data-qa=score-panel]").attr("tomatometerscore")).getOrElse("N/A")
            val audience = Option(doc.select("score-board[data-qa=score-panel]").attr("audiencescore")).getOrElse("N/A")
            val description = Option(doc.select("p[data-qa=movie-info-synopsis]").text()).getOrElse("N/A")

            // monta uma tupla com as informacoes 
            List((title, tomatometer, audience, description, link))
        }
        // retorna lista de tuplas
        extractedData
    }
}