package com.ares.ares_server.AI;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AIMissionGenerator {

    public static Object runPythonMissionGenerator() throws Exception {
        Dotenv dotenv = Dotenv.load();
        String mySecretKey = dotenv.get("API_KEY");

        if (mySecretKey == null) {
            throw new RuntimeException("API_KEY missing in Java .env file");
        }

        URL url = AIMissionGenerator.class.getResource("GetMissionsAndBonuses.py");

        File scriptFile;
        if (url != null) {
            scriptFile = new File(url.toURI());
        } else {

            String relativePath = "src/main/java/com/ares/ares_server/AI/GetMissionsAndBonuses.py";
            scriptFile = new File(System.getProperty("user.dir"), relativePath);
        }

        if (!scriptFile.exists()) {
            throw new RuntimeException("Cannot find Python script at: " + scriptFile.getAbsolutePath());
        }

        ProcessBuilder pb = new ProcessBuilder("python", scriptFile.getAbsolutePath());

        pb.environment().put("API_KEY_FROM_JAVA", mySecretKey);

        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String errorLine;
        while ((errorLine = stdError.readLine()) != null) {
            System.err.println("PYTHON ERROR: " + errorLine);
        }

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

        output =   output.replaceAll("\\}\\s*\\{", "},{") ;

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readTree(output);
    }
}
