package conf;

import com.codahale.metrics.health.HealthCheck;

/**
 * Created by zhou on 16-7-28.
 */
public class ConfigurationHealthCheck extends HealthCheck {
    CmbConfiguration configuration;

    public ConfigurationHealthCheck(CmbConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected Result check() throws Exception {
//        if (segModlePath != null && segModlePath.startsWith("/model")) {
//            return Result.healthy();
//        }
//
//        return Result.unhealthy("Segment Model Path Error!");
        return Result.healthy();
    }
}
