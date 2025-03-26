package net.virtualboss.job.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.virtualboss.common.model.entity.QJob;

import java.util.Optional;

@Getter
@AllArgsConstructor
@Builder
public class JobFilterCriteria {

    private String findString;
    private Boolean isDeleted;

    public BooleanBuilder toPredicate() {
        QJob job = QJob.job;
        BooleanBuilder builder = newBooleanBuilder();

        applySearchFilter(job, builder);
        applyDeletionFilter(job, builder);

        return builder;
    }

    private void applySearchFilter(QJob job, BooleanBuilder builder) {
        Optional.ofNullable(findString)
                .filter(str -> !str.isEmpty())
                .ifPresent(searchStr ->
                        builder.and(createSearchPredicate(job, searchStr.toLowerCase()))
                );
    }

    private void applyDeletionFilter(QJob job, BooleanBuilder builder) {
        Optional.ofNullable(isDeleted)
                .ifPresent(flag -> builder.and(job.isDeleted.eq(flag)));
    }


    private BooleanExpression createSearchPredicate(QJob job, String searchStr) {
        return job.number.containsIgnoreCase(searchStr)
                .or(job.subdivision.containsIgnoreCase(searchStr))
                .or(job.lot.containsIgnoreCase(searchStr))
                .or(job.directions.containsIgnoreCase(searchStr))
                .or(job.notes.containsIgnoreCase(searchStr))
                .or(job.ownerName.containsIgnoreCase(searchStr))
                .or(job.company.containsIgnoreCase(searchStr))
                .or(job.customFieldsAndListsValues.any().value.containsIgnoreCase(searchStr));
    }

        private BooleanBuilder newBooleanBuilder() {
        return new BooleanBuilder();
    }
}
