package asn.aosamesan.securitytest.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

@Getter
@AllArgsConstructor
public class PagingParameter {
    private final long start;
    private final long display;

    public long getCurrentPage() {
        if (display == 0) {
            return 0;
        }
        return (long) Math.floor((double) start / display) + 1;
    }

    public static PagingParameter fromQueryParams(MultiValueMap<String, String> queryParams) {
        var start = queryParams.getOrDefault("start", Collections.emptyList())
                .stream()
                .findFirst()
                .map(Long::parseLong)
                .orElse(0L);
        var display = queryParams.getOrDefault("display", Collections.emptyList())
                .stream()
                .findFirst()
                .map(Long::parseLong)
                .orElse(10L);
        return new PagingParameter(start, display);
    }
}
