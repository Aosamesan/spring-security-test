package asn.aosamesan.securitytest.constant;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public enum UserLevel {
    ROOT_ADMIN(
            UserAuthorities.READ.getPermission()
                    | UserAuthorities.WRITE.getPermission()
                    | UserAuthorities.WRITE_INFO.getPermission()
                    | UserAuthorities.USER_CONFIG.getPermission()
    ),
    ADMIN(
            UserAuthorities.READ.getPermission()
                    | UserAuthorities.WRITE.getPermission()
                    | UserAuthorities.WRITE_INFO.getPermission()
    ),
    USER(
            UserAuthorities.READ.getPermission()
                    | UserAuthorities.WRITE.getPermission()
    ),
    BLOCK(
            UserAuthorities.READ.getPermission()
    ),
    READY(
            UserAuthorities.NULL_PERMISSION
    )

    ;
    private final int permission;

    UserLevel(int permission) {
        this.permission = permission;
    }

    public Collection<UserAuthorities> toAuthorities() {
        return Arrays.stream(UserAuthorities.values())
                .filter(userAuthorities -> (userAuthorities.getPermission() & permission) != 0)
                .collect(Collectors.toList())
                ;
    }
}
