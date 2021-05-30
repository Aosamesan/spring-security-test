package asn.aosamesan.securitytest.handler;

import asn.aosamesan.securitytest.model.api.PageableResult;
import asn.aosamesan.securitytest.model.api.PagingParameter;
import asn.aosamesan.securitytest.model.dto.User;
import asn.aosamesan.securitytest.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class ViewHandler {
    private final UserRepository userRepository;

    public ViewHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<ServerResponse> index(ServerRequest request) {
        return renderWithRedirect("index", request, URI.create("/login"));
    }

    public Mono<ServerResponse> singIn(ServerRequest request) {
        return render("signin", request);
    }

    public Mono<ServerResponse> signUp(ServerRequest request) {
        return render("signup", request);
    }

    public Mono<ServerResponse> admin(ServerRequest request) {
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return userRepository.findAll(Sort.by("username").ascending())
                .skip(pagingParam.getStart())
                .take(pagingParam.getDisplay())
                .collectList()
                .zipWith(userRepository.count())
                .map(PageableResult.fromTuple(pagingParam))
                .flatMap(data -> render("admin", request, Collections.singletonMap("DATA", data)))
                ;
    }

    public Mono<ServerResponse> user(ServerRequest request) {
        return render("user", request, Collections.emptyMap());
    }

    public Mono<ServerResponse> board(ServerRequest request) {
        return render("board", request, Collections.emptyMap());
    }

    private Mono<ServerResponse> render(String viewName, ServerRequest request) {
        return render(viewName, request, Collections.emptyMap());
    }

    private Mono<ServerResponse> render(String viewName, ServerRequest request, Map<String, ?> model) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .map(user -> Collections.singletonMap("CURRENT_USER", user))
                .switchIfEmpty(Mono.just(Collections.emptyMap()))
                .flatMap(
                        userMap -> ServerResponse.ok().render(viewName, new HashMap<>() {{
                                    putAll(model);
                                    putAll(userMap);
                                    put("VIEW_PATH", request.path());
                                }})
                );
    }

    private Mono<ServerResponse> renderWithRedirect(String viewName, ServerRequest request, URI redirectWhenNotAuthenticated, Map<String, ?> model) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .map(user -> Collections.singletonMap("CURRENT_USER", user))
                .flatMap(
                        userMap -> ServerResponse.ok().render(viewName, new HashMap<>() {{
                            putAll(model);
                            putAll(userMap);
                            put("VIEW_PATH", request.path());
                        }})
                )
                .switchIfEmpty(ServerResponse.permanentRedirect(redirectWhenNotAuthenticated).build())
                ;
    }

    private Mono<ServerResponse> renderWithRedirect(String viewName, ServerRequest request, URI redirectWhenNotAuthenticated) {
        return renderWithRedirect(viewName, request, redirectWhenNotAuthenticated, Collections.emptyMap());
    }
}
