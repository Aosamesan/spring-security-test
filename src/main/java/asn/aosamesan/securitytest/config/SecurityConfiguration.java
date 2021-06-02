package asn.aosamesan.securitytest.config;

import asn.aosamesan.securitytest.constant.UserAuthorities;
import asn.aosamesan.securitytest.security.UserAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfiguration {
    private UserAuthenticationManager userAuthenticationManager;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf()
                .disable()
                // AuthenticationManager 설정
                .authenticationManager(userAuthenticationManager)
                // 로그인 폼 설정
                .formLogin()
                .loginPage("/login")
                .and()
                // 로그아웃 설정
                .logout()
                .and()
                .authorizeExchange()
                // 전체 가능
                .pathMatchers("/", "/static/**", "/login", "/signup", "/favicon.ico")
                .permitAll()
                .pathMatchers(HttpMethod.POST, "/api/users")
                .permitAll()
                // 로그인 시 가능
                .pathMatchers("/api/users", "/user", "/api/documents/my", "/api/replies")
                .authenticated()
                // WRITE 가 있을 때만 가능
                .pathMatchers(HttpMethod.POST, "/api/documents")
                .hasAuthority(UserAuthorities.WRITE.getAuthority())
                .pathMatchers(HttpMethod.PUT, "/api/documents/{id:[\\d+]}")
                .hasAuthority(UserAuthorities.WRITE.getAuthority())
                .pathMatchers(HttpMethod.DELETE, "/api/documents/{id:[\\d+]}")
                .hasAuthority(UserAuthorities.WRITE.getAuthority())
                .pathMatchers("/board/new", "/board/{id:[\\d+]}/edit")
                .hasAuthority(UserAuthorities.WRITE.getAuthority())
                .pathMatchers("/api/documents/{id:[\\d]+}/replies", "/api/replies/{id}")
                .hasAuthority(UserAuthorities.WRITE.getAuthority())
                // READ 가 있을 때만 가능
                .pathMatchers(
                        "/board",
                        "/board/{id:[\\d+]}",
                        "/board/my",
                        "/replies/my",
                        "/api/documents",
                        "/api/documents/{id:[\\d+]}",
                        "/api/documents/users/{username}"
                )
                .hasAuthority(UserAuthorities.READ.getAuthority())
                .pathMatchers(HttpMethod.GET, "/api/documents/{id:[\\d+]}")
                .hasAuthority(UserAuthorities.READ.getAuthority())
                // WRITE_INFO 가능
                .pathMatchers("/api/documents/freeze/{id:[\\d+]}")
                .hasAuthority(UserAuthorities.WRITE_INFO.getAuthority())
                // USER_CONFIG만 가능
                .pathMatchers("/admin", "/api/admin/**")
                .hasAnyAuthority(UserAuthorities.USER_CONFIG.getAuthority())
                // 나머지는 거부
                .anyExchange()
                .denyAll()
                .and()
                .build()
                ;
    }

    @Autowired
    public void setUserAuthenticationManager(UserAuthenticationManager userAuthenticationManager) {
        this.userAuthenticationManager = userAuthenticationManager;
    }
}
