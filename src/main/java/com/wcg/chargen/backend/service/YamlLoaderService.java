package com.wcg.chargen.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface YamlLoaderService<T> {
    Logger logger = LoggerFactory.getLogger(YamlLoaderService.class);

     String YAML_PATH = "/yaml/";

    default public T loadFromYaml() {
        var objectMapper = new ObjectMapper(new YAMLFactory());
        var yamlFilePath = getYamlPath() + getYamlFile();
        try {
            return objectMapper.readValue(
                    getClass().getResourceAsStream(yamlFilePath),
                    getObjClass());
        }
        catch(Exception e) {
            logger.error("Unable to load YAML file {} as class {}",
                    yamlFilePath,
                    getObjClass(),
                    e);
            return null;
        }
    }

    default public String getYamlPath() {
        return YAML_PATH;
    }

    public String getYamlFile();

    public Class<T> getObjClass();
}