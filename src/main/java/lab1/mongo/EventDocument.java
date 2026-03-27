package lab1.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "events")
@CompoundIndex(name = "title_created_by_idx", def = "{'title': 1, 'created_by': 1}")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("location")
    private EventLocation location;

    @Field("created_at")
    private String createdAt;

    @Indexed
    @Field("created_by")
    private String createdBy;

    @Field("started_at")
    private String startedAt;

    @Field("finished_at")
    private String finishedAt;
}
