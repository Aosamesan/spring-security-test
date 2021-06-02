package asn.aosamesan.securitytest.handler;

import asn.aosamesan.securitytest.model.api.PageableResult;
import asn.aosamesan.securitytest.model.api.PagingParameter;
import asn.aosamesan.securitytest.model.dto.User;
import asn.aosamesan.securitytest.repository.UserRepository;
import asn.aosamesan.securitytest.utils.BoardDocumentHandlerUtils;
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
    private final BoardDocumentHandlerUtils boardDocumentHandlerUtils;

    public ViewHandler(UserRepository userRepository, BoardDocumentHandlerUtils boardDocumentHandlerUtils) {
        this.userRepository = userRepository;
        this.boardDocumentHandlerUtils = boardDocumentHandlerUtils;
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
        return userRepository.findAll(Sort.by("createdAt").descending())
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
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return boardDocumentHandlerUtils
                .findAll(pagingParam)
                .zipWith(ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication).map(Authentication::getDetails).cast(User.class))
                .map(tuple -> new HashMap<String, Object>() {{
                    put("DATA", tuple.getT1());
                    put("CURRENT_USER", tuple.getT2());
                }})
                .flatMap(model -> render("board", request, model));
    }

    public Mono<ServerResponse> boardDetail(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return boardDocumentHandlerUtils
                .findOne(id)
                .flatMap(document -> render("document", request, Collections.singletonMap("DOCUMENT", document)));
    }

    public Mono<ServerResponse> boardNew(ServerRequest request) {
        return render("new", request);
    }

    public Mono<ServerResponse> boardEdit(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return boardDocumentHandlerUtils
                .checkCurrentUserWriteDocument(id)
                .flatMap(document -> render("edit", request, Collections.singletonMap("DOCUMENT", document)));
    }

    public Mono<ServerResponse> boardMine(ServerRequest request) {
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return boardDocumentHandlerUtils
                .findOwnDocuments(pagingParam)
                .flatMap(data -> render("mydocument", request, Collections.singletonMap("DATA", data)));
    }

    public Mono<ServerResponse> repliesMine(ServerRequest request) {
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return boardDocumentHandlerUtils
                .findOwnReplies(pagingParam)
                .flatMap(data -> render("myreplies", request, Collections.singletonMap("DATA", data)));
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
