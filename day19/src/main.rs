use std::collections::{HashMap, HashSet};
use std::env;
use std::fs::File;
use std::io::{BufRead, BufReader};

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if let Ok((patterns, designs)) = get_patterns_and_designs(&args[1]) {
        println!("{:?}", solve(&patterns, &designs))
    }
}

fn solve(patterns: &HashSet<String>, designs: &Vec<String>) -> (usize, u64) {
    fn calculate_num_of_realizations<'a>(
        design: &'a str,
        patterns: &'a HashSet<String>,
        feasible_designs: &mut HashMap<&'a str, u64>,
        infeasible_designs: &mut HashSet<&'a str>,
    ) -> u64 {
        if feasible_designs.contains_key(design) {
            return feasible_designs[design];
        }

        let num_of_realizations = patterns
            .iter()
            .filter_map(|p| design.strip_prefix(p).map(|s| (p, s)))
            .map(|(_, suffix)| {
                calculate_num_of_realizations(
                    suffix,
                    patterns,
                    feasible_designs,
                    infeasible_designs,
                ) as u64
            })
            .sum::<u64>();

        feasible_designs.insert(design, num_of_realizations);
        num_of_realizations
    }

    let mut feasible_designs_to_num_of_realizations = HashMap::<&str, u64>::new();
    feasible_designs_to_num_of_realizations.insert("", 1);
    for d in designs {
        calculate_num_of_realizations(
            d,
            &patterns,
            &mut feasible_designs_to_num_of_realizations,
            &mut HashSet::<&str>::new(),
        );
    }

    designs.iter().fold((0, 0), |acc, d| {
        let tmp = feasible_designs_to_num_of_realizations[d.as_str()];
        (acc.0 + if tmp > 0u64 { 1 } else { 0 }, acc.1 + tmp)
    })
}

fn get_patterns_and_designs(filename: &String) -> std::io::Result<(HashSet<String>, Vec<String>)> {
    let (patterns, designs) = BufReader::new(File::open(filename)?)
        .lines()
        .flatten()
        .fold(
            (HashSet::<String>::new(), vec![]),
            |(mut patterns, mut designs), line| {
                if line.contains(",") {
                    patterns.extend(line.split(", ").map(String::from));
                } else if !line.is_empty() {
                    designs.push(line);
                }
                (patterns, designs)
            },
        );

    Ok((patterns, designs))
}
