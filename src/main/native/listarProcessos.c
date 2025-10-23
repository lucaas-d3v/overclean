#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <errno.h>

#define THRESHOLD_MB 1UL

/* Converte páginas para bytes usando sysconf(_SC_PAGESIZE) */
static unsigned long pages_to_bytes(unsigned long pages) {
    long page_size = sysconf(_SC_PAGESIZE);
    if (page_size <= 0) page_size = 4096; /* fallback */
    return pages * (unsigned long)page_size;
}

/* Lê o nome do processo em /proc/<pid>/comm — retorna string malloc'd ou NULL */
static char *read_comm(pid_t pid) {
    char path[64];
    snprintf(path, sizeof(path), "/proc/%d/comm", pid);
    FILE *f = fopen(path, "r");
    if (!f) return NULL;
    char buf[256];
    if (!fgets(buf, sizeof(buf), f)) {
        fclose(f);
        return NULL;
    }
    fclose(f);
    /* remove newline final */
    size_t len = strlen(buf);
    if (len && buf[len - 1] == '\n') buf[len - 1] = '\0';
    return strdup(buf);
}

/* Lê /proc/<pid>/statm e devolve resident pages (0 se erro) */
static unsigned long read_statm_resident(pid_t pid) {
    char path[64];
    snprintf(path, sizeof(path), "/proc/%d/statm", pid);
    FILE *f = fopen(path, "r");
    if (!f) return 0;
    unsigned long size_pages = 0, resident_pages = 0;
    /* statm: size resident shared text lib data dt */
    if (fscanf(f, "%lu %lu", &size_pages, &resident_pages) != 2) {
        fclose(f);
        return 0;
    }
    fclose(f);
    return resident_pages;
}

/* Verifica se a string é composta só por dígitos (PID) */
static int is_numeric(const char *s) {
    if (!s || !*s) return 0;
    for (; *s; ++s)
        if (!isdigit((unsigned char)*s)) return 0;
    return 1;
}

/* Escapa caracteres especiais JSON e imprime direto no stdout */
static void escape_json_and_print(const char *s) {
    for (; *s; ++s) {
        unsigned char c = (unsigned char)*s;
        switch (c) {
            case '\"': fputs("\\\"", stdout); break;
            case '\\': fputs("\\\\", stdout); break;
            case '\b': fputs("\\b", stdout);  break;
            case '\f': fputs("\\f", stdout);  break;
            case '\n': fputs("\\n", stdout);  break;
            case '\r': fputs("\\r", stdout);  break;
            case '\t': fputs("\\t", stdout);  break;
            default:
                if (c < 0x20)
                    fprintf(stdout, "\\u%04X", c);
                else
                    putchar(c);
                break;
        }
    }
}

int main(void) {
    DIR *proc = opendir("/proc");
    if (!proc) {
        perror("opendir(/proc)");
        return 1;
    }

    struct dirent *ent;
    unsigned long threshold_bytes = THRESHOLD_MB * 1024UL * 1024UL;
    int first = 1;

    printf("{\"processes\":[");

    while ((ent = readdir(proc)) != NULL) {
        if (!is_numeric(ent->d_name)) continue;

        pid_t pid = (pid_t)atoi(ent->d_name);
        unsigned long resident_pages = read_statm_resident(pid);
        if (resident_pages == 0) continue; /* erro ou processo terminou / sem permissão */

        unsigned long rss_bytes = pages_to_bytes(resident_pages);
        if (rss_bytes <= threshold_bytes) continue;

        char *comm = read_comm(pid);
        double rss_mb = (double)rss_bytes / (1024.0 * 1024.0);
        unsigned long rss_kb = rss_bytes / 1024UL;

        if (!first) printf(",");
        first = 0;

        printf("{\"pid\":%d,", pid);
        printf("\"rss_mb\":%.2f,", rss_mb);
        printf("\"rss_kb\":%lu,", rss_kb);
        printf("\"name\":\"");
        if (comm) {
            escape_json_and_print(comm);
            free(comm);
        } else {
            fputs("-", stdout);
        }
        printf("\"}");

        fflush(stdout); /* útil para streaming contínuo */
    }

    printf("]}\n");

    closedir(proc);
    return 0;
}
