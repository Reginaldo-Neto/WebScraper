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

    val doc = Jsoup.connect(searchUrl).get()
    val links = extractLinks(doc)
    val filtered = links.filter(_.startsWith("/title/"))
    val fullLinks = filtered.map(link => baseLink + link)
    val uniqueLinks = fullLinks.distinct
    val validLinks = filterInvalidLinks(uniqueLinks)

    processIMDBPages(validLinks)
  }

  def processIMDBPages(links: List[String]): List[(String, String, String, String, String)] = {
    val extractedDataFutures = links.map { link =>
      Future {
        val doc = Jsoup.connect(link).get()
        val title = doc.select("span.sc-afe43def-1.fDTGTb").text()
        val ratingElement = Option(doc.select("span.sc-bde20123-1.iZlgcd").first())
        val rating = ratingElement.map(_.text()).getOrElse("N/A")
        val popularityElement = Option(doc.select("div[data-testid=hero-rating-bar__popularity__score]").first())
        val popularity = popularityElement.map(_.text()).getOrElse("N/A")
        val description = doc.select("span[data-testid=plot-xs_to_m]").text()
        (title, rating, popularity, description, link)
      }
    }

    val extractedDataFuture = Future.sequence(extractedDataFutures)
    val extractedData = Await.result(extractedDataFuture, 90.seconds)
    extractedData
  }
}