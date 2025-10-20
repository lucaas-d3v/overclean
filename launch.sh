echo "Compilando e rodando..."

# compilar
javac --module-path /home/lucas2078/javafx-sdk-21/lib --add-modules javafx.controls -d out **/*.java

# rodar
java --module-path /home/lucas2078/javafx-sdk-21/lib --add-modules javafx.controls -cp out Main