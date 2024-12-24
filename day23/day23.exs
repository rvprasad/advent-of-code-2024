defmodule Day23 do
  def read_network(filename) do
    edges =
      File.stream!(filename)
      |> Stream.map(&String.trim/1)
      |> Stream.filter(&(&1 != ""))
      |> Enum.map(&String.split(&1, "-"))

    nodes = edges |> Enum.flat_map(&Function.identity/1) |> Enum.uniq()

    nodes
    |> Enum.map(fn n ->
      neighbors =
        edges
        |> Enum.filter(&(n in &1))
        |> Enum.flat_map(&Function.identity/1)
        |> Enum.uniq()
        |> Enum.filter(&(&1 != n))

      {n, neighbors}
    end)
    |> Map.new()
  end

  def combinations(list, size) do
    if Enum.count(list) <= size do
      [list]
    else
      [hd | tl] = list
      combinations(tl, size) ++ Enum.map(tl, &[hd, &1])
    end
  end

  def is_neighbor_of(src, nodes, network) do
    neighbors = Map.get(network, src)
    nodes |> Enum.all?(&(&1 in neighbors))
  end

  def solve1(network) do
    Map.keys(network)
    |> Enum.filter(&String.starts_with?(&1, "t"))
    |> Enum.map(fn n ->
      Map.get(network, n)
      |> combinations(2)
      |> Enum.filter(fn [a | [b]] ->
        is_neighbor_of(a, [n, b], network) && is_neighbor_of(b, [n, a], network)
      end)
      |> Enum.map(&Enum.sort([n | &1]))
    end)
    |> Enum.flat_map(&Function.identity/1)
    |> Enum.uniq()
    |> Enum.count()
  end
end

args = System.argv()
network = Day23.read_network(Enum.fetch!(args, 0))
Day23.solve1(network) |> IO.puts()
