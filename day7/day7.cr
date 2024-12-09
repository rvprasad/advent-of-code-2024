def getEquations(filename)
  File.read_lines(filename).map { |line|
    total, vals = line.split(":")
    {total.to_u64, vals.strip.split(" ").map { |i| i.to_u64 }.to_a}
  }
end

def isValidPart?(test_value, values, curr_value, ops)
  if values.empty?
    curr_value == test_value
  elsif curr_value <= test_value
    head, *tail = values
    ops.find(if_none = false) { |op|
      isValidPart?(test_value, tail, op.call(curr_value, head), ops)
    }
  else
    false
  end
end

def solve(equations)
  # Crystal bug results in Arithmetic overflow error
  # part1 = equations.map { |k, v| isValidPart1?(k, v[1..], v[0]) ? k : 0 }.sum

  addOp = ->(v1 : UInt64, v2 : UInt64) { v1 + v2 }
  mulOp = ->(v1 : UInt64, v2 : UInt64) { v1 * v2 }
  spliceOp = ->(v1 : UInt64, v2 : UInt64) { "#{v1}#{v2}".to_u64 }

  ops1 = [addOp, mulOp]
  part1 = equations.select { |k, v| isValidPart?(k, v[1..], v[0], ops1) }
    .map { |k, _| k }.sum

  ops2 = [addOp, mulOp, spliceOp]
  part2 = equations.select { |k, v| isValidPart?(k, v[1..], v[0], ops2) }
    .map { |k, _| k }.sum
  {part1, part2}
end

puts solve(getEquations(ARGV[0]))
