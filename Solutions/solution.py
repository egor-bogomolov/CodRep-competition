import os
import sys
from prefSufMatch import *


def average_number(numbers):
    return sum(numbers) // len(numbers)


def first_number(numbers):
    return numbers[0]


def print_multiple_ans(path_to_task, indices):
    print(path_to_task + " ", end='')
    for ind in indices:
        print(str(ind + 1) + " ", end='')
    print()


def print_ans(path_to_task, index):
    print(path_to_task + " " + str(index + 1))


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

                        should_verify = run_verifier(path_to_task) == "OK"
                        prefix_index, pref_number = match_prefixes(replacing_line, lines, should_verify, path_to_task)
                        suffix_index, suf_number = match_suffixes(replacing_line, lines, should_verify, path_to_task)
                        if suf_number < pref_number:
                            print_ans(path_to_task, suffix_index)
                        else:
                            print_ans(path_to_task, prefix_index)


if __name__ == "__main__":
    main(sys.argv[1].split(':'))
