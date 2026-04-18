package nosql.service;

import nosql.api.dto.EventListItemResponse;
import nosql.api.dto.EventsResponse;
import nosql.api.dto.LocationResponse;
import nosql.model.CreateEventRequest;
import nosql.model.EventSearchCriteria;
import nosql.model.UpdateEventRequest;
import nosql.mongo.*;
import nosql.utils.EventUtils.DuplicateEventException;
import nosql.utils.EventUtils.EventEditForbiddenException;
import nosql.utils.EventUtils.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static nosql.mongo.EventRepository.EVENT_PROPERTY;
import static nosql.params.EventListItemParams.CREATED_BY_FIELD;
import static nosql.params.EventListItemParams.LOCATION_FIELD;
import static nosql.params.EventParams.*;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public String create(CreateEventRequest request, String userId) {
        var event = EventDocument.builder()
                .title(request.title())
                .description(request.description())
                .location(new EventLocation(null, request.address()))
                .createdAt(OffsetDateTime.now().toString())
                .createdBy(userId)
                .startedAt(request.startedAt())
                .finishedAt(request.finishedAt())
                .build();

        try {
            return eventRepository.save(event).getId();
        } catch (DuplicateKeyException exception) {
            throw new DuplicateEventException();
        }
    }

    public void update(String eventId, UpdateEventRequest request, String userId) {
        var query = new Query().addCriteria(Criteria.where(EVENT_PROPERTY).is(eventId).and(CREATED_BY_FIELD).is(userId));
        var update = new Update();
        if (request.category() != null) {
            update.set(CATEGORY_FIELD, request.category());
        }
        if (request.price() != null) {
            update.set(PRICE_FIELD, request.price());
        }
        var city = request.city();
        if (city != null) {
            if (Strings.isBlank(city)) {
                update.unset(LOCATION_FIELD + '.' + CITY_FIELD);
            } else {
                update.set(LOCATION_FIELD + '.' + CITY_FIELD, city);
            }
        }
        var result = mongoTemplate.updateFirst(query, update, EventDocument.class);
        if (result.getMatchedCount() == 0) {
            throw new EventEditForbiddenException();
        }
    }

    public EventsResponse findAll(EventSearchCriteria criteria) {
        var query = new Query().with(Sort.by(Sort.Direction.ASC, EVENT_PROPERTY));
        addCommonCriteria(query, criteria);

        var documentsStream = prepareDocumentsStream(query, criteria);

        var items = documentsStream
                .map(this::toResponse)
                .toList();
        return new EventsResponse(items, items.size());
    }

    public EventListItemResponse findById(String id) {
        return eventRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(EventNotFoundException::new);
    }

    public EventsResponse findByOrganizerId(String userId, EventSearchCriteria criteria) {
        var query = new Query()
                .with(Sort.by(Sort.Direction.ASC, EVENT_PROPERTY))
                .addCriteria(Criteria.where(CREATED_BY_FIELD).is(userId));
        addCommonCriteria(query, criteria);

        var documentsStream = prepareDocumentsStream(query, criteria);
        var items = documentsStream
                .map(this::toResponse)
                .toList();
        return new EventsResponse(items, items.size());
    }

    private Stream<EventDocument> prepareDocumentsStream(Query query, EventSearchCriteria criteria) {
        var stream = mongoTemplate.find(query, EventDocument.class).stream()
                .filter(document -> matchesDate(document.getStartedAt(), criteria.dateFrom(), criteria.dateTo()));
        if (criteria.offset() != null) {
            stream = stream.skip(criteria.offset());
        }
        if (criteria.limit() != null) {
            stream = stream.limit(criteria.limit());
        }
        return stream;
    }

    private void addCommonCriteria(Query query, EventSearchCriteria criteria) {
        if (criteria.id() != null) {
            query.addCriteria(Criteria.where(EVENT_PROPERTY).is(criteria.id()));
        }
        if (criteria.title() != null) {
            var titlePattern = ".*" + Pattern.quote(criteria.title()) + ".*";
            query.addCriteria(Criteria.where(TITLE_FIELD).regex(titlePattern));
        }
        if (criteria.category() != null) {
            query.addCriteria(Criteria.where(CATEGORY_FIELD).is(criteria.category()));
        }
        if (criteria.city() != null) {
            query.addCriteria(Criteria.where(LOCATION_FIELD + '.' + CITY_FIELD).is(criteria.city()));
        }
        if (criteria.priceFrom() != null || criteria.priceTo() != null) {
            var priceCriteria = Criteria.where(PRICE_FIELD);
            if (criteria.priceFrom() != null) {
                priceCriteria.gte(criteria.priceFrom());
            }
            if (criteria.priceTo() != null) {
                priceCriteria.lte(criteria.priceTo());
            }
            query.addCriteria(priceCriteria);
        }
        if (criteria.username() != null) {
            Optional<String> userId = userRepository.findByUsername(criteria.username()).map(UserDocument::getId);
            if (userId.isPresent()) {
                query.addCriteria(Criteria.where(CREATED_BY_FIELD).is(userId.get()));
            } else {
                query.addCriteria(Criteria.where(EVENT_PROPERTY).exists(false));
            }
        }
    }

    private boolean matchesDate(String startedAt, LocalDate dateFrom, LocalDate dateTo) {
        var startedDate = OffsetDateTime.parse(startedAt).toLocalDate();
        if (dateFrom != null && startedDate.isBefore(dateFrom)) {
            return false;
        }
        return dateTo == null || !startedDate.isAfter(dateTo);
    }

    private EventListItemResponse toResponse(EventDocument document) {
        var eventList = new EventListItemResponse(
                document.getId(),
                document.getTitle(),
                document.getCategory(),
                document.getPrice(),
                document.getDescription(),
                toLocationResponse(document.getLocation()),
                document.getCreatedAt(),
                document.getCreatedBy(),
                document.getStartedAt(),
                document.getFinishedAt()
        );
        eventList.validate();
        return eventList;
    }

    private LocationResponse toLocationResponse(EventLocation location) {
        if (location == null) return null;
        return new LocationResponse(location.city(), location.address());
    }
}
