def getReports(filename)
  File.read_lines(filename).map { |line|
    line.split().map { |i| i.to_i }
  }
end

def zipWithNext(list)
  (list.first (list.size - 1)).zip(list.skip(1))
end

def isSafe(r) 
    diffs = zipWithNext(r).map { |a, b| a - b }
    diffs.all? { |d| 1 <= d.abs <= 3 } &&
      (diffs.all? { |d| d > 0 } || diffs.all? { |d| d < 0 })
end

def countSafeReports(reports)
  reports.select { |r| isSafe(r) }.size()
end

reports = getReports(ARGV[0])
puts "Number of safe reports #{countSafeReports(reports)}"
