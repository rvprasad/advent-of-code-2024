defmodule Day8 do
  defp get_antennas(filename) do
    lines =
      File.stream!(filename)
      |> Stream.map(&String.trim/1)
      |> Enum.map(fn l -> String.split(l, "") |> Enum.filter(&(&1 != "")) end)

    antennas =
      Enum.with_index(lines)
      |> Enum.map(fn {line, row} ->
        Enum.with_index(line)
        |> Enum.map(fn {elem, col} -> {row, col, elem} end)
      end)
      |> List.flatten()
      |> Enum.filter(fn {_, _, elem} -> elem != "." end)

    {lines |> Enum.count(), lines |> Enum.fetch!(0) |> Enum.count(), antennas}
  end

  defp get_anti_nodes(r1, c1, r2, c2, n) do
    delta_r = abs(r1 - r2)
    delta_c = abs(c1 - c2)

    {r1_delta, r2_delta} = if(r1 < r2, do: {-delta_r, delta_r}, else: {delta_r, -delta_r})
    {c1_delta, c2_delta} = if(c1 < c2, do: {-delta_c, delta_c}, else: {delta_c, -delta_c})

    1..n
    |> Enum.map(fn i ->
      [
        {r1 + r1_delta * i, c1 + c1_delta * i},
        {r2 + r2_delta * i, c2 + c2_delta * i}
      ]
    end)
  end

  defp get_anti_nodes_of_group(antennas, num_rows, num_cols, n) do
    Enum.map(0..(Enum.count(antennas) - 1), fn i ->
      [head | tail] = Enum.drop(antennas, i)
      tail |> Enum.map(&{head, &1})
    end)
    |> List.flatten()
    |> Enum.map(fn {{r1, c1, _}, {r2, c2, _}} -> get_anti_nodes(r1, c1, r2, c2, n) end)
    |> List.flatten()
    |> Enum.filter(fn {r, c} -> r >= 0 && r < num_rows && c >= 0 && c < num_cols end)
  end

  def solve(filename) do
    {num_rows, num_cols, antennas} = get_antennas(filename)
    frequency_2_antennas = Enum.group_by(antennas, fn {_, _, e} -> e end)

    part1 =
      Map.values(frequency_2_antennas)
      |> Enum.map(&get_anti_nodes_of_group(&1, num_rows, num_cols, 1))
      |> List.flatten()
      |> Enum.uniq()
      |> Enum.count()

    part2 =
      Map.values(frequency_2_antennas)
      |> Enum.map(&get_anti_nodes_of_group(&1, num_rows, num_cols, max(num_rows, num_cols)))
      |> List.flatten()
      |> Enum.concat(antennas |> List.flatten() |> Enum.map(fn {r, c, _} -> {r, c} end))
      |> Enum.uniq()
      |> Enum.count()

    {part1, part2}
  end
end

filename = System.argv() |> Enum.fetch!(0)
Day8.solve(filename) |> Tuple.to_list() |> Enum.join(" ") |> IO.puts()
