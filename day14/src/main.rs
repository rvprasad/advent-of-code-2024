use regex::Regex;
use std::env;
use std::fs::File;
use std::io::{BufRead, BufReader};

fn main() {
    let args = env::args().collect::<Vec<String>>();
    let num_cols = args[2].parse::<i32>().unwrap();
    let num_rows = args[3].parse::<i32>().unwrap();
    let seconds = args[4].parse::<i32>().unwrap();
    if let Ok(robots) = get_robots(&args[1]) {
        println!("{}", solve_part1(&robots, num_rows, num_cols, seconds));
        println!("{}", solve_part2(&robots, num_rows, num_cols));
    }
}

fn solve_part2(robots: &Vec<Robot>, num_rows: i32, num_cols: i32) -> usize {
    (1..(num_cols * num_rows))
        .into_iter()
        .min_by_key(|seconds| solve_part1(robots, num_rows, num_cols, *seconds))
        .unwrap()
        .try_into()
        .unwrap()
}

fn solve_part1(robots: &Vec<Robot>, num_rows: i32, num_cols: i32, seconds: i32) -> usize {
    let locations = robots
        .iter()
        .map(|robot| {
            let x = (robot.start_x + (robot.vel_x * seconds)).rem_euclid(num_cols);
            let y = (robot.start_y + (robot.vel_y * seconds)).rem_euclid(num_rows);
            (x, y)
        })
        .collect::<Vec<(i32, i32)>>();

    calculate_safety_factor(locations, num_rows, num_cols)
}

fn calculate_safety_factor(locations: Vec<(i32, i32)>, num_rows: i32, num_cols: i32) -> usize {
    let mid_row = num_rows / 2;
    let mid_col = num_cols / 2;
    vec![
        (0, mid_col, 0, mid_row),
        (0, mid_col, mid_row + 1, num_rows),
        (mid_col + 1, num_cols, 0, mid_row),
        (mid_col + 1, num_cols, mid_row + 1, num_rows),
    ]
    .into_iter()
    .map(|(x_lo, x_hi, y_lo, y_hi)| {
        locations
            .iter()
            .filter(|(x, y)| (x_lo <= *x && x < &x_hi && y_lo <= *y && y < &y_hi))
            .count()
    })
    .product()
}

#[derive(Debug, Copy, Clone)]
struct Robot {
    start_x: i32,
    start_y: i32,
    vel_x: i32,
    vel_y: i32,
}

fn get_robots(filename: &String) -> std::io::Result<Vec<Robot>> {
    let regex = Regex::new(r"p=(\d+),(\d+) v=(-?\d+),(-?\d+)").unwrap();
    let robots = BufReader::new(File::open(filename)?)
        .lines()
        .flatten()
        .filter_map(|l| {
            let t = regex.captures(&l).map(|c| c.extract::<4>());
            t.map(|(_, vals)| Robot {
                start_x: vals[0].parse::<i32>().unwrap(),
                start_y: vals[1].parse::<i32>().unwrap(),
                vel_x: vals[2].parse::<i32>().unwrap(),
                vel_y: vals[3].parse::<i32>().unwrap(),
            })
        })
        .collect();
    Ok(robots)
}
