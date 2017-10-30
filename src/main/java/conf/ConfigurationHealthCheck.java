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
        return Result.healthy();
    }
}
