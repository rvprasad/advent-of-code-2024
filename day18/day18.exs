defmodule Day18 do
  def get_byte_positions(filename) do
    File.stream!(filename)
    |> Stream.map(&String.trim/1)
    |> Stream.filter(&(&1 != ""))
    |> Enum.map(fn l ->
      l
      |> String.split(",")
      |> Enum.map(&String.to_integer/1)
      |> List.to_tuple()
    end)
  end

  defp print(byte_positions, num_cols, num_rows) do
    0..num_rows
    |> Enum.each(fn y ->
      0..num_cols
      |> Enum.each(fn x ->
        if {x, y} in byte_positions do
          IO.write("#")
        else
          IO.write(".")
        end
      end)

      IO.puts("")
    end)
  end

  defp get_next_nodes(node, byte_positions, seen_nodes, num_cols, num_rows) do
    {col, row} = node

    (([1, -1]
      |> Enum.map(&(col + &1))
      |> Enum.filter(&(0 <= &1 && &1 <= num_cols))
      |> Enum.map(&{&1, row})) ++
       ([1, -1]
        |> Enum.map(&(row + &1))
        |> Enum.filter(&(0 <= &1 && &1 <= num_rows))
        |> Enum.map(&{col, &1})))
    |> Enum.filter(&(!MapSet.member?(seen_nodes, &1) && !Enum.member?(byte_positions, &1)))
  end

  defp get_shortest_path(_byte_positions, _num_cols, _num_rows, [], _seen_nodes) do
    -1
  end

  defp get_shortest_path(
         byte_positions,
         num_cols,
         num_rows,
         [work_item | workList],
         seen_nodes
       ) do
    {curr_node, path_length} = work_item

    if curr_node == {num_cols, num_rows} do
      path_length
    else
      next_nodes = get_next_nodes(curr_node, byte_positions, seen_nodes, num_cols, num_rows)
      next_work_items = next_nodes |> Enum.map(&{&1, path_length + 1})

      get_shortest_path(
        byte_positions,
        num_cols,
        num_rows,
        workList ++ next_work_items,
        MapSet.union(seen_nodes, MapSet.new(next_nodes))
      )
    end
  end

  def solve1(byte_positions, num_of_bytes_to_consider) do
    num_rows = byte_positions |> Enum.map(&elem(&1, 1)) |> Enum.max()
    num_cols = byte_positions |> Enum.map(&elem(&1, 0)) |> Enum.max()

    byte_positions
    |> Enum.take(num_of_bytes_to_consider)
    |> get_shortest_path(num_cols, num_rows, [{{0, 0}, 0}], MapSet.new([{0, 0}]))
  end

  def solve2(byte_positions, lo, hi) do
    if lo == hi - 1 do
      Enum.at(byte_positions, lo) |> Tuple.to_list() |> Enum.join(",")
    else
      mid = trunc((lo + hi) / 2)

      if solve1(byte_positions, mid) == -1 do
        solve2(byte_positions, lo, mid)
      else
        solve2(byte_positions, mid, hi)
      end
    end
  end
end

args = System.argv()
byte_positions = Day18.get_byte_positions(Enum.fetch!(args, 0))
num_of_bytes_to_consider = String.to_integer(Enum.fetch!(args, 1))
Day18.solve1(byte_positions, num_of_bytes_to_consider) |> IO.puts()
Day18.solve2(byte_positions, 0, Enum.count(byte_positions) - 1) |> IO.puts()
