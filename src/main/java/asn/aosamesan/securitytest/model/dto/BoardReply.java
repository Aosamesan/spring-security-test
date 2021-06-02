package asn.aosamesan.securitytest.model.dto;

import com.mongodb.lang.Nullable;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardReply {
    @Id
    private String id;
    @Indexed
    private String authorUsername;
    @Indexed
    private long documentDisplayId;
    private String content;
    @CreatedDate
    private Date createdAt;
    @LastModifiedDate @Nullable
    private Date updatedAt;

    @Transient @With
    private String authorNickname;
}
