package net.virtualboss.common.service;

import com.querydsl.core.JoinType;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.model.entity.Field;
import net.virtualboss.common.repository.FieldRepository;
import net.virtualboss.common.util.DtoFlattener;
import net.virtualboss.common.util.QueryDslUtil;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
public abstract class GenericService<E, K, R, Q extends EntityPathBase<E>> {

    protected final EntityManager entityManager;
    protected final MainService mainService;
    private final Function<String, K> idConverter;
    private final JpaRepository<E, K> repository;
    private final JPAQueryFactory queryFactory;
    protected Map<String, String> sortFieldMapping = new HashMap<>();

    protected abstract Map<String, String> getCustomMappings();

    protected abstract Map<String, String> getNestedMappings();

    protected final FieldRepository fieldRepository;

    protected GenericService(EntityManager entityManager,
                             MainService mainService,
                             Function<String, K> idConverter,
                             JpaRepository<E, K> repository,
                             FieldRepository fieldRepository) {
        this.entityManager = entityManager;
        this.mainService = mainService;
        this.idConverter = idConverter;
        this.repository = repository;
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.fieldRepository = fieldRepository;
    }

    protected abstract Q getQEntity();

    protected abstract Predicate buildFilterPredicate(CommonFilter filter);

    protected abstract String getCustomFieldPrefix();

    protected abstract String getCustomFieldsAndListsPrefix();

    protected abstract String getDefaultSort();

    protected abstract String getMustHaveFields();

    public interface JoinExpression {

        default void apply(JPAQuery<?> query) {
        }
    }

    public record EntityJoin<E>(
            EntityPath<E> source,
            Path<E> alias,
            JoinType type
    ) implements JoinExpression {
        @Override
        public void apply(JPAQuery<?> query) {
            switch (type) {
                case LEFTJOIN -> query.leftJoin(source, alias);
                case INNERJOIN -> query.innerJoin(source, alias);
                case RIGHTJOIN -> query.rightJoin(source, alias);
                default -> query.join(source, alias);
            }
        }
    }

    public record CollectionJoin<E>(
            CollectionExpression<?, E> source,
            Path<E> alias,
            JoinType type
    ) implements JoinExpression {
        @Override
        public void apply(JPAQuery<?> query) {
            switch (type) {
                case LEFTJOIN -> query.leftJoin(source, alias);
                case INNERJOIN -> query.innerJoin(source, alias);
                case RIGHTJOIN -> query.rightJoin(source, alias);
                default -> query.join(source, alias);
            }
        }
    }

    public record EntityJoinWithCondition<E>(
            EntityPath<E> source,
            Predicate condition,
            JoinType type
    ) implements JoinExpression {
        @Override
        public void apply(JPAQuery<?> query) {
            switch (type) {
                case LEFTJOIN -> query.leftJoin(source).on(condition);
                case INNERJOIN -> query.innerJoin(source).on(condition);
                case RIGHTJOIN -> query.rightJoin(source).on(condition);
                default -> query.join(source).on(condition);
            }
        }
    }
    public record GroupByExpression(Expression<?> expression) {
    }

    protected List<JoinExpression> getJoins() {
        return Collections.emptyList();
    }

    protected List<GroupByExpression> getGroupBy() {
        return Collections.emptyList();
    }

    public Page<Map<String, Object>> findAll(String fields, CommonFilter filter) {
        String combinedFields = fields == null ?
                getMustHaveFields() :
                fields + "," + getMustHaveFields();

        Set<String> fieldsSet = parseFields(combinedFields);
        List<String> fieldsList = new ArrayList<>(fieldsSet);

        initializeFilterDefaults(filter);
        PageRequest pageRequest = createPageRequest(filter);


        JPAQuery<R> query = queryFactory.select(
                        QueryDslUtil.buildProjection(getResponseClass(), getQEntity(), fieldsList)
                )
                .from(getQEntity())
                .where(buildFilterPredicate(filter));

        applyJoins(query);
        applyGroupBy(query);

        long totalCount = executeCountQuery(filter);

        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageRequest);

        List<R> responses = query
                .orderBy(orderSpecifiers)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();


        List<String> comdinedFieldsList = Arrays.asList(combinedFields.split(","));

        List<Map<String, Object>> data = responses.stream()
                .map(response -> DtoFlattener.flatten(response, comdinedFieldsList))
                .toList();

        return new PageImpl<>(data, pageRequest, totalCount);
    }

    protected OrderSpecifier<Comparable<Object>>[] getOrderSpecifiers(PageRequest pageRequest) {
        return QueryDslUtil.getOrderSpecifiers(
                pageRequest.getSort(),
                getEntityClass(),
                getQEntity().getMetadata().getName()
        );
    }

    protected void initializeFilterDefaults(CommonFilter filter) {
        if (filter.getLimit() == null) filter.setLimit(Integer.MAX_VALUE);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort(getDefaultSort());
    }

    protected PageRequest createPageRequest(CommonFilter filter) {
        return PageRequest.of(
                filter.getPage() - 1,
                filter.getLimit(),
                Sort.by(parseSortOrders(filter.getSort()))
        );
    }

    private List<Sort.Order> parseSortOrders(String sortString) {
        return Arrays.stream(sortString.split(","))
                .map(this::processSortField)
                .toList();
    }

    private Sort.Order processSortField(String sortField) {
        String[] parts = sortField.trim().split(":");
        String fieldName = convertField(parts[0]);
        Sort.Direction direction = parseDirection(parts);

        return new Sort.Order(direction, fieldName);
    }

    private String convertField(String frontendField) {
        return sortFieldMapping.getOrDefault(frontendField, frontendField);
    }

    private Sort.Direction parseDirection(String[] parts) {
        if (parts.length > 1) {
            return Sort.Direction.fromString(parts[1].toUpperCase());
        }
        return Sort.Direction.ASC; // Дефолтное направление
    }

    protected Set<String> parseFields(String fields) {
        return Arrays.stream(fields.split(","))
                .map(field -> {
                    if (field.contains(getCustomFieldPrefix())) {
                        return getCustomFieldsAndListsPrefix() + "." + field;
                    }
                    return field;
                })
                .collect(Collectors.toSet());
    }

    protected abstract Class<E> getEntityClass();

    protected abstract Class<R> getResponseClass();

    private void applyJoins(JPAQuery<R> query) {
        for (JoinExpression join : getJoins()) {
            join.apply(query);
        }
    }

    private void applyGroupBy(JPAQuery<R> query) {
        if (!getGroupBy().isEmpty()) {
            query.groupBy(getGroupBy().stream()
                    .map(GroupByExpression::expression)
                    .toArray(Expression[]::new));
        }
    }

    public E findById(String id) {
        K convertedId = idConverter.apply(id);
        return repository.findById(convertedId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Entity with Id: {0} not found!", id)
                ));
    }

    private long executeCountQuery(CommonFilter filter) {
        Q root = getQEntity();
        Predicate predicate = buildFilterPredicate(filter);

        Expression<Long> countExpression = root.countDistinct();

        JPAQuery<Long> countQuery = queryFactory
                .select(countExpression)
                .from(root)
                .where(predicate);

        applyJoins((JPAQuery<R>) countQuery);
        applyGroupBy((JPAQuery<R>) countQuery);

        return countQuery.fetch().size();
    }

    @PostConstruct
    public void initSortFieldMapping() {
        fieldRepository.findAll().forEach(this::mapFieldToSorting);
    }

    private void mapFieldToSorting(Field field) {
        String fieldName = field.getName();
        String path = field.getPath();
        String mappedPath = determinePath(getCustomMappings(), fieldName, path);
        if (mappedPath.equals(path)) {
            mappedPath = determinePath(getNestedMappings(), fieldName, path);
        }
        sortFieldMapping.put(fieldName, mappedPath);
    }

    private String determinePath(Map<String, String> mappings, String fieldName, String path) {
        return mappings.entrySet().stream()
                .filter(entry -> fieldName.startsWith(entry.getKey()))
                .findFirst()
                .map(entry -> entry.getValue() + path)
                .orElse(path);
    }
}