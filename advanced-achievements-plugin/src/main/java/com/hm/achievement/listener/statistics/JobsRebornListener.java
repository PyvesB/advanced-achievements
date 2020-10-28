package com.hm.achievement.listener.statistics;
package com.gamingmesh.jobs.api;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.gamingmesh.jobs.api.JobsLevelUpEvent;

/**
 * Listener class to deal with Jobs Reborn achievements.
 */
@Singleton
public class JobsRebornListener extends AbstractListener {

    @Inject
    public JobsRebornListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
                         Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
        super(MultipleAchievements.JOBSREBORN, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJob(JobsLevelUpEvent event) {
        if(event.getPlayer() == null)
            return;

        // Grab the player from the JobsPlayer
        Player player = event.getPlayer().getPlayer();

        if(player == null)
            return;

        MultipleAchievements category = MultipleAchievements.JOBSREBORN;

        if (!shouldIncreaseBeTakenIntoAccount(player, category)) {
            return;
        }

        String jobName = event.getJobName().toLowerCase();
        Set<String> foundAchievements = findAchievementsByCategoryAndName(category, jobName);

        // Check if the jobName exists in the Jobs Reborn Category.
        // This check replaced the previous lookup check.
        if (player.hasPermission(category.toPermName() + '.' + jobName)) {
            updateStatisticAndAwardAchievementsIfAvailable(player, category, foundAchievements, 1);
        }
    }
}
