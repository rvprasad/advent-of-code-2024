import io.Source
import scala.util.Using
import scala.collection.mutable.ListBuffer
import scala.collection.Seq

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

def getPagePositions(
    update: Seq[String],
    page2pages: Map[String, Set[String]]
) =
  page2pages.keySet
    .map(p => (p, update.indexOf(p)))
    .filter(_._2 != -1)
    .map(pageAndIndex =>
      page2pages(pageAndIndex._1)
        .map(i => (i, update.lastIndexOf(i)))
        .filter(_._2 != -1)
        .map(i => ((pageAndIndex), i))
    )
    .flatten()

def isUpdateGood(update: Seq[String], page2pages: Map[String, Set[String]]) =
  getPagePositions(update, page2pages).forall(i => i._1._2 < i._2._2)

def fixUpdate(update: Seq[String], page2pages: Map[String, Set[String]]) =
  def fix(update: ListBuffer[String]): ListBuffer[String] =
    getPagePositions(update, page2pages).find(pageAndIndexPair =>
      pageAndIndexPair._1._2 > pageAndIndexPair._2._2
    ) match {
      case None => update
      case Some((predIndex, succIndex)) =>
        update.insert(succIndex._2, update.remove(predIndex._2))
        fix(update)
    }

  fix(ListBuffer.from(update)).toList

def solve(input: Input): (Int, Int) =
  def sumUpdates(updates: List[List[String]]) =
    updates.map(u => { u(u.length / 2).toInt }).sum()

  val (goodUpdates, badUpdates) = input.updates
    .partition(isUpdateGood(_, input.page2pages))
  val fixedUpdates = badUpdates.map(fixUpdate(_, input.page2pages))
  (sumUpdates(goodUpdates), sumUpdates(fixedUpdates))

@main def main(filename: String) = {
  val input = createInput(filename)
  println(solve(input))
}
