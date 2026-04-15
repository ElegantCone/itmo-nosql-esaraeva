package lab1.service;

import lab1.api.dto.EventListItemResponse;
import lab1.api.dto.EventsResponse;
import lab1.api.dto.LocationResponse;
import lab1.model.CreateEventRequest;
import lab1.model.EventSearchCriteria;
import lab1.mongo.EventDocument;
import lab1.mongo.EventLocation;
import lab1.mongo.EventRepository;
import lab1.utils.EventUtils;
import lab1.utils.EventUtils.DuplicateEventException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final MongoTemplate mongoTemplate;
    private static final String EVENT_PROPERTY = "_id";

    public String create(CreateEventRequest request, String userId) {
        var event = EventDocument.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(new EventLocation(request.getAddress()))
                .createdAt(OffsetDateTime.now().toString())
                .createdBy(userId)
                .startedAt(request.getStartedAt())
                .finishedAt(request.getFinishedAt())
                .build();

        try {
            return eventRepository.save(event).getId();
        } catch (DuplicateKeyException exception) {
            throw new DuplicateEventException();
        }
    }

    public EventsResponse findAll(EventSearchCriteria criteria) {
        var query = new Query().with(Sort.by(Sort.Direction.ASC, EVENT_PROPERTY));
        if (criteria.title() != null) {
            var titlePattern = ".*" + Pattern.quote(criteria.title()) + ".*";
            query.addCriteria(Criteria.where(EventUtils.TITLE_FIELD).regex(titlePattern));
        }
        if (criteria.offset() != null) {
            query.skip(criteria.offset());
        }
        if (criteria.limit() != null) {
            query.limit(criteria.limit());
        }

        var items = mongoTemplate.find(query, EventDocument.class).stream()
                .map(this::toResponse)
                .toList();
        return new EventsResponse(items, items.size());
    }

    private EventListItemResponse toResponse(EventDocument document) {
        var eventList = new EventListItemResponse(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                new LocationResponse(document.getLocation().getAddress()),
                document.getCreatedAt(),
                document.getCreatedBy(),
                document.getStartedAt(),
                document.getFinishedAt()
        );
        eventList.validate();
        return eventList;
    }
}
