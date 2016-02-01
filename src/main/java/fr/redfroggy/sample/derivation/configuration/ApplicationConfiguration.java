package fr.redfroggy.sample.derivation.configuration;

import fr.redfroggy.sample.derivation.exception.DiversificationException;
import fr.redfroggy.sample.derivation.services.DiversificationStandard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration
 */
@Configuration
public class ApplicationConfiguration {

    @Autowired
    protected Settings settings;

    /**
     * Create diversification service
     *
     * @return Diversification service
     * @throws DiversificationException If diversification service cannot be construct
     */
    @Bean
    public DiversificationStandard getDiversificationStandard() throws DiversificationException {
        return new DiversificationStandard(DiversificationStandard.Standard.valueOf(settings.getStandard()));
    }
}
