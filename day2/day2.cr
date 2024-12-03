def getReports(filename)
  File.read_lines(filename).map { |line|
    line.split.map { |i| i.to_i }
  }
end

def zipWithNext(list)
  (list.first(list.size - 1)).zip(list.skip(1))
end

def safe?(r)
  diffs = zipWithNext(r).map { |a, b| a - b }
  diffs.all? { |d| 1 <= d.abs <= 3 } &&
    (diffs.all? { |d| d > 0 } || diffs.all? { |d| d < 0 })
end

def countSafeReports(reports)
  reports.select { |r| safe?(r) }.size
end

def intolerable?(diffs)
  zeroes = diffs.select { |d| d == 0 }.size
  positives = diffs.select { |d| d > 0 }.size
  negatives = diffs.select { |d| d < 0 }.size
  big_diffs = diffs.select { |d| d.abs > 3 }.size
  zeroes > 1 ||                      # more than one zero diff cannot be fixed
    big_diffs > 2 ||                 # more than two big diffs cannot be fixed; two consecutive big diffs may be fixed
    (big_diffs > 0 && zeroes > 0) || # a zero diff and a big diff cannot be fixed
    (positives > 1 && negatives > 1) # more than one positive diff and one negative diff cannot be fixed
end

def safeAfterDeletion?(r, idx)
  safe?(r.dup.tap { |x| x.delete_at(idx) })
end

def tolerablySafe?(r)
  diffs = zipWithNext(r).map { |a, b| a - b }
  if intolerable?(diffs)
    return false
  end

  zero_index = diffs.index { |e| e == 0 }
  if zero_index
    return safeAfterDeletion?(r, zero_index)
  end

  big_diff_index = diffs.index { |e| e.abs > 3 }
  if big_diff_index
    return safeAfterDeletion?(r, big_diff_index) || safeAfterDeletion?(r, big_diff_index + 1)
  end

  out_of_order_index = zipWithNext(diffs).index { |(a, b)| a.positive? ^ b.positive? }
  if out_of_order_index
    return safeAfterDeletion?(r, out_of_order_index) ||
      safeAfterDeletion?(r, out_of_order_index + 1) ||
      safeAfterDeletion?(r, out_of_order_index + 2)
  end

  raise "A report slipped through our check!! #{r}"
end

def countTolerablySafeReports(reports)
  reports.select { |r| safe?(r) || tolerablySafe?(r) }.size
end

reports = getReports(ARGV[0])
puts "Number of safe reports #{countSafeReports(reports)}"
puts "Number of tolerable safe reports #{countTolerablySafeReports(reports)}"
