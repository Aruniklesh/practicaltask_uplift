package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.CalculatorTestCase;
import models.Member;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * JsonUtils provides helper methods to read and deserialize JSON files
 * using the Jackson Databind library.
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reads a list of Member records from a JSON file path.
     *
     * @param filePath Relative or absolute path to the members JSON file
     * @return List of Member objects
     */
    public static List<Member> readMembersFromJson(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return objectMapper.readValue(file, new TypeReference<List<Member>>() {});
            } else {
                // Fallback to classpath if not on filesystem directly
                try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(filePath)) {
                    if (is != null) {
                        return objectMapper.readValue(is, new TypeReference<List<Member>>() {});
                    }
                }
            }
            throw new RuntimeException("Could not find JSON file at: " + filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading members JSON from " + filePath + ": " + e.getMessage(), e);
        }
    }

    /**
     * Reads a list of CalculatorTestCase records from a JSON file.
     *
     * @param resourceOrFilePath Path or resource path to calculator test data
     * @return List of CalculatorTestCase objects
     */
    public static List<CalculatorTestCase> readCalculatorTestCases(String resourceOrFilePath) {
        try {
            File file = new File(resourceOrFilePath);
            if (file.exists()) {
                return objectMapper.readValue(file, new TypeReference<List<CalculatorTestCase>>() {});
            } else {
                try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(resourceOrFilePath)) {
                    if (is != null) {
                        return objectMapper.readValue(is, new TypeReference<List<CalculatorTestCase>>() {});
                    }
                }
            }
            throw new RuntimeException("Could not find calculator test cases at: " + resourceOrFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading calculator test cases: " + e.getMessage(), e);
        }
    }
}
