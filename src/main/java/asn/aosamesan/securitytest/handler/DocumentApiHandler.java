package asn.aosamesan.securitytest.handler;

import asn.aosamesan.securitytest.model.api.PagingParameter;
import asn.aosamesan.securitytest.repository.BoardDocumentRepository;
import asn.aosamesan.securitytest.utils.BoardDocumentHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
public class DocumentApiHandler {
    private final BoardDocumentRepository boardDocumentRepository;
    private final BoardDocumentHandlerUtils boardDocumentHandlerUtils;

    public DocumentApiHandler(BoardDocumentRepository boardDocumentRepository, BoardDocumentHandlerUtils boardDocumentHandlerUtils) {
        this.boardDocumentRepository = boardDocumentRepository;
        this.boardDocumentHandlerUtils = boardDocumentHandlerUtils;
    }

    // [POST] Create
    public Mono<ServerResponse> create(ServerRequest request) {
        return boardDocumentHandlerUtils
                .createOne(request.formData())
                .flatMap(boardDocumentRepository::save)
                .flatMap(boardDocument -> ServerResponse
                        .created(URI.create("/api/board/" + boardDocument.getDisplayId()))
                        .body(BodyInserters.fromValue(boardDocument)))
                ;
    }

    // [GET} retrieve all
    public Mono<ServerResponse> retrieveAll(ServerRequest request) {
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return boardDocumentHandlerUtils
                .findAll(pagingParam)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [GET] retrieve current user
    public Mono<ServerResponse> retrieveMyDocuments(ServerRequest request) {
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return boardDocumentHandlerUtils
                .findOwnDocuments(pagingParam)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [GET] retrieve by username
    public Mono<ServerResponse> retrieveAllByUsername(ServerRequest request) {
        var username = request.pathVariable("username");
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return boardDocumentHandlerUtils
                .findDocumentsByUsername(pagingParam, username)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [GET] retrieve one
    public Mono<ServerResponse> retrieveOne(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return boardDocumentHandlerUtils
                .findOne(id)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                .switchIfEmpty(ServerResponse.notFound().build())
                ;
    }

    // [PUT] update one
    public Mono<ServerResponse> update(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return boardDocumentHandlerUtils
                .checkCurrentUserWriteDocument(id)
                .zipWith(request.formData())
                .flatMap(
                        tuple -> {
                            var document = tuple.getT1();
                            var formData = tuple.getT2();
                            var title = formData.getFirst("title");
                            var content = formData.getFirst("content");
                            var isNotice = formData.containsKey("isNotice") && Boolean.parseBoolean(formData.getFirst("isNotice"));
                            var updated = false;
                            var now = DateTime.now().toDate();

                            if (!document.getTitle().equals(title)) {
                                document.setTitle(title);
                                updated = true;
                            }

                            if (!document.getContent().equals(content)) {
                                document.setContent(content);
                                updated = true;
                            }

                            if (document.isNotice() != isNotice) {
                                document.setNotice(isNotice);
                                updated = true;
                            }

                            if (updated) {
                                document.setUpdatedAt(now);
                                return boardDocumentRepository.save(document);
                            }

                            return Mono.just(document);
                        }
                )
                .flatMap(boardDocumentRepository::save)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.created(URI.create("/api/board/" + id))::body)
                .switchIfEmpty(ServerResponse.notFound().build())
                ;
    }

    // [PUT] freeze one
    public Mono<ServerResponse> freeze(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return boardDocumentRepository.findBoardDocumentByDisplayId(id)
                .map(
                        boardDocument -> {
                            boardDocument.setFrozen(!boardDocument.isFrozen());
                            log.info("Freeze : {}", boardDocument);
                            return boardDocument;
                        }
                )
                .flatMap(boardDocumentRepository::save)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.created(URI.create("/api/board/" + id))::body)
                .switchIfEmpty(ServerResponse.notFound().build())
                ;
    }

    // [DELETE] delete one
    public Mono<ServerResponse> delete(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return boardDocumentHandlerUtils
                .checkCurrentUserWriteDocument(id)
                .then(boardDocumentHandlerUtils.removeReplies(id))
                .then(boardDocumentRepository.removeByDisplayId(id))
                .then(ServerResponse.noContent().build())
                ;
    }

    // [GET] retrieve own replies
    public Mono<ServerResponse> retrieveMyReplies(ServerRequest request) {
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return boardDocumentHandlerUtils
                .findOwnReplies(pagingParam)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [POST] create reply
    public Mono<ServerResponse> createReply(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return boardDocumentHandlerUtils
                .createReply(id, request.formData())
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.created(URI.create("/api/board" + id))::body)
                ;
    }

    // [PUT] update reply
    public Mono<ServerResponse> updateReply(ServerRequest request) {
        var id = request.pathVariable("id");
        log.info("Update Reply : {}", id);
        return boardDocumentHandlerUtils
                .updateReply(id, request.formData())
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [DELETE] delete reply
    public Mono<ServerResponse> deleteReply(ServerRequest request) {
        var id = request.pathVariable("id");
        return boardDocumentHandlerUtils
                .deleteReply(id)
                .then(ServerResponse.noContent().build())
                ;

    }
}
