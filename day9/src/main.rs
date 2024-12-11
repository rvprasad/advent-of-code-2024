use std::cmp::Ordering;
use std::cmp::Reverse;
use std::collections::BinaryHeap;
use std::collections::HashMap;
use std::env;
use std::fs;
use std::hash::RandomState;

fn main() {
    let args: Vec<String> = env::args().collect();
    if let Ok(disk_map) = get_disk_map(&args[1]) {
        println!("{}", solve_part1(&disk_map));
        println!("{}", solve_part2(&disk_map));
    }
}

fn solve_part2(disk_map: &Vec<u32>) -> u64 {
    let block_num_and_size_list = get_block_num_and_size_list(disk_map);
    let mut size2free_space_heaps = create_free_space_heaps(&block_num_and_size_list);
    let mut checksum = 0u64;
    for file in get_files(&block_num_and_size_list).iter().rev() {
        if let Some(free_space) = find_free_space_for(file, &mut size2free_space_heaps) {
            checksum += calculate_checksum(free_space.block_num, file.size, file.file_id);
            if free_space.size > file.size {
                let new_free_space = FreeSpace {
                    block_num: free_space.block_num + file.size,
                    size: free_space.size - file.size,
                };
                size2free_space_heaps
                    .get_mut(&new_free_space.size)
                    .unwrap()
                    .push(Reverse(new_free_space));
            }
        } else {
            checksum += calculate_checksum(file.block_num, file.size, file.file_id);
        }
    }
    checksum
}

fn get_block_num_and_size_list(disk_map: &Vec<u32>) -> Vec<(u32, u32)> {
    disk_map
        .iter()
        .scan(0u32, |acc, e| {
            *acc += e;
            Some(*acc - e)
        })
        .zip(disk_map.clone())
        .collect::<Vec<(u32, u32)>>()
}

#[derive(Debug, Copy, Clone)]
struct FreeSpace {
    block_num: u32,
    size: u32,
}

impl Ord for FreeSpace {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        self.block_num.cmp(&other.block_num)
    }
}

impl PartialOrd for FreeSpace {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl PartialEq for FreeSpace {
    fn eq(&self, other: &Self) -> bool {
        self.block_num == other.block_num && self.size == other.size
    }
}

impl Eq for FreeSpace {}

fn create_free_space_heaps(
    block_num_and_size: &Vec<(u32, u32)>,
) -> HashMap<u32, BinaryHeap<Reverse<FreeSpace>>, RandomState> {
    let mut result: HashMap<u32, BinaryHeap<Reverse<FreeSpace>>, RandomState> =
        HashMap::from_iter((1u32..10u32).map(|i| (i, BinaryHeap::new())));
    for chunk in block_num_and_size.chunks_exact(2) {
        let (block_num, size) = chunk[1];
        result
            .get_mut(&size)
            .iter_mut()
            .for_each(|x| x.push(Reverse(FreeSpace { block_num, size })));
    }
    result
}

#[derive(Debug)]
struct File {
    block_num: u32,
    size: u32,
    file_id: u32,
}

fn get_files(block_num_and_size: &Vec<(u32, u32)>) -> Vec<File> {
    block_num_and_size
        .chunks(2)
        .enumerate()
        .map(|(i, chunk)| File {
            block_num: chunk[0].0,
            size: chunk[0].1,
            file_id: i as u32,
        })
        .collect()
}

fn find_free_space_for(
    file: &File,
    size2free_space_heap: &mut HashMap<u32, BinaryHeap<Reverse<FreeSpace>>, RandomState>,
) -> Option<FreeSpace> {
    (file.size..10)
        .map(|s| size2free_space_heap[&s].peek())
        .flatten()
        .map(|x| x.0)
        .filter(|f| f.block_num < file.block_num)
        .min_by_key(|f| f.block_num)
        .map(|free_space| {
            size2free_space_heap
                .get_mut(&free_space.size)
                .unwrap()
                .pop();
            free_space
        })
}

fn solve_part1(disk_map: &Vec<u32>) -> u64 {
    let at_file = |disk_map_pos: usize| disk_map_pos % 2 == 0;
    let file_id = |disk_map_pos: usize| (disk_map_pos as u32) / 2;

    let mut front_pointer = 0;
    let mut rear_pointer = disk_map.len() - 1;
    let mut left_over_file_blocks = disk_map[rear_pointer];
    let mut block_num = 0;
    let mut checksum: u64 = 0;
    while front_pointer < rear_pointer {
        if at_file(front_pointer) {
            let file_size = disk_map[front_pointer];
            checksum += calculate_checksum(block_num, file_size, file_id(front_pointer));
            block_num += file_size;
            front_pointer += 1;
        } else {
            let free_space = disk_map[front_pointer];
            for curr_block_num in block_num..(block_num + free_space) {
                if left_over_file_blocks == 0 {
                    rear_pointer -= 2;
                    if rear_pointer < front_pointer {
                        break;
                    }

                    left_over_file_blocks = disk_map[rear_pointer];
                }

                let curr_file_id = file_id(rear_pointer);
                checksum += (curr_block_num * curr_file_id) as u64;
                left_over_file_blocks -= 1;
            }
            block_num += free_space;
            front_pointer += 1;
        }
    }
    if front_pointer == rear_pointer {
        checksum += calculate_checksum(block_num, left_over_file_blocks, file_id(front_pointer))
    }

    checksum
}

fn calculate_checksum(start_block: u32, num_blocks: u32, file_id: u32) -> u64 {
    (start_block..(start_block + num_blocks))
        .map(|b| (b * file_id) as u64)
        .sum::<u64>()
}

fn get_disk_map(filename: &String) -> std::io::Result<Vec<u32>> {
    let tmp1 = fs::read_to_string(filename)?;
    Ok(tmp1
        .trim()
        .chars()
        .map(|s| s.to_digit(10).unwrap())
        .collect())
}
