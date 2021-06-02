package asn.aosamesan.securitytest.repository;

import asn.aosamesan.securitytest.model.dto.BoardDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BoardDocumentRepository extends ReactiveMongoRepository<BoardDocument, String> {
    Mono<BoardDocument> findBoardDocumentByDisplayId(long displayId);
    Mono<Void> removeByDisplayId(long displayId);
    Flux<BoardDocument> findAllByAuthorUsernameOrderByCreatedAtDesc(String authorUsername);
    Mono<Long> countAllByAuthorUsername(String authorUsername);
    Mono<BoardDocument> findTopByOrderByDisplayIdDesc();

    default Mono<Long> getNextDisplayId() {
        return findTopByOrderByDisplayIdDesc()
                .map(BoardDocument::getDisplayId)
                .map(id -> id + 1)
                .switchIfEmpty(Mono.just(1L))
                ;
    }
}
