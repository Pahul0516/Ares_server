package com.ares.ares_server.AI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AIMissionBonusGenerator {

    public static void main(String[] args) {
        try {
            List<Object> missions = runPythonMissionGenerator();

            System.out.println("=== Mission Received in Java ===");
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(missions));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Object> runPythonMissionGenerator() throws Exception {
        List<Object> missions = new ArrayList<>();

        // Adjust path to your Python script
        String pythonScript = "C:\\Users\\Omen\\IdeaProjects\\Ares_server\\src\\main\\java\\com\\ares\\ares_server\\AI\\GetMissionsAndBonuses.py";

        // Build command
        ProcessBuilder pb = new ProcessBuilder("python", pythonScript);
        pb.redirectErrorStream(true); // merge stdout and stderr

        Process process = pb.start();

        // Read Python stdout
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Python script exited with code " + exitCode);
        }

        String output = sb.toString().trim();

        // Your Python prints multiple JSON objects (one per mission). Wrap in [] to parse as list
        output = "[" + output.replaceAll("\\}\\s*\\{", "},{") + "]";

        ObjectMapper mapper = new ObjectMapper();
        missions = mapper.readValue(output, List.class);

        return missions;
    }
}
