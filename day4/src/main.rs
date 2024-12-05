use regex::Regex;
use std::env;
use std::fs::File;
use std::io::{BufRead, BufReader};

fn main() {
    let args: Vec<String> = env::args().collect();
    if let Ok(lines) = get_content(&args[1]) {
        println!("{}", solve_first_part(&lines));
        println!("{}", solve_second_part(&lines));
    }
}
fn get_content(filename: &String) -> std::io::Result<Vec<Vec<char>>> {
    let f = File::open(filename)?;
    let reader = BufReader::new(f);
    let lines = reader
        .lines()
        .flatten()
        .filter_map(|l| {
            if l.is_empty() {
                None
            } else {
                Some(l.chars().collect())
            }
        })
        .collect();
    Ok(lines)
}

fn solve_first_part(orig_lines: &Vec<Vec<char>>) -> i32 {
    fn get_lines_in_different_orientations(orig_lines: &Vec<Vec<char>>) -> Vec<String> {
        let mut lines = Vec::<String>::new();
        // horizontal lines
        lines.extend(orig_lines.iter().map(|l| l.iter().collect()));

        // vertical lines
        lines.extend(get_vertical_lines(orig_lines));

        // top-left to bottom-right diagonal lines
        lines.extend(get_diagonal_lines(orig_lines));

        // top-right to bottom-left diagonal lines
        let mut tmp = orig_lines.clone();
        tmp.reverse();
        lines.extend(get_diagonal_lines(&tmp));

        lines
    }

    fn get_vertical_lines(orig_lines: &Vec<Vec<char>>) -> Vec<String> {
        let num_cols = orig_lines.first().unwrap().len();
        let mut lines: Vec<Vec<char>> = (0..num_cols).map(|_| Vec::<char>::new()).collect();
        for l in orig_lines {
            for i in 0..num_cols {
                lines[i].push(l[i]);
            }
        }
        lines.iter().map(|l| l.iter().collect()).collect()
    }

    fn get_diagonal_lines(orig_lines: &Vec<Vec<char>>) -> Vec<String> {
        let num_cols = orig_lines.first().unwrap().len();
        let mut diag_lines1: Vec<Vec<char>> = (0..num_cols).map(|_| Vec::<char>::new()).collect();
        for r in 0..num_cols {
            for (l, ch) in orig_lines[r][r..num_cols].iter().enumerate() {
                diag_lines1[l].push(*ch);
            }
        }

        let num_rows = orig_lines.len();
        let mut diag_lines2: Vec<Vec<char>> =
            (0..(num_rows - 1)).map(|_| Vec::<char>::new()).collect();
        for r in 1..num_rows {
            for (l, ch) in orig_lines[r][0..r].iter().rev().enumerate() {
                diag_lines2[l].push(*ch);
            }
        }

        return Vec::from_iter(
            diag_lines1
                .iter()
                .map(|l| l.iter().collect())
                .chain(diag_lines2.iter().map(|l| l.iter().collect())),
        );
    }

    get_lines_in_different_orientations(orig_lines)
        .iter()
        .map(|l| {
            Regex::new("XMAS").unwrap().find_iter(l).count()
                + Regex::new("SAMX").unwrap().find_iter(l).count()
        })
        .sum::<usize>()
        .try_into()
        .unwrap()
}

fn solve_second_part(orig_lines: &Vec<Vec<char>>) -> i32 {
    let num_cols = orig_lines.first().unwrap().len();
    let num_rows = orig_lines.len();
    let mut count = 0;
    for r in 0..num_rows {
        for c in 0..num_cols {
            if orig_lines[r][c] == 'A' && c > 0 && c < num_cols - 1 && r > 0 && r < num_rows - 1 {
                let lt = orig_lines[r - 1][c - 1];
                let lb = orig_lines[r + 1][c - 1];
                let rt = orig_lines[r - 1][c + 1];
                let rb = orig_lines[r + 1][c + 1];
                if ((lt == 'M' && rb == 'S') || (lt == 'S' && rb == 'M'))
                    && ((rt == 'M' && lb == 'S') || (rt == 'S' && lb == 'M'))
                {
                    count += 1;
                }
            }
        }
    }
    count
}
