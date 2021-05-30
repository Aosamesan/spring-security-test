package asn.aosamesan.securitytest.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class PageableResult<T> {
    private final List<? extends T> itemList;
    private final long total;
    private final PagingParameter pagingInfo;

    public static <T, L extends List<? extends T>> Function<Tuple2<L, Long>, PageableResult<T>> fromTuple(PagingParameter pagingInfo) {
        return tuple -> new PageableResult<T>(tuple.getT1(), tuple.getT2(), pagingInfo);
    }
}
