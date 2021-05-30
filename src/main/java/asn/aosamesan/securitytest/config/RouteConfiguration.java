package asn.aosamesan.securitytest.config;

import asn.aosamesan.securitytest.handler.UserHandler;
import asn.aosamesan.securitytest.handler.ViewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouteConfiguration {
    private final UserHandler userHandler;
    private final ViewHandler viewHandler;

    public RouteConfiguration(UserHandler userHandler, ViewHandler viewHandler) {
        this.userHandler = userHandler;
        this.viewHandler = viewHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .resources("/static/**", new ClassPathResource("static/"))
                .andNest(
                        RequestPredicates.path("/api"),
                        RouterFunctions.nest(
                                RequestPredicates.path("/users"),
                                RouterFunctions.route(
                                        RequestPredicates.GET(""),
                                        userHandler::retrieveCurrentUser
                                ).andRoute(
                                        RequestPredicates.PUT(""),
                                        userHandler::updateCurrentUser
                                ).andRoute(
                                        RequestPredicates.DELETE(""),
                                        userHandler::deleteCurrentUser
                                ).andRoute(
                                        RequestPredicates.POST(""),
                                        userHandler::create
                                )
                        ).andNest(
                                RequestPredicates.path("/admin"),
                                RouterFunctions.nest(
                                        RequestPredicates.path("/users"),
                                        RouterFunctions.route(
                                                RequestPredicates.GET(""),
                                                userHandler::retrieveAll
                                        ).andRoute(
                                                RequestPredicates.PUT("/{username}"),
                                                userHandler::update
                                        ).andRoute(
                                                RequestPredicates.DELETE("/{username}"),
                                                userHandler::delete
                                        )
                                )
                        )
                ).andNest(
                        RequestPredicates.path("/"),
                        RouterFunctions.route(
                                RequestPredicates.GET(""),
                                viewHandler::index
                        ).andRoute(
                                RequestPredicates.GET("/login"),
                                viewHandler::singIn
                        ).andRoute(
                                RequestPredicates.GET("/signup"),
                                viewHandler::signUp
                        ).andRoute(
                                RequestPredicates.GET("/admin"),
                                viewHandler::admin
                        ).andRoute(
                                RequestPredicates.GET("/user"),
                                viewHandler::user
                        ).andRoute(
                                RequestPredicates.GET("/board"),
                                viewHandler::board
                        )
                );
    }
}
