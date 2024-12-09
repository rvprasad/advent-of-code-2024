Execute this code via `crystal day7.cr <filename>`

### Observations

1. Crystal implementation v1.14.0 is buggy.

   `part1 = equations.select { |k, v| isValid?(k, v[1..], v[0]) }.map { |k, _| k }.sum` compiles and executes fine but `part1 = equations.map { |k, v| isValid?(k, v[1..], v[0]) ? k : 0 }.sum` crashes with Arithmetic overflow.
