use std::env;
use std::fs;

fn main() {
    let args: Vec<String> = env::args().collect();
    if let Ok(disk_map) = get_disk_map(&args[1]) {
        println!("{:#?}", disk_map);
        println!("{}", solve1(disk_map));
    }
}

fn solve1(disk_map: Vec<u32>) -> u64 {
    fn file_id(disk_map_pos: usize) -> u32 {
        (disk_map_pos as u32) / 2
    }
    fn at_file(disk_map_pos: usize) -> bool {
        disk_map_pos % 2 == 0
    }

    let mut front_pointer = 0;
    let mut rear_pointer = disk_map.len() - 1;
    let mut left_over_file_blocks = disk_map[rear_pointer];
    let mut block_num = 0;
    let mut checksum: u64 = 0;
    while front_pointer < rear_pointer {
        if at_file(front_pointer) {
            let curr_file_id = file_id(front_pointer);
            let file_size = disk_map[front_pointer];
            checksum += (block_num..(block_num + file_size))
                .map(|b| (b * curr_file_id) as u64)
                .sum::<u64>();
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
        let curr_file_id = file_id(front_pointer);
        checksum += (block_num..(block_num + left_over_file_blocks))
            .map(|b| (b * curr_file_id) as u64)
            .sum::<u64>();
    }

    checksum
}

fn get_disk_map(filename: &String) -> std::io::Result<Vec<u32>> {
    let tmp1 = fs::read_to_string(filename)?;
    Ok(tmp1
        .trim()
        .chars()
        .map(|s| s.to_digit(10).unwrap())
        .collect())
}
