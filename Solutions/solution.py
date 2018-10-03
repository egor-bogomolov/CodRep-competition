import os
import sys
from difflib import SequenceMatcher

def similar(a, b):
    return SequenceMatcher(None, a, b).ratio()

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

                        best = -1.
                        index = -1
                        for i, line in enumerate(lines):
                            sim = similar(line, replacing_line)
                            if sim == 1.:
                                continue
                            if sim >= best:
                                index = i
                                best = sim
                        print_ans(path_to_task, index)
                        # sol = open(path_to_task.replace('Tasks', 'Solutions'), 'r')
                        # print("answer = " + sol.readline())


if __name__ == "__main__":
    main(sys.argv[1].split(':'))
