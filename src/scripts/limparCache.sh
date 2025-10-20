echo "Limpando cache do APT..."
sudo apt clean

echo "Limpando cache do Snap..."
sudo snap set system refresh.retain=2   # mantém só as últimas 2 versões
sudo snap remove $(snap list --all | awk '/disabled/{print $1, $2}') 2>/dev/null

echo "Limpando cache do Flatpak..."
flatpak uninstall --unused -y
