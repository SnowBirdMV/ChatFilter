package com.mcjty.tut1basics.client.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private List<FilterRule> filterRules;
    private final String configDir = "./config/ChatFilter";
    private final String chatFilterFileName = "ChatFilters.json";
    private final Logger logger = LogManager.getLogger(Config.class);
    private boolean modEnabled = true;

    public boolean isModEnabled() {
        return modEnabled;
    }

    public void setModEnabled(boolean newModEnabled) {modEnabled = newModEnabled;}

    public Config() {
        readConfig();
    }


    public void readConfig() {
        try {
            Files.createDirectories(Paths.get(configDir));
            File filterDataFile = new File(Paths.get(configDir, chatFilterFileName).toString());
            // create file if none is there
            if (filterDataFile.createNewFile()){
                FileUtils.writeStringToFile(filterDataFile, "{[]}", StandardCharsets.UTF_8);

            }
            Gson gson = new Gson();
            FilterRuleContainer container = gson.fromJson(FileUtils.readFileToString(filterDataFile, StandardCharsets.UTF_8), FilterRuleContainer.class);
            filterRules = container.getFilterRules();
        } catch (IOException e) {
            logger.error("An error occurred while reading the config.", e);
        }
    }

    public void writeConfig(List<FilterRule> filters) {
        try {
            FilterRuleContainer container = new FilterRuleContainer(filters);
            File file = new File(Paths.get(configDir, chatFilterFileName).toString());
            Gson gson = new Gson();
            String json = gson.toJson(container);
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
            filterRules = filters;
        } catch (IOException e) {
            logger.error("An error occurred while writing the config.", e);
        }
    }

    public List<FilterRule> getFilterRules() {
        System.out.println("MADE IT HERE");
        if (filterRules == null) {
            readConfig();
        }
        return filterRules;
    }

    private class FilterRuleContainer {
        private List<FilterRule> filterRules;
        public FilterRuleContainer(List<FilterRule> filterRules) {
            this.filterRules = filterRules;
        }

        public List<FilterRule> getFilterRules() {
            return this.filterRules;
        }

        public void setFilterRules(List<FilterRule> newFilterRules) {
            this.filterRules = newFilterRules;
        }
    }
}