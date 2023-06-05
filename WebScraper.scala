import org.jsoup.Jsoup
import scala.collection.JavaConverters._
import scala.io.Source
import java.io.PrintWriter

object WebCrawler {
  def main(args: Array[String]): Unit = {
    val searchTerm = "Moonlight" // Termo de busca desejado
    val baseUrl = "https://www.rottentomatoes.com/search?search=" // URL base do site a ser rastreado
    val searchUrl = baseUrl + searchTerm
    val visitedPages = collection.mutable.Set[String]() // Páginas já visitadas

    crawlPage(searchUrl, searchTerm, visitedPages)
  }

  def crawlPage(url: String, searchTerm: String, visitedPages: collection.mutable.Set[String]): Unit = {
    if (!visitedPages.contains(url)) {
      try {
        visitedPages.add(url)
        val doc = Jsoup.connect(url).get()

        // Verificar se o termo de busca está presente no conteúdo da página
        if (doc.body().text().toLowerCase.contains(searchTerm.toLowerCase)) {
            // val document = Jsoup.connect(url).get()

            // println(s"Mencao encontrada em $url.")

            val links = doc.select("a[href]") // Selecionar todos os links na página
            val matchingLinks = links.asScala.filter(link => link.text().toLowerCase.contains(searchTerm.toLowerCase)) // Filtrar os links que contêm o termo de busca
              

            val finalLinks = matchingLinks.filterNot(link =>
              link.text().contains("reviews")
            )

            finalLinks.foreach { link =>
              val movieUrl = link.absUrl("href") // Extrair o URL do link
              println(movieUrl)
              crawlPage(movieUrl, searchTerm, visitedPages) // Chamar a função crawlPage para visitar a página associada ao link
            }

            val htmlContent = doc.html()
            val filepath = "output.txt"
            val writer = new PrintWriter(filepath)
            writer.println(htmlContent)
            writer.close()
            println(s"Conteudo salvo em $filepath")
              
            
            // Extrair informações relevantes da página
            // val movieName = doc.select("sc-afe43def-0 esVJhx").text()
            // val description = doc.select("sc-2eb29e65-1 goRLhJ").text()
            // val rating = doc.select("sc-bde20123-1 iZlgcd").text()

            // // Imprimir as informações extraídas
            // println("Nome do filme: " + movieName)
            // println("Descrição: " + description)
            // println("Avaliação: " + rating + "/10")
            // println("-----------------------------------")
        }

        // Extrair links das páginas e recursivamente visitar cada uma delas
        val links = doc.select("a[href]")
        val newUrls = links.asScala.map(_.absUrl("href"))
        newUrls.foreach { newUrl =>
          if (newUrl.startsWith(url)) {
            crawlPage(newUrl, searchTerm, visitedPages)
          }
        }
      } catch {
        case ex: Exception => println(s"Erro ao acessar a página $url: ${ex.getMessage}")
      }
    }
  }
}
