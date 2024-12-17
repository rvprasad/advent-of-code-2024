import io.Source
import scala.util.Using
import scala.math.max
import scala.compiletime.ops.boolean

enum Direction:
  case Up, Down, Right, Left

case class Location(x: Int, y: Int):
  def nextLocation(direction: Direction) =
    direction match {
      case Direction.Up    => Location(x, y - 1)
      case Direction.Down  => Location(x, y + 1)
      case Direction.Left  => Location(x - 1, y)
      case Direction.Right => Location(x + 1, y)
    }

case class Obstacle()
case class Box(locations: List[Location])

object Direction:
  private val str2dir = Map("^" -> Up, "v" -> Down, ">" -> Right, "<" -> Left)
  def from(s: String) = str2dir(s)

type Entity = Box | Obstacle
type Config = Map[Location, Entity]

def readConfigAndMovements(filename: String) = {
  val content = Source.fromFile(filename).mkString.split("\n\n")

  val lines = content(0).split("\n")
  val numRows = lines.size
  val numCols = lines(0).size
  val n = max(numRows, numCols)

  val (config, robot) = lines.zipWithIndex.foldLeft(
    (Map[Location, Entity](), Location(-1, -1))
  ) { (acc, e1) =>
    val (line, y) = e1
    line.zipWithIndex.foldLeft(acc) { (acc, e2) =>
      val (v, x) = e2
      val (config, robot) = acc
      val location = Location(x, y)
      v match {
        case '.' => acc
        case '@' => (config, location)
        case '#' => (config + { location -> Obstacle() }, robot)
        case 'O' => (config + { location -> Box(List(location)) }, robot)
      }
    }
  }

  val movements = content(1).replace("\n", "").split("").map(Direction.from(_))
  (config, robot, movements)
}

def isMovePossible(
    boxes: List[Box],
    dir: Direction,
    config: Config
): Boolean =
  boxes
    .flatMap(b =>
      b.locations.map(_.nextLocation(dir)).filter(!b.locations.contains(_))
    )
    .map(config.get)
    .distinct
    .forall(
      _ match {
        case None                => true
        case Some(f @ Box(locs)) => isMovePossible(List(f), dir, config)
        case _                   => false
      }
    )

def updateConfig(
    config: Config,
    boxes: List[Box],
    dir: Direction
): Config =
  if boxes.isEmpty then return config

  val nextBoxes = boxes
    .flatMap(b =>
      b.locations.map(l => l.nextLocation(dir)).filter(!b.locations.contains(_))
    )
    .map(l => config.get(l))
    .flatMap(identity)
    .map(_.asInstanceOf[Box])
    .distinct
  val newConfig = updateConfig(config, nextBoxes, dir)
  val removedBoxLocations = (boxes.flatMap(_.locations).distinct)
  val addBoxLocations = boxes
    .map(b => Box(b.locations.map(l => l.nextLocation(dir))))
    .flatMap(b => b.locations.map(l => l -> b))
  newConfig -- removedBoxLocations ++ addBoxLocations

def solve(config: Config, robot: Location, movements: Array[Direction]) =
  def helper(config: Config, robot: Location) =
    movements
      .foldLeft((config, robot)) { (acc, moveDirection) =>
        val (config, location) = acc
        val nextLocation = location.nextLocation(moveDirection)
        config.get(nextLocation) match {
          case None => (config, nextLocation)
          case Some(b @ Box(_))
              if isMovePossible(List(b), moveDirection, config) =>
            (updateConfig(config, List(b), moveDirection), nextLocation)
          case _ => acc
        }
      }

  def calculateResult(config: Config) =
    config.values.toSet
      .flatMap(_ match {
        case Box(locations) => Some(locations.minBy(_.x))
        case _              => None
      })
      .map(l => l.x + 100 * l.y)
      .sum()

  val finalState1 = helper(config, robot)
  val finalState2 = helper.apply.tupled(transformForPart2(config, robot))
  (calculateResult(finalState1(0)), calculateResult(finalState2(0)))

def printConfig(config: Config, robot: Location) =
  for i <- 0 until (config.keySet.map(_.y).max + 1) do
    for j <- 0 until (config.keySet.map(_.x).max + 1) do
      val l = Location(j, i)
      if l == robot then print("@")
      else
        config.get(l) match {
          case Some(Box(_))     => print("O")
          case Some(Obstacle()) => print("#")
          case None             => print(".")
        }
    println()
  println()

def transformForPart2(config: Config, robot: Location) =
  def expand(loc: Location) =
    List(Location(loc.x * 2, loc.y), Location(loc.x * 2 + 1, loc.y))
  val transformedConfig = config.foldLeft(Map[Location, Entity]()) {
    (acc, kv) =>
      val (loc, entity) = kv
      val additions = entity match {
        case o @ Obstacle() => expand(loc).zip(List(o, o)).toMap
        case Box(locations) =>
          val newBox = Box(expand(loc))
          newBox.locations.zip(List(newBox, newBox))
      }
      acc ++ additions
  }
  (transformedConfig, Location(robot.x * 2, robot.y))

@main def main(filename: String) =
  val (config, robot, movements) = readConfigAndMovements(filename)
  println(solve(config, robot, movements))
