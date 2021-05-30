package asn.aosamesan.securitytest.handler;

import asn.aosamesan.securitytest.constant.UserLevel;
import asn.aosamesan.securitytest.model.api.PageableResult;
import asn.aosamesan.securitytest.model.api.PagingParameter;
import asn.aosamesan.securitytest.model.dto.User;
import asn.aosamesan.securitytest.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
public class UserHandler {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // [POST] create user
    public Mono<ServerResponse> create(ServerRequest request) {
        var now = DateTime.now().toDate();
        return request.formData()
                .map(formData -> User.builder()
                        .username(formData.getFirst("username"))
                        .password(passwordEncoder.encode(formData.getFirst("password")))
                        .nickname(formData.getFirst("nickname"))
                        .userLevel(UserLevel.READY)
                        .createdAt(now)
                        .updatedAt(now)
                        .lastPasswordModifiedAt(now)
                        .build())
                .flatMap(user -> userRepository.findByUsername(user.getUsername())
                        .flatMap(alreadyUser -> Mono.<User>error(new Exception("Already exist: " + alreadyUser.getUsername())))
                        .switchIfEmpty(userRepository.save(user)))
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.created(URI.create("/"))::body)
                ;
    }

    // [GET] retrieve current user info
    public Mono<ServerResponse> retrieveCurrentUser(ServerRequest request) {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [GET] retrieve all (for admin)
    public Mono<ServerResponse> retrieveAll(ServerRequest request) {
        var pagingParam = PagingParameter.fromQueryParams(request.queryParams());
        return userRepository.findAll(Sort.by("username").ascending())
                .skip(pagingParam.getStart())
                .take(pagingParam.getDisplay())
                .collectList()
                .zipWith(userRepository.count())
                .map(PageableResult.fromTuple(pagingParam))
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [PUT] update current user info
    public Mono<ServerResponse> updateCurrentUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .zipWith(request.formData())
                .flatMap(tuple -> {
                    var user = tuple.getT1();
                    var formData = tuple.getT2();
                    var rawPassword = formData.getFirst("password");
                    // check password
                    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                        return Mono.error(new BadCredentialsException("password unmatched"));
                    }
                    var now = DateTime.now().toDate();
                    var updated = false;

                    // nickname
                    var nickname = formData.getFirst("nickname");
                    if (nickname != null && !"".equals(nickname.trim()) && !user.getNickname().equals(nickname)) {
                        user.setNickname(nickname);
                        updated = true;
                    }

                    // password
                    var newPassword = formData.getFirst("newPassword");
                    if (newPassword != null && !"".equals(newPassword.trim()) && !passwordEncoder.matches(newPassword, user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        user.setLastPasswordModifiedAt(now);
                        updated = true;
                    }

                    if (updated) {
                        user.setUpdatedAt(now);
                    }

                    return userRepository.save(user);
                })
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [PUT] update user level for admin
    public Mono<ServerResponse> update(ServerRequest request) {
        var username = request.pathVariable("username");
        var queryParams = request.queryParams();
        if (!queryParams.containsKey("level")) {
            return ServerResponse.badRequest().build();
        }
        return userRepository.findByUsername(username)
                .map(user -> {
                    var level = request.queryParam("level")
                            .map(levelString -> switch (levelString.toUpperCase()) {
                                case "ROOT_ADMIN" -> UserLevel.ROOT_ADMIN;
                                case "ADMIN" -> UserLevel.ADMIN;
                                case "USER" -> UserLevel.USER;
                                case "BLOCK" -> UserLevel.BLOCK;
                                case "READY" -> UserLevel.READY;
                                default -> throw new EnumConstantNotPresentException(UserLevel.class, levelString);
                            })
                            .orElse(UserLevel.READY);
                    request.queryParam("level").ifPresent(l -> log.info("#### Request level : {}", l));
                    user.setUserLevel(level);
                    return user;
                })
                .flatMap(userRepository::save)
                .map(BodyInserters::fromValue)
                .flatMap(ServerResponse.ok()::body)
                ;
    }

    // [DELETE] remove current user
    public Mono<ServerResponse> deleteCurrentUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .cast(User.class)
                .map(User::getId)
                .flatMap(userRepository::deleteById)
                .then(ServerResponse.noContent().build())
                ;
    }

    // [DELETE] remove user for admin
    public Mono<ServerResponse> delete(ServerRequest request) {
        var username = request.pathVariable("username");
        return userRepository.findByUsername(username)
                .map(User::getId)
                .flatMap(userRepository::deleteById)
                .then(ServerResponse.noContent().build())
                ;
    }
}
