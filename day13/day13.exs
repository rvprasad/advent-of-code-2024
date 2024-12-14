defmodule Day13 do
  defp convert_to_tuple(matches) do
    Enum.split(matches, 1)
    |> elem(1)
    |> Enum.map(&String.to_integer/1)
    |> List.to_tuple()
  end

  defp get_machine_behavior(filename) do
    regex = ~r/(.*): X.(\d+), Y.(\d+)/

    File.stream!(filename)
    |> Stream.map(&String.trim/1)
    |> Stream.filter(&(&1 != ""))
    |> Enum.reduce([], fn l, acc ->
      matches = Regex.run(regex, l, capture: :all_but_first)

      case hd(matches) do
        "Button A" -> [%{:a => convert_to_tuple(matches)}] ++ acc
        "Button B" -> [Map.put(hd(acc), :b, convert_to_tuple(matches))] ++ tl(acc)
        "Prize" -> [Map.put(hd(acc), :p, convert_to_tuple(matches))] ++ tl(acc)
      end
    end)
  end

  defp solve_equation1(equation) do
    {a1, a2} = equation[:a]
    {b1, b2} = equation[:b]
    {p1, p2} = equation[:p]
    a1b2_minus_a2b1 = a1 * b2 - a2 * b1

    if a1b2_minus_a2b1 == 0 do
      nil
    else
      y = (p2 * a1 - p1 * a2) / a1b2_minus_a2b1
      x = (p1 - b1 * y) / a1

      if floor(x) == x && floor(y) == y do
        {x, y}
      else
        nil
      end
    end
  end

  defp solve_equation2(equation) do
    {p1, p2} = equation[:p]
    new_p1 = 10_000_000_000_000 + p1
    new_p2 = 10_000_000_000_000 + p2
    solve_equation1(%{equation | :p => {new_p1, new_p2}})
  end

  def helper(machine_behaviors, fun) do
    machine_behaviors
    |> Enum.map(fun)
    |> Enum.reject(&is_nil/1)
    |> Enum.map(&(elem(&1, 0) * 3 + elem(&1, 1)))
    |> Enum.sum()
  end

  def solve(filename) do
    machine_behaviors = get_machine_behavior(filename)

    part1 = helper(machine_behaviors, &solve_equation1/1)
    part2 = helper(machine_behaviors, &solve_equation2/1)
    {part1, part2}
  end
end

System.argv() |> Enum.fetch!(0) |> Day13.solve() |> Tuple.to_list() |> Enum.join(" ") |> IO.puts()
