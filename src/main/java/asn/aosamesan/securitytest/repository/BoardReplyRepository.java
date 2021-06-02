package asn.aosamesan.securitytest.repository;

import asn.aosamesan.securitytest.model.dto.BoardReply;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BoardReplyRepository extends ReactiveMongoRepository<BoardReply, String> {
    Flux<BoardReply> findAllByDocumentDisplayId(long documentDisplayId);
    Mono<Long> countAllByDocumentDisplayId(long documentDisplayId);
    Flux<BoardReply> findAllByAuthorUsernameOrderByCreatedAtDesc(String authorUsername);
    Mono<Long> countAllByAuthorUsername(String authorUsername);
    Mono<Void> removeAllByDocumentDisplayId(long documentDisplayId);
}
