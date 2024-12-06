import io.Source
import scala.util.Using

case class Input(
    page2pages: Map[String, Set[String]],
    updates: List[List[String]]
)

def createInput(filename: String): Input =
  val (p, updates) = Using(Source.fromFile(filename)) {
    _.getLines.toList
  }.get.iterator.foldLeft(
    (
      Map.empty[String, Set[String]].withDefaultValue(Set.empty[String]),
      List.empty[List[String]]
    )
  ) { (input, currLine) =>
    val (page2pages, updates) = input
    currLine match {
      case l if l.contains('|') => {
        val page2page = l.split('|')
        val page: String = page2page(0)
        (page2pages + ((page, page2pages(page) + page2page(1))), updates)
      }
      case l if l.contains(',') => {
        (page2pages, updates :+ l.split(',').toList)
      }
      case _ => input
    }
  }
  Input(p, updates)

def isGood(page2pages: Map[String, Set[String]], update: List[String]) =
  page2pages.keySet
    .map(i => (i, update.indexOf(i)))
    .filter(_._2 != -1)
    .forall((page, index) =>
      page2pages(page)
        .map(update.lastIndexOf(_))
        .filter(_ != -1)
        .forall(_ > index)
    )

def solvePart1(input: Input): Int =
  input.updates
    .filter(isGood(input.page2pages, _))
    .map(u => { u(u.length / 2).toInt })
    .sum()

@main def main(filename: String) = {
  val input = createInput(filename)
  println(solvePart1(input))
}
