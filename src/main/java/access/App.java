package access;

import conf.CmbConfiguration;
import conf.ConfigurationHealthCheck;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import services.CmbService;


public class App extends Application<CmbConfiguration>
{
    public static void main( String[] args ) throws Exception
    {
        if (args == null || args.length == 0)
            args = "server cmb.yaml".split("\\s+");
        new App().run(args);
    }

    @Override
    public String getName()
    {
        return "cmb server";
    }

    public void run(CmbConfiguration configuration,
                    Environment environment) throws Exception
    {
        CmbService service = new CmbService(configuration.getCmb());
        environment.healthChecks().register("Configuration Check",
                new ConfigurationHealthCheck(configuration));
        environment.jersey().register(service);
    }
}
