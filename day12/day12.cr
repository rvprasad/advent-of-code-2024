struct Location
  property x, y

  def initialize(@x : Int32, @y : Int32)
  end
end

struct Node
  property plant, neighbor_locs

  def initialize(@plant : String, @neighbor_locs : Array(Location))
  end
end

def getFarm(filename) : Hash(Location, Node)
  lines = File.read_lines(filename)
  numRows = lines.size
  numCols = lines[0].size

  lines.zip(0..numRows)
    .reduce(Hash(Location, Node).new) { |acc, (line, y)|
      line.split("").zip(0..numCols)
        .reduce(acc) { |acc, (plant, x)|
          locations = [1, -1].map { |i| [{x + i, y}, {x, y + i}] }
            .flatten
            .select { |(x, y)| 0 <= x && x < numCols && 0 <= y && y < numRows }
            .map { |i| Location.new(*i) }
          acc[Location.new(x, y)] = Node.new(plant, locations)
          acc
        }
    }
end

def traverse(loc : Location, farm, explored, locations, perimeter)
  if explored.includes?(loc)
    {explored, locations, perimeter}
  else
    node = farm[loc]
    like_neighbors = node.neighbor_locs.select { |l|
      farm[l].plant == node.plant
    }
    node_perimeter = 4 - like_neighbors.size
    acc = {explored << loc, locations << loc, perimeter + node_perimeter}
    like_neighbors.reduce(acc) { |acc, l|
      traverse(l, farm, *acc)
    }
  end
end

def get_sides(area, farm)
  nil_node = Node.new("", [] of Location)
  area.map { |loc|
    node = farm[loc]
    like_neighbors = node.neighbor_locs.select { |l|
      farm[l].plant == node.plant
    }
    case like_neighbors.size
    when 0
      4
    when 1
      2
    else
      like_neighbors.combinations(2)
        .select { |(l1, l2)| l1.x != l2.x && l1.y != l2.y }
        .map { |e|
          # (row aligned loc, col aligned loc)
          l1, l2 = e[0].y == loc.y ? e : e.reverse
          inner_loc = [Location.new(l1.x, l2.y), Location.new(l2.x, l1.y)]
            .select { |e| e != loc }.first
          l1_mirror = Location.new(loc.x + (loc.x - inner_loc.x), loc.y)
          l2_mirror = Location.new(loc.x, loc.y + (loc.y - inner_loc.y))
          mirror_nodes = [farm.fetch(l1_mirror, nil_node),
                          farm.fetch(l2_mirror, nil_node)]

          (farm[inner_loc].plant != node.plant ? 1 : 0) + \
            (mirror_nodes.all? { |n| n.plant != node.plant } ? 1 : 0)
        }.sum
    end
  }.sum
end

def solve(farm : Hash(Location, Node))
  acc = {Set(Location).new, [] of {String, Set(Location), Int32}}
  locs_perimeter_list = farm.keys.reduce(acc) { |acc, l|
    new_explored, area, perimeter = traverse(l, farm, acc[0],
      Set(Location).new, 0)
    {new_explored, acc[1] << {farm[l].plant, area, perimeter}}
  }[1].select { |e| !e[1].empty? }
  part1 = locs_perimeter_list.map { |e| e[1].size * e[2] }.sum
  part2 = locs_perimeter_list.map { |e| e[1].size * get_sides(e[1], farm) }.sum
  {part1, part2}
end

puts solve(getFarm(ARGV[0]))
