use std::collections::{HashMap, VecDeque};
use std::env;
use std::fs::File;
use std::io::{BufRead, BufReader};

type State = HashMap<String, i32>;
type Circuit = HashMap<(String, usize), (String, String, String)>;

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if let Ok((state, circuit)) = get_init_state_and_circuit(&args[1]) {
        println!("{:?}", solve1(state, &circuit));
        println!("{:?}", solve2(circuit));
    }
}

fn solve2(circuit: Circuit) -> String {
    let mut swapped_wires = circuit
        .iter()
        .filter_map(
            |((gate_type, _), (in1, in2, out))| match gate_type.as_str() {
                "AND" => is_invalid_and(in1, in2, out, &circuit),
                "OR" => is_invalid_or(out, &circuit),
                "XOR" => is_invalid_xor(in1, in2, out, &circuit),
                _ => panic!("How?"),
            },
        )
        .collect::<Vec<String>>();
    swapped_wires.sort();
    swapped_wires.join(",")
}

fn is_invalid_xor(in1: &String, in2: &String, out: &String, circuit: &Circuit) -> Option<String> {
    if in1.starts_with("x") && in2.starts_with("y") || in1.starts_with("y") && in2.starts_with("x")
    {
        let bit_position = &in1[1..];
        let z = format!("z{}", bit_position);
        let flag1 = bit_position == "00" && &z == out;
        let flag2 =
            find_ins(&z, String::from("XOR"), &circuit).filter(|e| &e.0 == out || &e.1 == out);
        let flag3 = find_gate_with_in(String::from("AND"), out, &circuit);
        if flag1 || flag2.is_some() || flag3.is_some() {
            None
        } else {
            Some(out.to_string())
        }
    } else if !out.starts_with("z") {
        Some(out.to_string())
    } else {
        None
    }
}

fn is_invalid_or(out: &String, circuit: &Circuit) -> Option<String> {
    let max_z = circuit
        .values()
        .filter(|e| e.2.starts_with("z"))
        .max_by_key(|e| &e.2)
        .unwrap();
    let flag1 = *out == max_z.2;
    let flag2 = find_gate_with_in(String::from("XOR"), out, circuit).is_some()
        && find_gate_with_in(String::from("AND"), out, circuit).is_some();
    if flag1 || flag2 {
        None
    } else {
        Some(out.to_string())
    }
}
fn is_invalid_and(in1: &String, in2: &String, out: &String, circuit: &Circuit) -> Option<String> {
    let flag1 = in1 == "x00" && in2 == "y00" || in1 == "y00" && in2 == "x00";
    let flag2 = find_gate_with_in(String::from("OR"), out, circuit).is_some();
    if flag1 || flag2 {
        None
    } else {
        Some(out.to_string())
    }
}

fn find_gate_with_in(
    gate_type: String,
    input: &String,
    circuit: &Circuit,
) -> Option<(String, String, String)> {
    circuit
        .iter()
        .find(|(k, v)| k.0 == *gate_type && (v.0 == *input || v.1 == *input))
        .map(|(_, v)| v.clone())
}

fn find_ins(z: &String, gate_type: String, circuit: &Circuit) -> Option<(String, String)> {
    circuit
        .iter()
        .find(|(k, v)| k.0 == *gate_type && (v.2 == *z))
        .map(|e| (e.1 .0.clone(), e.1 .1.clone()))
}

fn solve1(mut state: State, circuit: &Circuit) -> i64 {
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
