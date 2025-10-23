package br.com.overclean;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class ProcessReader {
    public JsonNode getJsonProcesses() {
        try {
            // 1️⃣ Executa o programa C
            ProcessBuilder pb = new ProcessBuilder("./bin/listarProcessos");
            pb.directory(new File(System.getProperty("user.dir"))); // garante execução na raiz
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 2️⃣ Captura a saída JSON
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Programa C retornou código " + exitCode);
                return null;
            }

            // 3️⃣ Faz o parse do JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(output.toString());

            return root;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
