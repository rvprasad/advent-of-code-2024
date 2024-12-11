import io.Source
import scala.util.Using
import scala.collection.mutable.ListBuffer
import scala.collection.Seq
import java.util.HashMap

case class Location(val x: Int, val y: Int)
case class Node(val value: Int, neighbors: List[Location])
type TopographicGraph = Map[Location, Node]

def createTopographicMap(filename: String): TopographicGraph = {
  val lines = Using(Source.fromFile(filename)) {
    _.getLines.map(_.toList).toList
  }.get

  val numRows = lines.size
  val numCols = lines(0).size
  lines.zipWithIndex.foldLeft(Map[Location, Node]()) { (acc, e1) =>
    val (line, row) = e1
    line.zipWithIndex.foldLeft(acc) { (acc, e2) =>
      val (v, col) = e2
      val locations = List(1, -1)
        .map(i => List((row, col + i), (row + i, col)))
        .flatten()
        .filter((r, c) => 0 <= r && r < numRows && 0 <= c && c < numCols)
        .map(Location.apply.tupled)
      acc + ((Location(row, col), Node(v.asDigit, locations)))
    }
  }
}

def solve(graph: TopographicGraph): (Int, Int) =
  def getScore(start: Location) =
    var trailEnds = scala.collection.mutable.ListBuffer[Location]()
    val workList = scala.collection.mutable.Stack(start)
    while (!workList.isEmpty) {
      val loc = workList.pop()
      val node = graph(loc)
      if (node.value == 9) {
        trailEnds.addOne(loc)
      } else {
        workList.pushAll(
          node.neighbors.filter(l => graph(l).value == node.value + 1)
        )
      }
    }
    trailEnds

  val trailEnds = graph.filter(_._2.value == 0).map(_._1).map(getScore)
  (trailEnds.map(_.distinct.size).sum, trailEnds.map(_.size).sum)

@main def main(filename: String) = {
  val map = createTopographicMap(filename)
  println(solve(map))
}
