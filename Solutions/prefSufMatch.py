import os
import glob
import sys


def strip_lines(replacing_line, lines):
    replacing_line = replacing_line.strip()
    for i, line in enumerate(lines):
        lines[i] = line.strip()
    return replacing_line, lines


def match_prefixes(replacing_line, lines):
    replacing_line, lines = strip_lines(replacing_line, lines)
    indices = [i for i in range(len(lines))]
    for pos in range(len(replacing_line)):
        possible_indices = []
        for ind in indices:
            if len(lines[ind]) > pos and lines[ind][pos] == replacing_line[pos]:
                possible_indices.append(ind)
        if len(possible_indices) == 0:
            break
        indices = possible_indices
    return indices


def match_suffixes(replacing_line, lines):
    replacing_line, lines = strip_lines(replacing_line, lines)
    indices = [i for i in range(len(lines))]
    for pos in range(len(replacing_line)):
        possible_indices = []
        for ind in indices:
            if len(lines[ind]) > pos and lines[ind][-pos - 1] == replacing_line[-pos - 1]:
                possible_indices.append(ind)
        if len(possible_indices) == 0:
            break
        indices = possible_indices
    return indices


def print_ans(path_to_task, indices):
    print(path_to_task + " ", end='')
    for ind in indices:
        print(str(ind + 1) + " ", end='')
    print()


def main(paths):
    for path_to_dataset in paths:
        if os.path.isdir(path_to_dataset):
            path_to_tasks = os.path.join(path_to_dataset, "Tasks/")
            for task in os.listdir(path_to_tasks):
                if task.endswith(".txt"):
                    path_to_task = os.path.abspath(os.path.join(path_to_tasks, task))
                    with open(path_to_task, 'r') as file:
                        content = file.readlines()
                        replacing_line = content[0]
                        lines = content[2:]
                        prefix_indices = match_prefixes(replacing_line, lines)
                        suffix_indices = match_suffixes(replacing_line, lines)
                        if len(suffix_indices) < len(prefix_indices):
                            print_ans(path_to_task, suffix_indices)
                        else:
                            print_ans(path_to_task, prefix_indices)


if __name__ == "__main__":
    main(sys.argv[1].split(':'))
