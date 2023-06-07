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

object ScrapRotten {
  val searchBase = "https://www.rottentomatoes.com/search?search="

  def scrapRotten(searchTerm: String): List[(String, String, String, String, String)] = {
    val searchExp = searchTerm.replace(" ", "%20")
    val searchUrl = searchBase + searchExp
    val doc = Jsoup.connect(searchUrl).get()
    val links = extractLinks(doc)
    val movieLinks = links.filter(_.startsWith("https://www.rottentomatoes.com/m/"))
    val filteredLinks = movieLinks.filterNot(_.endsWith("/reviews?intcmp=rt-scorecard_tomatometer-reviews"))
    val uniqueLinks = filteredLinks.distinct
    val validLinks = filterInvalidLinks(uniqueLinks)

    processRottenPages(validLinks)
  }

  def processRottenPages(links: List[String]): List[(String, String, String, String, String)] = {
    val extractedDataFutures = links.map { link =>
      Future {
        val doc = Jsoup.connect(link).get()
        val title = Option(doc.select("h1.title[data-qa=score-panel-title]").text()).getOrElse("N/A")
        val tomatometer = Option(doc.select("score-board[data-qa=score-panel]").attr("tomatometerscore")).getOrElse("N/A")
        val audience = Option(doc.select("score-board[data-qa=score-panel]").attr("audiencescore")).getOrElse("N/A")
        val description = Option(doc.select("p[data-qa=movie-info-synopsis]").text()).getOrElse("N/A")
        (title, tomatometer, audience, description, link)
      }
    }

    val extractedDataFuture = Future.sequence(extractedDataFutures)
    val extractedData = Await.result(extractedDataFuture, 90.seconds)
    extractedData
  }
}
