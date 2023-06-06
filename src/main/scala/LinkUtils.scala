import LinkUtils._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.io.Source
import java.io.PrintWriter

object LinkUtils {
 
    def extractLinks(doc: Document): List[String] = {
        // Utiliza o seletor CSS para encontrar os elementos <a> que contêm os links
        val linkElements = doc.select("a")

        // Extrai o atributo "href" de cada elemento <a> e converte para uma lista de strings
        val links = linkElements.asScala.map(_.attr("href")).toList

        links
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
}
