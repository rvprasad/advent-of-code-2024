def getEquations(filename)
  File.read_lines(filename).map { |line|
    total, vals = line.split(":")
    {total.to_u64, vals.strip.split(" ").map { |i| i.to_u64 }.to_a}
  }
end

def isValidPart1?(test_value, values, curr_value)
  if values.empty?
    curr_value == test_value
  elsif curr_value <= test_value
    head, *tail = values
    isValidPart1?(test_value, tail, curr_value * head) ||
      isValidPart1?(test_value, tail, curr_value + head)
  else
    false
  end
end

def isValidPart2?(test_value, values, curr_value)
  if values.empty?
    curr_value == test_value
  elsif curr_value <= test_value
    head, *tail = values
    isValidPart2?(test_value, tail, curr_value * head) ||
      isValidPart2?(test_value, tail, curr_value + head) ||
      isValidPart2?(test_value, tail, "#{curr_value}#{head}".to_u64)
  else
    false
  end
end

def solve(equations)
  # Crystal bug results in Arithmetic overflow error
  # part1 = equations.map { |k, v| isValidPart1?(k, v[1..], v[0]) ? k : 0 }.sum

  part1 = equations.select { |k, v| isValidPart1?(k, v[1..], v[0]) }
    .map { |k, _| k }.sum

  part2 = equations.select { |k, v| isValidPart2?(k, v[1..], v[0]) }
    .map { |k, _| k }.sum
  {part1, part2}
end

puts solve(getEquations(ARGV[0]))
