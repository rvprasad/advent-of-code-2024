import io.Source
import scala.util.Using
import scala.collection.mutable.ListBuffer
import scala.math.abs

case class Location(x: Int, y: Int)
case class Node(value: Char, neighbors: List[Location])
type RaceTrack = Map[Location, Node]

def readRaceTrack(filename: String) =
  def getValid(e: Int, min: Int, max: Int) =
    List(e + 1, e - 1).filter(e => min <= e && e < max)

  val lines = Using(Source.fromFile(filename)) {
    _.getLines.filter(!_.isEmpty).map(_.toList).toList
  }.get
  val numRows = lines.size
  val numCols = lines(0).size

  lines.zip(LazyList.from(1)).foldLeft(Map[Location, Node]()) {
    (raceTrack, e1) =>
      val (line, y) = e1
      line.zip(LazyList.from(1)).foldLeft(raceTrack) { (raceTrack, e2) =>
        val (v, x) = e2
        val neighbors =
          getValid(x, 0, numCols).map(Location(_, y)) ++
            getValid(y, 0, numRows).map(Location(x, _))
        raceTrack + { Location(x, y) -> Node(v, neighbors) }
      }
  }

def solve(
    raceTrack: RaceTrack,
    maxCheatLength: Int,
    minPicoSecondsToSave: Int
) =
  def getFullPath(path: List[Location]): List[Location] =
    val tmp = raceTrack(path.head).neighbors.filter(l =>
      raceTrack(l).value != '#' && (path.length < 2 || path(1) != l)
    )
    if tmp.isEmpty then path.reverse
    else getFullPath(tmp.head :: path)

  val fullPath = getFullPath(
    List[Location](raceTrack.filter((l, n) => n.value == 'S').head._1)
  )
  val nullNode = Node('#', List.empty)

  fullPath
    .map(src =>
      val srcIndex = fullPath.indexOf(src)
      (for
        x <- -maxCheatLength to maxCheatLength
        y <- 0 to maxCheatLength
        if (y != 0 || x > 0) && // avoid duplicates due to overlapping x range
          abs(x) + y <= maxCheatLength
      yield (Location(x + src.x, y + src.y), abs(x) + y))
        .filter((trg, cl) => raceTrack.getOrElse(trg, nullNode).value != '#')
        .map((trg, cl) => abs(srcIndex - fullPath.indexOf(trg)) - cl)
        .filter(_ >= minPicoSecondsToSave)
    )
    .flatten
    .length

@main def main(
    filename: String,
    maxCheatLength: Int,
    minPicoSecondsToSave: Int
) =
  val raceTrack = readRaceTrack(filename)
  println(solve(raceTrack, maxCheatLength, minPicoSecondsToSave))
