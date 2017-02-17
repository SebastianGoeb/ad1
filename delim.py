#!/usr/bin/python3

import sys

taken_chars = set()

for line in sys.stdin:
    for char in line:
        taken_chars.add(char)

for i in range(128):
    if not chr(i) in taken_chars:
        print(repr(chr(i))[1:-1], '\t', i)
