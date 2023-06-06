package crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import java.io.PrintWriter

object WebCrawler {
  val searchBase = "https://www.livrariascuritiba.com.br/" // Insira o link de busca aqui

  def main(args: Array[String]): Unit = {
    val searchTerm = "carros"
    val searchUrl = searchBase + searchTerm

    // Realiza a requisição HTTP e obtém o conteúdo da página de resultados
    val doc = Jsoup.connect(searchUrl).get()

    // Extrai todos os links da página de resultados
    val links = extractLinks(doc)

    // Filtra os links para manter apenas aqueles que contenham menções ao termo pesquisado
    val filteredLinks = filterLinks(links, searchTerm)

    val uniqueLinks = filteredLinks.distinct

    val validLinks = filterInvalidLinks(uniqueLinks)

    // validLinks.foreach(println)

    // Processa os links e extrai informações das páginas
    val extractedData = processLinks(validLinks)

    // Imprime as informações extraídas
    extractedData.foreach(println)
  }

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

  def processLinks(links: List[String]): List[(String, String, String)] = {
    val extractedData = links.flatMap { link =>
      val doc = Jsoup.connect(link).get()

      // val htmlContent = doc.html()
      // val filepath = "output.html"
      // val writer = new PrintWriter(filepath)
      // writer.println(htmlContent)
      // writer.close()
      // println(s"Conteudo salvo em $filepath")

      // Extrai o título, o preço e a descrição do item da página
      val title = doc.select("div[class^=fn productName]").text()
      val price = doc.select("strong.skuBestPrice").text()
      val description = doc.select("div.productDescription").text()

      // Retorna uma tupla contendo o título, o preço e a descrição do item
      List((title, price, description))
    }

    extractedData
  }
}
