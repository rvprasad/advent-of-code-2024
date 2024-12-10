Execute this code via `crystal day7.cr <filename>`

### Observations

1. Crystal implementation v1.14.0 is buggy.

   `part1 = equations.select { |k, v| isValid?(k, v[1..], v[0]) }.map { |k, _| k }.sum`
   compiles and executes fine but `part1 = equations.map { |k, v| isValid?(k, v[1..], v[0]) ? k : 0 }.sum`
   crashes with Arithmetic overflow.  Thanks to [Frank Fischer's input](https://toot.kif.rocks/@fifr/113628896104952994),
   this is a known issue in Crystal.

   Since the ternary operator uses `0`, the type of the value of the ternary
   operator is determined to be `Int32|UInt64` and then `sum()` uses `Int32`
   instead of `UInt64` as the type for the sum value.

   A user-level solution is improve the precision of the type by using `0_64` in
   the ternary operator or `sum()`.  A type-checker level solution is to
   automatically coerce the sum type to the "wider" type and issue a
   warning/notification about the coercion.  Even as is, type-checker can be
   more helpful by warning/notifying about such type coercions, i.e., from
   `Int32|UInt64` to `Int32`.
