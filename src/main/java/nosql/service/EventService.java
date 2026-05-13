package nosql.service;

import lombok.RequiredArgsConstructor;
import nosql.api.dto.EventReviewsResponse;
import nosql.api.dto.EventListItemResponse;
import nosql.api.dto.EventsResponse;
import nosql.api.dto.LocationResponse;
import nosql.api.dto.ReactionsResponse;
import nosql.api.dto.ReviewResponse;
import nosql.api.dto.ReviewsResponse;
import nosql.cassandra.CassandraReviewsRepository;
import nosql.cassandra.CassandraReactionsRepository;
import nosql.cassandra.EventReview;
import nosql.cassandra.Reaction;
import nosql.cassandra.ReactionKey;
import nosql.cassandra.ReviewKey;
import nosql.model.CreateEventRequest;
import nosql.model.CreateReviewRequest;
import nosql.model.EventSearchCriteria;
import nosql.model.UpdateEventRequest;
import nosql.model.UpdateReviewRequest;
import nosql.mongo.*;
import nosql.redis.RedisReviewsRepository;
import nosql.redis.RedisReactionsRepository;
import nosql.utils.EventUtils.DuplicateEventException;
import nosql.utils.EventUtils.EventEditForbiddenException;
import nosql.utils.EventUtils.EventNotFoundException;
import nosql.utils.ReviewUtils.ReviewAlreadyExistsException;
import nosql.utils.ReviewUtils.ReviewEventNotFoundException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    private final CassandraReactionsRepository cassandraReactionsRepository;
    private final CassandraReviewsRepository cassandraReviewsRepository;
    private final RedisReactionsRepository redisReactionsRepository;
    private final RedisReviewsRepository redisReviewsRepository;
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
                .map(e -> toResponse(e, criteria))
                .toList();
        return new EventsResponse(items, items.size());
    }

    public EventListItemResponse findById(String id, EventSearchCriteria criteria) {
        return eventRepository.findById(id)
                .map(e -> toResponse(e, criteria))
                .orElseThrow(EventNotFoundException::new);
    }

    public EventsResponse findByOrganizerId(String userId, EventSearchCriteria criteria) {
        var query = new Query()
                .with(Sort.by(Sort.Direction.ASC, EVENT_PROPERTY))
                .addCriteria(Criteria.where(CREATED_BY_FIELD).is(userId));
        addCommonCriteria(query, criteria);

        var documentsStream = prepareDocumentsStream(query, criteria);
        var items = documentsStream
                .map(e -> toResponse(e, criteria))
                .toList();
        return new EventsResponse(items, items.size());
    }

    public void react(String eventId, String userId, boolean isLiked) {
        var event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);
        var existingReaction = cassandraReactionsRepository.findFirstByKeyEventIdAndKeyCreatedBy(eventId, userId);
        var reaction = existingReaction == null?
                Reaction.builder()
                        .key(new ReactionKey(eventId, userId))
                .build() :
                existingReaction;
        reaction.setCreatedAt(Timestamp.from(Instant.now()));
        reaction.setLikeValue(isLiked ? 1 : -1);
        cassandraReactionsRepository.save(reaction);
        refreshReactionsCache(event.getTitle(), existingReaction != null? existingReaction.isLike() : null, isLiked);
    }

    public ReactionsResponse getReactionsByEventId(String eventId) {
        var event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);
        return getReactionsByTitle(event.getTitle());
    }

    public String createReview(String eventId, CreateReviewRequest request, String userId) {
        var event = eventRepository.findById(eventId).orElseThrow(ReviewEventNotFoundException::new);
        var existingReview = cassandraReviewsRepository.findFirstByKeyEventIdAndKeyCreatedBy(eventId, userId);
        if (existingReview != null) {
            throw new ReviewAlreadyExistsException();
        }

        var now = Timestamp.from(Instant.now());
        var review = EventReview.builder()
                .key(new ReviewKey(eventId, userId))
                .id(UUID.randomUUID())
                .comment(request.comment())
                .rating(request.rating())
                .createdAt(now)
                .updatedAt(now)
                .build();
        cassandraReviewsRepository.save(review);
        rebuildReviewsCache(event.getTitle());
        return review.getId().toString();
    }

    public ReviewsResponse findReviews(String eventId, Integer limit, Integer offset) {
        eventRepository.findById(eventId).orElseThrow(ReviewEventNotFoundException::new);
        Stream<EventReview> stream = cassandraReviewsRepository.findReviewsByKeyEventId(eventId).stream()
                .sorted(Comparator.comparing(EventReview::getCreatedAt).reversed());
        if (offset != null) {
            stream = stream.skip(offset);
        }
        if (limit != null) {
            stream = stream.limit(limit);
        }
        List<ReviewResponse> reviews = stream.map(this::toReviewResponse).toList();
        return new ReviewsResponse(reviews, reviews.size());
    }

    public void updateReview(String eventId, String reviewId, UpdateReviewRequest request, String userId) {
        var event = eventRepository.findById(eventId).orElseThrow(ReviewEventNotFoundException::new);
        var review = cassandraReviewsRepository.findFirstByKeyEventIdAndKeyCreatedBy(eventId, userId);
        if (review == null || !reviewIdMatches(review, reviewId)) {
            throw new ReviewEventNotFoundException();
        }

        if (request.comment() != null) {
            review.setComment(request.comment());
        }
        if (request.rating() != null) {
            review.setRating(request.rating());
        }
        review.setUpdatedAt(Timestamp.from(Instant.now()));
        cassandraReviewsRepository.save(review);
        rebuildReviewsCache(event.getTitle());
    }

    private ReactionsResponse getReactionsByTitle(String title) {
        var cached = redisReactionsRepository.getReactions(title);
        if (cached != null) {
            return cached;
        }
        return rebuildReactionsCache(title);
    }

    private EventReviewsResponse getReviewsByTitle(String title) {
        var cached = redisReviewsRepository.getReviews(title);
        if (cached != null) {
            return cached;
        }
        return rebuildReviewsCache(title);
    }

    private void refreshReactionsCache(String title, Boolean previousIsLike, boolean currentIsLike) {
        var cached = redisReactionsRepository.getReactions(title);
        if (cached != null) {
            redisReactionsRepository.updateEventReactions(title, previousIsLike, currentIsLike);
        } else {
            rebuildReactionsCache(title);
        }
    }

    private ReactionsResponse rebuildReactionsCache(String title) {
        var query = new Query().addCriteria(Criteria.where(TITLE_FIELD).is(title));
        var eventsWithSameTitle = mongoTemplate.find(query, EventDocument.class);
        long likes = 0;
        long dislikes = 0;
        boolean hasAnyReaction = false;
        for (var eventDocument : eventsWithSameTitle) {
            var reactions = cassandraReactionsRepository.findReactionsByKeyEventId(eventDocument.getId());
            if (!reactions.isEmpty()) {
                hasAnyReaction = true;
            }
            likes += reactions.stream().filter(Reaction::isLike).count();
            dislikes += reactions.stream().filter(reaction -> !reaction.isLike()).count();
        }
        if (hasAnyReaction) {
            return redisReactionsRepository.save(title, likes, dislikes);
        }
        redisReactionsRepository.remove(title);
        return new ReactionsResponse(0L, 0L);
    }

    private EventReviewsResponse rebuildReviewsCache(String title) {
        var query = new Query().addCriteria(Criteria.where(TITLE_FIELD).is(title));
        var eventsWithSameTitle = mongoTemplate.find(query, EventDocument.class);
        long count = 0;
        long ratingSum = 0;
        for (var eventDocument : eventsWithSameTitle) {
            var reviews = cassandraReviewsRepository.findReviewsByKeyEventId(eventDocument.getId());
            count += reviews.size();
            ratingSum += reviews.stream().mapToLong(EventReview::getRating).sum();
        }
        if (count == 0) {
            redisReviewsRepository.remove(title);
            return new EventReviewsResponse(0, 0.0);
        }
        var rating = BigDecimal.valueOf((double) ratingSum / count)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        return redisReviewsRepository.save(title, count, rating);
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

    private EventListItemResponse toResponse(EventDocument document, EventSearchCriteria criteria) {
        var eventListBuilder = EventListItemResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .category(document.getCategory())
                .price(document.getPrice())
                .description(document.getDescription())
                .location(toLocationResponse(document.getLocation()))
                .createdBy(document.getCreatedBy())
                .createdAt(document.getCreatedAt())
                .startedAt(document.getStartedAt())
                .finishedAt(document.getFinishedAt());
        if (criteria.includeReactions()) {
            eventListBuilder.reactions(getReactionsByEventId(document.getId()));
        }
        if (criteria.includeReviews()) {
            eventListBuilder.reviews(getReviewsByTitle(document.getTitle()));
        }
        var eventList = eventListBuilder.build();
        eventList.validate();
        return eventList;
    }

    private LocationResponse toLocationResponse(EventLocation location) {
        if (location == null) return null;
        return new LocationResponse(location.city(), location.address());
    }

    private ReviewResponse toReviewResponse(EventReview review) {
        return new ReviewResponse(
                review.getId().toString(),
                review.getEventId(),
                review.getComment(),
                formatTimestamp(review.getCreatedAt()),
                review.getCreatedBy(),
                review.getRating(),
                formatTimestamp(review.getUpdatedAt())
        );
    }

    private String formatTimestamp(Timestamp timestamp) {
        return OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault())
                .withNano(0)
                .toString();
    }

    private boolean reviewIdMatches(EventReview review, String reviewId) {
        try {
            return review.getId().equals(UUID.fromString(reviewId));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
