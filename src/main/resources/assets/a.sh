#!/bin/bash
# gerar_icons.sh
# Uso: ./gerar_icons.sh logo.png

if [ "$#" -ne 1 ]; then
    echo "Uso: $0 <caminho_para_logo.png>"
    exit 1
fi

INPUT="$1"
OUTPUT_DIR="icons"
mkdir -p "$OUTPUT_DIR"

# Lista de tamanhos
SIZES=(8 16 32 64 128 256 512 1024)

for SIZE in "${SIZES[@]}"; do
    convert "$INPUT" -resize "${SIZE}x${SIZE}" "$OUTPUT_DIR/logo_${SIZE}.png"
    echo "Gerado: $OUTPUT_DIR/logo_${SIZE}.png"
done

echo "Todos os ícones foram gerados em $OUTPUT_DIR"
