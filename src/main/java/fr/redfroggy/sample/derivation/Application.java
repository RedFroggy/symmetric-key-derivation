package fr.redfroggy.sample.derivation;

import fr.redfroggy.sample.derivation.configuration.Settings;
import fr.redfroggy.sample.derivation.exception.DiversificationException;
import fr.redfroggy.sample.derivation.services.DiversificationStandard;
import fr.redfroggy.sample.derivation.utils.BytesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

/**
 * Application
 */
@SpringBootApplication
@EnableAutoConfiguration
@Slf4j
public class Application {

    @Autowired
    protected Settings settings;

    @Autowired
    protected DiversificationStandard div;

    @PostConstruct
    protected void run() {
        try {
            byte[] divK = div.diversify(
                    BytesUtils.hexToBytes(settings.getKey()),
                    settings.getKeyIndex(),
                    BytesUtils.hexToBytes(settings.getUid()),
                    settings.getAid() != null ? BytesUtils.hexToBytes(settings.getAid()) : null,
                    settings.getSystemIdentifier() != null ? BytesUtils.hexToBytes(settings.getSystemIdentifier()) : null);

            System.out.println("Diversified key: " + BytesUtils.bytesToHex(divK));
        } catch (DiversificationException e) {
            System.out.println("Diversification error : " + e.getMessage());
            log.error("Diversification error", e);
        }
    }

    /**
     * Main client method
     *
     * @param args Command args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Application.log.info("Application is running");
        SpringApplication app = new SpringApplication(Application.class);
        app.setWebEnvironment(false);
        ConfigurableApplicationContext ctx = app.run(args);
        SpringApplication.exit(ctx);
        Application.log.info("Application stopped");
    }

}
