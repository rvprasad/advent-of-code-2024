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
        |> MapSet.new()

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

  def get_3_clique(network) do
    Map.keys(network)
    |> Enum.map(fn n ->
      Map.get(network, n)
      |> MapSet.to_list()
      |> combinations(2)
      |> Enum.filter(fn [a | [b]] ->
        is_neighbor_of(a, [n, b], network) && is_neighbor_of(b, [n, a], network)
      end)
      |> Enum.map(&Enum.sort([n | &1]))
    end)
    |> Enum.flat_map(&Function.identity/1)
    |> Enum.uniq()
  end

  def solve1(network) do
    get_3_clique(network)
    |> Enum.filter(fn c -> Enum.any?(c, &String.starts_with?(&1, "t")) end)
    |> Enum.count()
  end

  def grow_cliques(clique, network, seen_cliques) do
    common_neighbors =
      clique
      |> Enum.reduce(nil, fn c, acc ->
        if acc == nil do
          Map.get(network, c)
        else
          MapSet.intersection(acc, Map.get(network, c))
        end
      end)
      |> MapSet.difference(clique)

    if Enum.empty?(common_neighbors) do
      seen_cliques
    else
      new_cliques =
        common_neighbors
        |> Enum.map(&MapSet.put(clique, &1))
        |> Enum.filter(&(!MapSet.member?(seen_cliques, &1)))
        |> MapSet.new()

      new_seen_cliques = MapSet.union(seen_cliques, new_cliques)

      new_cliques
      |> Enum.reduce(new_seen_cliques, fn cl, acc ->
        grow_cliques(cl, network, acc)
      end)
    end
  end

  def solve2(network) do
    get_3_clique(network)
    |> Enum.reduce(MapSet.new(), fn cl, cliques ->
      grow_cliques(MapSet.new(cl), network, cliques)
    end)
    |> Enum.max_by(&Enum.count/1)
    |> Enum.sort()
    |> Enum.join(",")
  end
end

args = System.argv()
network = Day23.read_network(Enum.fetch!(args, 0))
Day23.solve1(network) |> IO.puts()
Day23.solve2(network) |> IO.puts()
