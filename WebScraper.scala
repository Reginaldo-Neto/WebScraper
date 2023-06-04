import org.jsoup.Jsoup
import scala.collection.JavaConverters._

object WebCrawler {
  def main(args: Array[String]): Unit = {
    val searchTerm = "moonlight" // Termo de busca desejado
    val baseUrl = "https://www.imdb.com/find/?q=moonlight" // URL base do site a ser rastreado
    val visitedPages = collection.mutable.Set[String]() // Páginas já visitadas

    crawlPage(baseUrl, searchTerm, visitedPages)
  }

  def crawlPage(url: String, searchTerm: String, visitedPages: collection.mutable.Set[String]): Unit = {
    if (!visitedPages.contains(url)) {
      try {
        visitedPages.add(url)
        val doc = Jsoup.connect(url).get()

        // Verificar se o termo de busca está presente no conteúdo da página
        if (doc.body().text().toLowerCase.contains(searchTerm.toLowerCase)) {
            val document = Jsoup.connect(url).get()
            
            // Extrair informações relevantes da página
            val movieName = document.select(".sc-afe43def-1 fDTGTb").text()
            val description = document.select(".sc-2eb29e65-1 goRLhJ").text()
            val rating = document.select(".sc-bde20123-1 iZlgcd").text()

            // Imprimir as informações extraídas
            println("Nome do filme: " + movieName)
            println("Descrição: " + description)
            println("Avaliação: " + rating + "/10")
            println("-----------------------------------")
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
