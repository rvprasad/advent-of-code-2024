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

  lines.map_with_index { |l, i| {l, i} }
    .reduce(Hash(Location, Node).new) { |acc, e1|
      line, y = e1
      line.split("").map_with_index { |c, i| {c, i} }
        .reduce(acc) { |acc, e2|
          plant, x = e2
          locations = [1, -1].map { |i| [{x + i, y}, {x, y + i}] }
            .flatten
            .select { |xy| 0 <= xy[0] && xy[0] < numCols && 0 <= xy[1] && xy[1] < numRows }
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
    like_neighbors = node.neighbor_locs.select { |l| farm[l].plant == node.plant }
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
    like_neighbors = node.neighbor_locs.select { |l| farm[l].plant == node.plant }
    case like_neighbors.size
    when 0
      4
    when 1
      2
    else
      like_neighbors.combinations(2)
        .select { |e| e[0].x != e[1].x && e[0].y != e[1].y }
        .map { |e| e[0].y == loc.y ? e : {e[1], e[0]} } # (row aligned loc, col aligned loc)
        .map { |e|
          l1, l2 = e
          inner_loc = [Location.new(l1.x, l2.y), Location.new(l2.x, l1.y)]
            .select { |e| e != loc }.first
          l1_mirror = Location.new(loc.x + (loc.x - inner_loc.x), loc.y)
          l2_mirror = Location.new(loc.x, loc.y + (loc.y - inner_loc.y))
          mirror_nodes = [farm.fetch(l1_mirror, nil_node), farm.fetch(l2_mirror, nil_node)]

          (farm[inner_loc].plant != node.plant ? 1 : 0) + \
            (mirror_nodes.all? { |n| n.plant != node.plant } ? 1 : 0)
        }.sum
    end
  }.sum
end

def solve(farm : Hash(Location, Node))
  acc = {Set(Location).new, [] of {String, Set(Location), Int32}}
  locations_perimeter_list = farm.keys.reduce(acc) { |acc, l|
    new_explored, area, perimeter = traverse(l, farm, acc[0], Set(Location).new, 0)
    {new_explored, acc[1] << {farm[l].plant, area, perimeter}}
  }[1].select { |e| !e[1].empty? }
  part1 = locations_perimeter_list.map { |e| e[1].size * e[2] }.sum
  part2 = locations_perimeter_list.map { |e| e[1].size * get_sides(e[1], farm) }.sum
  {part1, part2}
end

puts solve(getFarm(ARGV[0]))
