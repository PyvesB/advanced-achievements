package com.hm.achievement.config;

import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainConfigController {
    private final CommentedYamlConfiguration mainConfig;

    public MainConfigController(CommentedYamlConfiguration mainConfig) {
        this.mainConfig = mainConfig;
    }

    /**
     * Extracts the achievement message/goals
     *
     * @param category
     * @param path
     * @param completed
     * @return the description to display in the GUI
     */
    public List<String> getDescriptionsToDisplay(String category, String path, boolean completed) {
        String goal = mainConfig.getString(category + '.' + path + ".Goal", "");
        if (StringUtils.isNotBlank(goal) && !completed) {
            return Arrays.asList(StringUtils.splitByWholeSeparator(goal, "\\n"));
        }
        return Collections.singletonList(mainConfig.getString(category + '.' + path + ".Message", ""));
    }
}
