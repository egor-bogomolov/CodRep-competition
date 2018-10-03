#!/bin/bash

python3 Solutions/solution.py Datasets/Dataset1 > out1
python3 Baseline/evaluate.py -d Datasets/Dataset1 < out1

python3 Solutions/solution.py Datasets/Dataset2 > out2
python3 Baseline/evaluate.py -d  Datasets/Dataset2 < out2

