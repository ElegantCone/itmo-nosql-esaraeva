package lab1.model;

public record EventSearchCriteria(String title,
                                  Integer limit,
                                  Integer offset
) {
}
