from javaVerifier import run_verifier
import numpy as np


def prepare_lines(replacing_line, lines):
    replacing_line = replacing_line.strip()
    for i, line in enumerate(lines):
        lines[i] = line.strip()
        if lines[i] == replacing_line:
            lines[i] = ""
    return replacing_line, lines


max_top_check = 10


def match_prefixes(replacing_line, lines, should_verify=False, path_to_task=None):
    replacing_line, lines = prepare_lines(replacing_line, lines)
    indices = [i for i in range(len(lines))]
    drop_time = [0 for _ in range(len(lines))]
    for pos in range(len(replacing_line)):
        possible_indices = []
        for ind in indices:
            if len(lines[ind]) > pos and lines[ind][pos] == replacing_line[pos]:
                possible_indices.append(ind)
            else:
                drop_time[ind] = pos
        if len(possible_indices) == 0:
            break
        indices = possible_indices

    if should_verify and len(indices):
        order = reversed(np.argsort(drop_time))
        num = 1
        for index in order:
            if num >= max_top_check:
                break
            result = run_verifier(path_to_task, should_replace=True, line_number=index + 1)
            if result == "OK":
                return index, len(indices)
            num += 1

    return indices[0], len(indices)


def match_suffixes(replacing_line, lines, should_verify=False, path_to_task=None):
    replacing_line, lines = prepare_lines(replacing_line, lines)
    indices = [i for i in range(len(lines))]
    drop_time = [0 for _ in range(len(lines))]
    for pos in range(len(replacing_line)):
        possible_indices = []
        for ind in indices:
            if len(lines[ind]) > pos and lines[ind][-pos - 1] == replacing_line[-pos - 1]:
                possible_indices.append(ind)
            else:
                drop_time[ind] = pos
        if len(possible_indices) == 0:
            break
        indices = possible_indices
    if should_verify:
        order = reversed(np.argsort(drop_time))
        num = 1
        for index in order:
            if num >= max_top_check:
                break
            result = run_verifier(path_to_task, should_replace=True, line_number=index + 1)
            if result == "OK":
                return index, len(indices)
            num += 1

    return indices[0], len(indices)


