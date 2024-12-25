use std::collections::{HashMap, HashSet, VecDeque};
use std::env;
use std::fs::File;
use std::io::{BufRead, BufReader};

type State = HashMap<String, i32>;
type Circuit = HashMap<(String, usize), (String, String, String)>;

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if let Ok((state, circuit)) = get_init_state_and_circuit(&args[1]) {
        println!("{:?}", solve(state, circuit))
    }
}

fn solve(mut state: State, circuit: Circuit) -> i64 {
    let mut gates: VecDeque<&(String, usize)> =
        VecDeque::<&(String, usize)>::from(circuit.keys().collect::<Vec<&(String, usize)>>());
    while !gates.is_empty() {
        let gate_id = gates.pop_front().unwrap();
        let gate = circuit.get(gate_id).unwrap();
        let v1_opt = state.get(&gate.0);
        let v2_opt = state.get(&gate.1);
        if v1_opt.is_some() && v2_opt.is_some() {
            let v1 = v1_opt.unwrap();
            let v2 = v2_opt.unwrap();
            let out = gate.2.clone();
            match gate_id.0.as_str() {
                "AND" => state.insert(out, v1 & v2),
                "OR" => state.insert(out, v1 | v2),
                "XOR" => state.insert(out, v1 ^ v2),
                &_ => panic!("How?"),
            };
        } else {
            gates.push_back(gate_id);
        }
    }
    let mut z_entries = state.iter().collect::<Vec<(&String, &i32)>>();
    z_entries.sort_by_key(|e| e.0);
    z_entries.reverse();
    z_entries
        .iter()
        .filter(|e| e.0.starts_with("z"))
        .fold(0_i64, |acc, e| acc * 2 + i64::from(*e.1))
}

fn get_init_state_and_circuit(filename: &String) -> std::io::Result<(State, Circuit)> {
    let mut state = State::new();
    let mut circuit = Circuit::new();
    let lines = BufReader::new(File::open(filename)?).lines().flatten();

    for (i, line) in lines.enumerate() {
        if line.contains(":") {
            let mut tmp1 = line.split(":").map(String::from);
            state.insert(
                tmp1.next().unwrap(),
                tmp1.next().unwrap().trim().parse::<i32>().unwrap(),
            );
        } else if line.contains("->") {
            let mut tmp1 = line.split(" ").map(String::from);
            let in1 = tmp1.next().unwrap();
            let gate = (tmp1.next().unwrap(), i);
            let in2 = tmp1.next().unwrap();
            tmp1.next();
            let out = tmp1.next().unwrap();
            circuit.insert(gate, (in1, in2, out));
        }
    }
    Ok((state, circuit))
}
