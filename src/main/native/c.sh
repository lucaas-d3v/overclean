#!/bin/bash
# c.sh

output="${1%.c}"

if [ "$2" = "-c" ] && [ -f "$output" ]; then
    rm "$output"
fi

gcc -O2 "$1" -o "$output"

./"$output"
