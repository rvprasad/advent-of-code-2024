def getEquations(filename)
  File.read_lines(filename).map { |line|
    total, vals = line.split(":")
    {total.to_u64, vals.strip.split(" ").map { |i| i.to_u64 }.to_a}
  }
end

def isValid?(test_value, values, curr_value)
  if values.empty?
    curr_value == test_value
  elsif curr_value <= test_value
    head, *tail = values
    isValid?(test_value, tail, curr_value * head) ||
      isValid?(test_value, tail, curr_value + head)
  else
    false
  end
end

def solve(equations)
  part1 = equations.select { |k, v| isValid?(k, v[1..], v[0]) }
    .map { |k, _| k }.sum

  # Crystal bug results in Arithmetic overflow error
  # part1 = equations.map { |k, v| isValid?(k, v[1..], v[0]) ? k : 0 }.sum

  {part1}
end

puts solve(getEquations(ARGV[0]))
