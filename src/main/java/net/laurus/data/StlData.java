package net.laurus.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import eu.mihosoft.jcsg.CSG;
import lombok.Builder;
import lombok.Value;

/**
 * Represents STL data generated from a CSG model. Provides convenient methods
 * for creation and safe file output.
 */
@Value
@Builder
public class StlData {

    String value;

    /**
     * Creates an {@link StlData} instance from a CSG model.
     *
     * @param model the model to convert to STL
     * @return a new {@link StlData} instance containing the STL string
     */
    public static StlData from(CSG model) {
        return StlData.builder().value(model.toStlString()).build();
    }

    /**
     * Safely writes this STL data to a file, creating directories as needed.
     *
     * @param fileName the target file path (e.g., "output/model.stl")
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean writeToFile(String fileName) {

        try {
            Path path = Path.of(fileName);
            Files.createDirectories(path.getParent()); // ensure parent dir exists

            // use try-with-resources for safety
            Files.writeString(path, value, StandardCharsets.UTF_8);

            return true;
        }
        catch (
                IOException
                | NullPointerException e) {
            System.err.println("[StlData] Failed to write STL file: " + e.getMessage());
            return false;
        }

    }

}
