import io.Source
import scala.util.Using
import scala.math.max

case class Location(x: Int, y: Int):
  def nextLocation(direction: Direction) =
    direction match {
      case Direction.Up    => Location(x, y - 1)
      case Direction.Down  => Location(x, y + 1)
      case Direction.Left  => Location(x - 1, y)
      case Direction.Right => Location(x + 1, y)
    }

enum Direction:
  case Up, Down, Right, Left

enum ObjectType:
  case Box, Obstacle

object Direction:
  private val str2dir = Map("^" -> Up, "v" -> Down, ">" -> Right, "<" -> Left)
  def from(s: String) = str2dir(s)

type Config = Map[Location, ObjectType]

def readConfigAndMovements(filename: String) = {
  val content = Source.fromFile(filename).mkString.split("\n\n")

  val lines = content(0).split("\n")
  val numRows = lines.size
  val numCols = lines(0).size
  val n = max(numRows, numCols)

  val (config, robot) = lines.zipWithIndex.foldLeft(
    (Map[Location, ObjectType](), Location(-1, -1))
  ) { (acc, e1) =>
    val (line, y) = e1
    line.zipWithIndex.foldLeft(acc) { (acc, e2) =>
      val (v, x) = e2
      val (config, robot) = acc
      val location = Location(x, y)
      v match {
        case '.' => acc
        case '@' => (config, location)
        case '#' =>
          (config + { location -> ObjectType.Obstacle }, robot)
        case 'O' =>
          (config + { location -> ObjectType.Box }, robot)
      }
    }
  }

  val movements = content(1).replace("\n", "").split("").map(Direction.from(_))
  (config, robot, movements)
}

def nextFreeLocation(
    location: Location,
    dir: Direction,
    config: Config
): Option[Location] =
  val nextLocation = location.nextLocation(dir)
  config.get(nextLocation) match
    case Some(ObjectType.Box) =>
      nextFreeLocation(nextLocation, dir, config)
    case Some(ObjectType.Obstacle) => None
    case _                         => Some(nextLocation)

def solve(
    config: Config,
    robot: Location,
    movements: Array[Direction]
): (Int, Int) =

  val part1 =
    movements
      .foldLeft((config, robot)) { (acc, moveDirection) =>
        val (config, location) = acc
        val nextLocation = location.nextLocation(moveDirection)
        config.get(nextLocation) match {
          case Some(ObjectType.Obstacle) => acc
          case Some(ObjectType.Box) =>
            nextFreeLocation(location, moveDirection, config) match {
              case None => acc
              case Some(freeLocation) =>
                (
                  (config - nextLocation) + { freeLocation -> ObjectType.Box },
                  nextLocation
                )
            }
          case _ => (config, nextLocation)
        }
      }(0)
      .filter((k, v) => v == ObjectType.Box)
      .map((l, _) => l.x + 100 * l.y)
      .sum()
  (part1, 0)

@main def main(filename: String) =
  val (config, robot, movements) = readConfigAndMovements(
    filename
  )
  println(solve(config, robot, movements))
