defmodule Day3 do
  def get_memory(filename) do
    File.stream!(filename) |> Stream.map(&Function.identity/1) 
  end 

  def get_multiplicands1(line) do
    Regex.scan(~r/mul\((\d{1,3}),(\d{1,3})\)/, line) |> Enum.map(&Enum.drop(&1, 1))
  end

  def get_result(filename, get_multiplicands) do
    get_memory(filename)
    |> Enum.map(get_multiplicands)
    |> Enum.flat_map(&Function.identity/1)
    |> Enum.map(fn pair -> Enum.map(pair, &Integer.parse(&1)) |> Enum.map(&elem(&1, 0)) end)
    |> Enum.map(&Enum.product/1)
    |> Enum.sum()
  end
end

filename = System.argv() |> List.first()
Day3.get_result(filename, &Day3.get_multiplicands1/1) |> IO.puts
