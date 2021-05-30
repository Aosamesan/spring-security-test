package asn.aosamesan.securitytest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class ApplicationConfiguration implements WebFluxConfigurer {
    @Override
    public void configureViewResolvers(ViewResolverRegistry resolverRegistry) {
        resolverRegistry.freeMarker();
    }
}
