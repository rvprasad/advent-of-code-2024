defmodule Day3 do
  def get_memory(filename) do
    File.stream!(filename) 
    |> Stream.map(&String.trim_trailing/1) 
    |> Enum.join("")
  end

  def get_multiplicands1(line) do
    Regex.scan(~r/mul\((\d{1,3}),(\d{1,3})\)/, line) 
    |> Enum.map(&Enum.drop(&1, 1))
  end

  def get_multiplicands2(line) do
    Regex.scan(~r/do\(\)(.*?)(don't\(\)|$)/, "do()" <> line)
    |> Enum.map(&Enum.at(&1, 1))
    |> Enum.join("")
    |> then(&Regex.scan(~r/mul\((\d{1,3}),(\d{1,3})\)/, &1))
    |> Enum.map(&Enum.drop(&1, 1))
  end

  def get_result(filename, get_multiplicands) do
    get_memory(filename)
    |> get_multiplicands.()
    |> Enum.map(fn pair ->
      Enum.map(pair, &Integer.parse(&1))
      |> Enum.map(&elem(&1, 0))
    end)
    |> Enum.map(&Enum.product/1)
    |> Enum.sum()
  end
end

filename = System.argv() |> List.first()
Day3.get_result(filename, &Day3.get_multiplicands1/1) |> IO.puts()
Day3.get_result(filename, &Day3.get_multiplicands2/1) |> IO.puts()
