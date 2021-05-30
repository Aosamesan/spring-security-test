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
                .pathMatchers("/", "/static/**", "/login", "/signup")
                .permitAll()
                .pathMatchers(HttpMethod.POST, "/api/users")
                .permitAll()
                // 로그인 시 가능
                .pathMatchers("/api/users", "/user")
                .authenticated()
                // READ 가 있을 때만 가능
                .pathMatchers("/board")
                .hasAuthority(UserAuthorities.READ.getAuthority())
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
