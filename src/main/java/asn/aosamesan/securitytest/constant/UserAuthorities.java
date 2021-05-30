package asn.aosamesan.securitytest.constant;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public enum UserAuthorities implements GrantedAuthority {
    READ            (0b0000_0001, "READ"),
    WRITE           (0b0000_0010, "WRITE"),
    WRITE_INFO      (0b0000_0100, "WRITE_INFO"),
    USER_CONFIG     (0b0001_0000, "USER_CONFIG")

    ;
    public static final int NULL_PERMISSION = 0b0000_0000;

    private static final String USER_AUTHORITY_PREFIX = "ROLE_";

    private final int permission;
    private final String name;

    UserAuthorities(int permission, String name) {
        this.permission = permission;
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return USER_AUTHORITY_PREFIX + name;
    }

    public static Collection<UserAuthorities> fromPermission(int permission) {
        return Arrays.stream(values())
                .filter(userAuthorities -> (userAuthorities.getPermission() & permission) != 0)
                .collect(Collectors.toList());
    }
}
