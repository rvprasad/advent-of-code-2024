import io.Source
import scala.util.Using
import scala.collection.mutable.ListBuffer

type Lock = List[Int]
type Key = List[Int]

def readLocksAndKeys(filename: String) =
  val tmp = Using(Source.fromFile(filename)) {
    _.getLines.foldLeft((List[List[String]](), List[String]())) { (acc, l) =>
      val (schemas, currentSchema) = acc
      if l.isEmpty then (schemas :+ currentSchema, List[String]())
      else (schemas, currentSchema :+ l)
    }
  }.get
  val schemas = tmp._1 :+ tmp._2

  schemas.foldLeft((List[Lock](), List[Lock]())) {
    (lockAndKeyAcc, schemaLines) =>
      val (locks, keys) = lockAndKeyAcc
      val schema = (0 until schemaLines(0).length)
        .map(i => schemaLines.filter(l => l(i) == '#').length - 1)
        .toList

      if schemaLines(0).forall(_ == '#') then (locks :+ schema, keys)
      else (locks, keys :+ schema)
  }

def solve(locks: List[Lock], keys: List[Key]) =
  locks
    .map(lock =>
      keys
        .filter(key => lock.zip(key).map((l, k) => l + k).forall(_ <= 5))
        .length
    )
    .sum

@main def main(filename: String) =
  val (locks, keys) = readLocksAndKeys(filename)
  println(solve(locks, keys))
