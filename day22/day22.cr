def readInitialSecretNumber(filename)
  File.read_lines(filename).map &.to_i64
end

def generate(secret)
  tmp1 = (secret ^ (secret << 6)) % 16777216
  tmp2 = (tmp1 ^ (tmp1 >> 5)) % 16777216
  (tmp2 ^ (tmp2 << 11)) % 16777216
end

def generateSecrets(secret, numOfGenerations)
  (0...numOfGenerations).accumulate(secret) { |secret, _| generate(secret) }
end

def solve1(secrets, numOfGenerations)
  secrets.map { |secret|
    generateSecrets(secret, numOfGenerations)[numOfGenerations]
  }.sum(0_u64)
end

def getValidPriceChangeSeqToPrice(priceChanges, prices)
  (0..(priceChanges.size - 4))
    .map { |i|
      {priceChanges[i..(i + 3)].join(""), prices[i + 4]}
    }
    .uniq { |e| e[0] }
    .to_h
end

def solve2(initialSecrets, numOfGenerations)
  secret2prices = initialSecrets.to_h { |secret|
    {secret, generateSecrets(secret, numOfGenerations).map { |s| s % 10 }}
  }

  secret2priceChanges = secret2prices.to_h { |s2p|
    {s2p[0], s2p[1][..-2].zip(s2p[1][1..]).map { |p| p[1] - p[0] }}
  }

  secret2priceChangeSeq2Price = initialSecrets.to_h { |secret|
    {secret, getValidPriceChangeSeqToPrice(secret2priceChanges[secret],
      secret2prices[secret])}
  }

  secret2priceChangeSeq2Price.values.map { |v| v.keys }
    .flatten
    .uniq
    .map { |seq|
      initialSecrets.map { |s|
        secret2priceChangeSeq2Price[s].fetch(seq, 0)
      }.sum
    }.max
end

initialSecrets = readInitialSecretNumber(ARGV[0])
numOfGenerations = ARGV[1].to_i
puts solve1(initialSecrets, numOfGenerations)
puts solve2(initialSecrets, numOfGenerations)
