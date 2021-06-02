package asn.aosamesan.securitytest.model.dto;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Document
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private long displayId;
    private String title;
    private String content;
    private boolean isFrozen;
    @Indexed
    private boolean isNotice;
    @Indexed
    private String authorUsername;
    @CreatedDate
    private Date createdAt;
    @LastModifiedDate @Builder.Default
    private Date updatedAt = null;

    @Transient @With
    private List<BoardReply> replies;
    @Transient @With
    private long replyCount;
    @Transient @With
    private String authorNickname;
}
