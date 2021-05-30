package asn.aosamesan.securitytest.security;

import asn.aosamesan.securitytest.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserAuthenticationManager implements ReactiveAuthenticationManager {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAuthenticationManager(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var username = (String) authentication.getPrincipal();
        var password = (String) authentication.getCredentials();

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)))
                .flatMap(user -> {
                    // check password
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new BadCredentialsException("Invalid password"));
                    }
                    var token = new UsernamePasswordAuthenticationToken(
                            username,
                            user.getPassword(),
                            user.getAuthorities()
                    );
                    token.setDetails(user);
                    // update last login at
                    var now = DateTime.now().toDate();
                    user.setLastLoggedInAt(now);
                    return userRepository.save(user)
                            .then(Mono.just(token));
                });
    }
}
