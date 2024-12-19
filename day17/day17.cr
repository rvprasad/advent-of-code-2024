require "big"

struct Registers
  property a, b, c

  def initialize(@a : BigInt, @b : BigInt, @c : BigInt)
  end
end

alias Program = Array(Int)

def getRegistersAndProgram(filename)
  values = File.read_lines(filename).reduce(Hash(String, String).new) { |acc, l|
    if l.includes?("Register")
      match = /Register (\w): (\d+)/.match!(l)
      acc[match[1]] = match[2]
    elsif l.includes?("Program")
      acc["Program"] = /Program: ([\w,]+)/.match!(l)[1]
    end
    acc
  }
  {Registers.new(BigInt.new(values["A"]), BigInt.new(values["B"]), BigInt.new(values["C"])),
   values["Program"].split(",").map &.to_i}
end

def get_value(operand, regs)
  case operand
  when 0, 1, 2, 3
    BigInt.new(operand)
  when 4
    regs.a
  when 5
    regs.b
  when 6
    regs.c
  else
    raise "How?"
  end
end

def adv_inst(regs, program, ip) : BigInt
  value = get_value(program[ip], regs)
  BigInt.new(regs.a >> value)
end

def solve1(regs, program, ip : Int32, output)
  if ip == program.size
    return output
  end

  new_ip = nil
  case program[ip]
  when 0
    regs.a = adv_inst(regs, program, ip + 1)
  when 1
    regs.b = regs.b ^ program[ip + 1]
  when 2
    regs.b = get_value(program[ip + 1], regs) % 8
  when 3
    if regs.a != 0
      new_ip = program[ip + 1]
    end
  when 4
    regs.b = regs.b ^ regs.c
  when 5
    output << get_value(program[ip + 1], regs) % 8
  when 6
    regs.b = adv_inst(regs, program, ip + 1)
  when 7
    regs.c = adv_inst(regs, program, ip + 1)
  end

  ip = new_ip ? new_ip : ip + 2
  solve1(regs, program, ip, output)
end

def solve2(regs, program)
  program.reverse.reduce({[] of Int32, [BigInt.new(0)]}) { |acc, output|
    expected_outputs = [output] + acc[0]
    new_a_values = acc[1].map { |curr_a|
      (0..7).map { |a| BigInt.new((curr_a * 8) + a) }.select { |a|
        regs.a = a
        expected_outputs == solve1(regs, program, 0, Array(BigInt).new)
      }
    }.flatten
    {expected_outputs, new_a_values}
  }[1].min
end

regs, program = getRegistersAndProgram(ARGV[0])
puts solve1(regs, program, 0, Array(BigInt).new).join(",")
puts solve2(regs, program)
