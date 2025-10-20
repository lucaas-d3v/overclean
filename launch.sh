#!/bin/bash

echo "Compilando e rodando..."

# Define o diretório do projeto
PROJECT_DIR="/home/lucas2078/Gerenciador-de-Performance"
JAVAFX_PATH="/home/lucas2078/javafx-sdk-21/lib"

# Vai para o diretório do projeto
cd "$PROJECT_DIR"

# Compilar
javac --module-path "$JAVAFX_PATH" \
      --add-modules javafx.controls \
      -d out \
      src/Main.java

# Verifica se compilou
if [ $? -ne 0 ]; then
    echo "❌ Erro na compilação!"
    exit 1
fi

# Rodar
java --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls \
     -cp out \
     Main