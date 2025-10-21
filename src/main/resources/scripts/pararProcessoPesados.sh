#!/bin/bash

LIMITE_MB=100

# Cria uma lista de PIDs de processos acima do limite
pids=$(ps --no-headers -eo pid,comm,rss --sort=-rss | awk -v limite=$LIMITE_MB '{
    rss_mb=$3/1024;
    if(rss_mb>limite) print $1
}')

# Mostra os PIDs e processos para conferência
echo "Processos acima de $LIMITE_MB MB de RAM:"
ps --no-headers -eo pid,comm,rss --sort=-rss | awk -v limite=$LIMITE_MB '{
    rss_mb=$3/1024;
    if(rss_mb>limite) printf "PID: %d | Processo: %s | RAM: %.2f MB\n",$1,$2,rss_mb
}'

# Loop seguro para matar cada PID
for pid in $pids; do
    # Pega o nome do processo
    proc_name=$(ps -p $pid -o comm=)
    
    # Ignora processos críticos do sistema
    if [[ "$proc_name" =~ ^(systemd|Xorg|cinnamon|mate-session|bash|gnome-shell)$ ]]; then
        echo "Ignorando processo crítico: $proc_name (PID $pid)"
        continue
    fi

    # Pergunta antes de matar
    read -p "Deseja encerrar $proc_name (PID $pid)? [y/N]: " resp
    if [[ "$resp" =~ ^[Yy]$ ]]; then
        kill -15 $pid
        sleep 1
        # Se ainda estiver rodando, força kill
        if ps -p $pid > /dev/null; then
            kill -9 $pid
        fi
        echo "Processo $proc_name encerrado."
    fi
done
