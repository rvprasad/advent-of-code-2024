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

  lines.zipWithIndex.foldLeft(Map[Location, Node]()) { (raceTrack, e1) =>
    val (line, y) = e1
    line.zipWithIndex.foldLeft(raceTrack) { (raceTrack, e2) =>
      val (v, x) = e2
      val location = Location(x, y)
      val neighbors = getValid(x, 0, numCols).map(Location(_, y)) ++
        getValid(y, 0, numRows).map(Location(x, _))
      raceTrack + { location -> Node(v, neighbors) }
    }
  }

def getFullPath(path: List[Location], raceTrack: RaceTrack): List[Location] =
  val tmp = raceTrack(path.head).neighbors.filter(l =>
    raceTrack(l).value != '#' && (path.length < 2 || path(1) != l)
  )
  if tmp.isEmpty then path.reverse
  else getFullPath(tmp.head :: path, raceTrack)

def solve(raceTrack: RaceTrack) =
  val fullPath = getFullPath(
    List[Location](raceTrack.filter((l, n) => n.value == 'S').head._1),
    raceTrack
  )
  raceTrack
    .filter((l, n) => n.value == '#')
    .foldLeft(List[Int]()) { (saves, pair) =>
      val (loc, node) = pair
      val nonWalls = node.neighbors.filter(l => raceTrack(l).value != '#')
      saves ++ nonWalls
        .combinations(2)
        .map(e => abs((fullPath.indexOf(e(0)) - fullPath.indexOf(e(1)))) - 2)
        .filter(_ > 0)
        .toList
    }
    .filter(_ >= 100)
    .length

@main def main(filename: String) =
  val raceTrack = readRaceTrack(filename)
  println(solve(raceTrack))
