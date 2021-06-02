package asn.aosamesan.securitytest.utils;

import asn.aosamesan.securitytest.constant.UserAuthorities;
import asn.aosamesan.securitytest.model.api.PageableResult;
import asn.aosamesan.securitytest.model.api.PagingParameter;
import asn.aosamesan.securitytest.model.dto.BoardDocument;
import asn.aosamesan.securitytest.model.dto.BoardReply;
import asn.aosamesan.securitytest.model.dto.User;
import asn.aosamesan.securitytest.repository.BoardDocumentRepository;
import asn.aosamesan.securitytest.repository.BoardReplyRepository;
import asn.aosamesan.securitytest.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Comparator;

@Slf4j
@Component
public class BoardDocumentHandlerUtils {
    private final BoardDocumentRepository boardDocumentRepository;
    private final BoardReplyRepository boardReplyRepository;
    private final UserRepository userRepository;

    public BoardDocumentHandlerUtils(BoardDocumentRepository boardDocumentRepository, BoardReplyRepository boardReplyRepository, UserRepository userRepository) {
        this.boardDocumentRepository = boardDocumentRepository;
        this.boardReplyRepository = boardReplyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create BoardDocument
     * @param formDataMono request.formData()
     * @return BoardDocument Mono
     */
    public Mono<BoardDocument> createOne(Mono<MultiValueMap<String, String>> formDataMono) {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .zipWith(formDataMono)
                .flatMap(
                        tuple -> {
                            var user = tuple.getT1();
                            var username = user.getUsername();
                            var formData = tuple.getT2();
                            var title = formData.getFirst("title");
                            var content = formData.getFirst("content");
                            var isNotice = formData.containsKey("isNotice") && Boolean.parseBoolean(formData.getFirst("isNotice"));

                            if (isNotice && !user.getAuthorities().contains(UserAuthorities.WRITE_INFO)) {
                                return Mono.error(new BadCredentialsException("공지 등록 권한이 없음."));
                            }

                            var now = DateTime.now().toDate();
                            return boardDocumentRepository
                                    .getNextDisplayId()
                                    .map(nextDisplayId -> BoardDocument.builder()
                                            .displayId(nextDisplayId)
                                            .title(title)
                                            .content(content)
                                            .authorUsername(username)
                                            .createdAt(now)
                                            .isFrozen(false)
                                            .isNotice(isNotice)
                                            .build()
                                    );
                        })
                ;
    }

    /**
     * findOne
     * @param displayId displayID
     * @return BoardDocument
     */
    public Mono<BoardDocument> findOne(long displayId) {
        return boardDocumentRepository
                .findBoardDocumentByDisplayId(displayId)
                .flatMap(this::withAuthorNickname)
                .flatMap(this::withReplies)
                ;
    }

    /**
     * findAll
     * @param pagingParam pagingParam
     * @return BoardDocument PageableResult
     */
    public Mono<PageableResult<BoardDocument>> findAll(PagingParameter pagingParam) {
        return boardDocumentRepository.findAll(Sort.by("createdAt").descending())
                .sort(Comparator.comparing(BoardDocument::isNotice).reversed())
                .skip(pagingParam.getStart())
                .take(pagingParam.getDisplay())
                .flatMapSequential(this::withAuthorNickname)
                .flatMapSequential(this::withRepliesCountOnly)
                .collectList()
                .zipWith(boardDocumentRepository.count())
                .map(PageableResult.fromTuple(pagingParam))
                ;
    }

    /**
     * find own documents
     * @param pagingParam pagingParam
     * @return BoardDocument PageableResult
     */
    public Mono<PageableResult<BoardDocument>> findOwnDocuments(PagingParameter pagingParam) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .map(User::getUsername)
                .flatMap(username -> boardDocumentRepository
                        .findAllByAuthorUsernameOrderByCreatedAtDesc(username)
                        .skip(pagingParam.getStart())
                        .take(pagingParam.getDisplay())
                        .flatMap(this::withAuthorNickname)
                        .flatMap(this::withRepliesCountOnly)
                        .collectList()
                        .zipWith(boardDocumentRepository.countAllByAuthorUsername(username)))
                .map(PageableResult.fromTuple(pagingParam))
                ;
    }

    /**
     * find documents by username
     * @param pagingParam pagingParam
     * @param username username
     * @return BoardDocument PageableResult
     */
    public Mono<PageableResult<BoardDocument>> findDocumentsByUsername(PagingParameter pagingParam, String username) {
        return boardDocumentRepository.findAllByAuthorUsernameOrderByCreatedAtDesc(username)
                .skip(pagingParam.getStart())
                .take(pagingParam.getDisplay())
                .flatMapSequential(this::withAuthorNickname)
                .flatMapSequential(this::withRepliesCountOnly)
                .collectList()
                .zipWith(boardDocumentRepository.countAllByAuthorUsername(username))
                .map(PageableResult.fromTuple(pagingParam))
                ;
    }

    /**
     * find own replies
     * @param pagingParam pagingParam
     * @return BoardReply PageableResult
     */
    public Mono<PageableResult<BoardReply>> findOwnReplies(PagingParameter pagingParam) {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .flatMap(
                        user -> {
                            var username = user.getUsername();
                            return boardReplyRepository.findAllByAuthorUsernameOrderByCreatedAtDesc(username)
                                    .skip(pagingParam.getStart())
                                    .take(pagingParam.getDisplay())
                                    .collectList()
                                    .zipWith(boardReplyRepository.countAllByAuthorUsername(username))
                                    .map(PageableResult.fromTuple(pagingParam))
                                    ;
                        }
                );
    }

    /**
     * Create reply
     * @param documentDisplayId documentDisplayId
     * @param formDataMono  formDataMono
     * @return BoardReply Mono
     */
    public Mono<BoardReply> createReply(long documentDisplayId, Mono<MultiValueMap<String, String>> formDataMono) {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .zipWith(formDataMono)
                .flatMap(
                        tuple -> {
                            var user = tuple.getT1();
                            var formData = tuple.getT2();
                            var content = formData.getFirst("content");
                            var now = DateTime.now().toDate();
                            var reply = BoardReply.builder()
                                    .content(content)
                                    .authorUsername(user.getUsername())
                                    .documentDisplayId(documentDisplayId)
                                    .createdAt(now)
                                    .build();
                            return boardReplyRepository.save(reply);
                        }
                )
                ;
    }

    /**
     * Update reply
     * @param replyId replyId
     * @param formDataMono formDataMono
     * @return BoardReply Mono
     */
    public Mono<BoardReply> updateReply(String replyId, Mono<MultiValueMap<String, String>> formDataMono) {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .zipWith(boardReplyRepository.findById(replyId))
                .filter(tuple -> tuple.getT1().getUsername().equals(tuple.getT2().getAuthorUsername()))
                .map(Tuple2::getT2)
                .zipWith(formDataMono)
                .flatMap(
                        tuple -> {
                            var stored = tuple.getT1();
                            var formData = tuple.getT2();
                            var content = formData.getFirst("content");
                            var now = DateTime.now().toDate();
                            if (!stored.getContent().equals(content)) {
                                stored.setContent(content);
                                stored.setUpdatedAt(now);
                                return boardReplyRepository.save(stored);
                            }
                            return Mono.just(stored);
                        }
                )
                ;
    }

    /**
     * Delete reply
     * @param replyId replyId
     * @return Void Mono
     */
    public Mono<Void> deleteReply(String replyId) {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .zipWith(boardReplyRepository.findById(replyId))
                .filter(tuple -> tuple.getT1().getUsername().equals(tuple.getT2().getAuthorUsername()))
                .then(boardReplyRepository.deleteById(replyId))
                ;
    }

    public Mono<Void> removeReplies(long documentDisplayId) {
        return boardReplyRepository.removeAllByDocumentDisplayId(documentDisplayId);
    }


    public Mono<BoardDocument> checkCurrentUserWriteDocument(long id) {
        return boardDocumentRepository.findBoardDocumentByDisplayId(id)
                .zipWith(ReactiveSecurityContextHolder
                        .getContext()
                        .map(SecurityContext::getAuthentication)
                        .map(Authentication::getDetails)
                        .cast(User.class)
                        .map(User::getUsername))
                .filter(tuple -> tuple.getT1().getAuthorUsername().equals(tuple.getT2()))
                .map(Tuple2::getT1)
                ;
    }

    private Mono<BoardDocument> withAuthorNickname(BoardDocument boardDocument) {
        return userRepository.findByUsername(boardDocument.getAuthorUsername())
                .map(User::getNickname)
                .map(boardDocument::withAuthorNickname)
                ;
    }

    private Mono<BoardDocument> withReplies(BoardDocument boardDocument) {
        return boardReplyRepository.findAllByDocumentDisplayId(boardDocument.getDisplayId())
                .sort(Comparator.comparing(BoardReply::getCreatedAt))
                .flatMapSequential(boardReply -> userRepository.findByUsername(boardReply.getAuthorUsername())
                        .map(User::getNickname)
                        .map(boardReply::withAuthorNickname))
                .collectList()
                .map(boardDocument::withReplies)
                .zipWith(boardReplyRepository.countAllByDocumentDisplayId(boardDocument.getDisplayId()))
                .map(tuple -> tuple.getT1().withReplyCount(tuple.getT2()))
                ;
    }

    private Mono<BoardDocument> withRepliesCountOnly(BoardDocument boardDocument) {
        return boardReplyRepository.countAllByDocumentDisplayId(boardDocument.getDisplayId())
                .map(boardDocument::withReplyCount)
                ;
    }
}
