package asn.aosamesan.securitytest.config;

import asn.aosamesan.securitytest.handler.DocumentApiHandler;
import asn.aosamesan.securitytest.handler.UserHandler;
import asn.aosamesan.securitytest.handler.ViewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

@Configuration
public class RouteConfiguration {
    private final UserHandler userHandler;
    private final ViewHandler viewHandler;
    private final DocumentApiHandler documentApiHandler;

    public RouteConfiguration(UserHandler userHandler, ViewHandler viewHandler, DocumentApiHandler documentApiHandler) {
        this.userHandler = userHandler;
        this.viewHandler = viewHandler;
        this.documentApiHandler = documentApiHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .resources("/static/**", new ClassPathResource("static/"))
                .andRoute(
                        RequestPredicates.GET("/favicon.ico"),
                        req -> ServerResponse.permanentRedirect(URI.create("/static/favicon.ico")).build()
                )
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
                        ).andNest(
                                RequestPredicates.path("/documents"),
                                RouterFunctions.route(
                                        RequestPredicates.GET(""), // READ
                                        documentApiHandler::retrieveAll
                                ).andRoute(
                                        RequestPredicates.POST(""), // WRITE
                                        documentApiHandler::create
                                ).andRoute(
                                        RequestPredicates.GET("/my"), // authenticated
                                        documentApiHandler::retrieveMyDocuments
                                ).andRoute(
                                        RequestPredicates.GET("/users/{username}"), // READ
                                        documentApiHandler::retrieveAllByUsername
                                ).andRoute(
                                        RequestPredicates.PUT("/freeze/{id:[0-9]+}"), // WRITE_INFO
                                        documentApiHandler::freeze
                                ).andRoute(
                                        RequestPredicates.GET("/{id:[0-9]+}"), // READ
                                        documentApiHandler::retrieveOne
                                ).andRoute(
                                        RequestPredicates.PUT("/{id:[0-9]+}"), // WRITE
                                        documentApiHandler::update
                                ).andRoute(
                                        RequestPredicates.DELETE("/{id:[0-9]+}"), // WRITE
                                        documentApiHandler::delete
                                ).andRoute(
                                        RequestPredicates.POST("/{id:[0-9]+}/replies"), // WRITE reply
                                        documentApiHandler::createReply
                                )
                        ).andNest(
                                RequestPredicates.path("/replies"),
                                RouterFunctions.route(
                                        RequestPredicates.GET(""),
                                        documentApiHandler::retrieveMyReplies
                                ).andRoute(
                                        RequestPredicates.PUT("/{id}"),
                                        documentApiHandler::updateReply
                                ).andRoute(
                                        RequestPredicates.DELETE("/{id}"),
                                        documentApiHandler::deleteReply
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
                        ).andRoute(
                                RequestPredicates.GET("/board/{id:[0-9]+}"),
                                viewHandler::boardDetail
                        ).andRoute(
                                RequestPredicates.GET("/board/new"),
                                viewHandler::boardNew
                        ).andRoute(
                                RequestPredicates.GET("/board/{id:[0-9]+}/edit"),
                                viewHandler::boardEdit
                        ).andRoute(
                                RequestPredicates.GET("/board/my"),
                                viewHandler::boardMine
                        ).andRoute(
                                RequestPredicates.GET("/replies/my"),
                                viewHandler::repliesMine
                        )
                );
    }
}
