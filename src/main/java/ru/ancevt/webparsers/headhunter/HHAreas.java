package ru.ancevt.webparsers.headhunter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ancevt
 */
public class HHAreas extends HashMap<String, Integer> {
    private static final String AREAS_DB_FILE = "areas.db";

    private static HHAreas instance;
    
    public static final HHAreas getInstance() throws IOException {
        return instance == null ? instance = new HHAreas(
                HHAreas.class.getClassLoader().getResourceAsStream(AREAS_DB_FILE)
        ) : instance;
    } 
    
    private HHAreas(final InputStream inputStream) throws FileNotFoundException, IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String[] splitted = line.split("=");
                put(splitted[0], Integer.valueOf(splitted[1]));
            }
        }
    }
    
    private HHAreas(final File file) throws FileNotFoundException, IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String[] splitted = line.split("=");
                put(splitted[0], Integer.valueOf(splitted[1]));
            }
        }
    }

    public Integer[] getIdsByWord(String word) {
        final List<Integer> list = new ArrayList<>();

        this.entrySet().stream().forEach((entry) -> {
            final String key = entry.getKey();

            if (word.contains(" ")) {
                if (key.contains(word)) {
                    list.add(entry.getValue());
                }
            } else {

                final String[] splitted = key.split("\\s+");

                for (final String part : splitted) {
                    if (part.equalsIgnoreCase(word)) {
                        list.add(entry.getValue());
                    }
                }
            }
        });

        return list.toArray(new Integer[]{});
    }

    public int getIdByWord(String word) {
        for (Map.Entry<String, Integer> entry : this.entrySet()) {
            final String key = entry.getKey();

            if (word.contains(" ")) {
                if (key.contains(word)) {
                    return entry.getValue();
                }
            } else {
                final String[] splitted = key.split("\\s+");

                for (final String part : splitted) {
                    if (part.equalsIgnoreCase(word)) {
                        return entry.getValue();
                    }
                }
            }
        }

        return 0;
    }

    public String getNameById(int id) {
        for (Map.Entry<String, Integer> entry : this.entrySet()) {
            if (entry.getValue() == id) {
                return entry.getKey();
            }
        }

        return null;
    }
}
