#!/bin/bash

echo "Atualizando repositórios..."
sudo apt update

echo "Atualizando pacotes instalados..."
sudo apt upgrade -y

echo "Removendo pacotes desnecessários..."
sudo apt autoremove -y
sudo apt autoclean -y