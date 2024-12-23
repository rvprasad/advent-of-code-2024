def readInitialSecretNumber(filename)
  File.read_lines(filename).map &.to_u64
end

def generate(secret)
  tmp1 = (secret ^ (secret << 6)) % 16777216
  tmp2 = tmp1 ^ (tmp1 >> 5)
  (tmp2 ^ (tmp2 << 11)) % 16777216
end

def generateSecrets(secret, numOfGenerations)
  (0...numOfGenerations).reduce(secret) { |secret, _| generate(secret) }
end

def solve1(secrets, numOfGenerations)
  secrets.map { |secret| generateSecrets(secret, numOfGenerations) }.sum(0_u64)
end

initialSecrets = readInitialSecretNumber(ARGV[0])
numOfGenerations = ARGV[1].to_i
puts solve1(initialSecrets, numOfGenerations)
