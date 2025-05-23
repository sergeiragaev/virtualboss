package net.virtualboss.contact.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.virtualboss.common.model.entity.QContact;

import java.util.Optional;

@Getter
@AllArgsConstructor
@Builder
public class ContactFilterCriteria {
    private static final String UNASSIGNED_COMPANY = "unassigned";

    private String findString;
    private Boolean showUnassigned;
    private Boolean isDeleted;

    public BooleanBuilder toPredicate() {
        QContact contact = QContact.contact;
        BooleanBuilder builder = newBooleanBuilder();

        applySearchFilter(contact, builder);
        applyUnassignedFilter(contact, builder);
        applyDeletionFilter(contact, builder);

        return builder;
    }

    private BooleanBuilder newBooleanBuilder() {
        return new BooleanBuilder();
    }

    private void applySearchFilter(QContact contact, BooleanBuilder builder) {
        Optional.ofNullable(findString)
                .filter(str -> !str.isEmpty())
                .ifPresent(searchStr ->
                        builder.and(createSearchPredicate(contact, searchStr.toLowerCase()))
                );
    }

    private BooleanExpression createSearchPredicate(QContact contact, String searchStr) {
        return contact.profession.name.containsIgnoreCase(searchStr)
                .or(contact.firstName.containsIgnoreCase(searchStr))
                .or(contact.lastName.containsIgnoreCase(searchStr))
                .or(contact.notes.containsIgnoreCase(searchStr))
                .or(contact.comments.containsIgnoreCase(searchStr))
                .or(contact.supervisor.containsIgnoreCase(searchStr))
                .or(contact.spouse.containsIgnoreCase(searchStr))
                .or(contact.company.name.containsIgnoreCase(searchStr))
                .or(contact.phones.any().title.containsIgnoreCase(searchStr))
                .or(contact.addresses.any().address1.containsIgnoreCase(searchStr))
                .or(contact.addresses.any().address2.containsIgnoreCase(searchStr))
                .or(contact.addresses.any().city.containsIgnoreCase(searchStr))
                .or(contact.addresses.any().state.containsIgnoreCase(searchStr))
                .or(contact.addresses.any().postal.containsIgnoreCase(searchStr))
                .or(contact.customFieldsAndListsValues.any().customValue.containsIgnoreCase(searchStr));
    }

    private void applyUnassignedFilter(QContact contact, BooleanBuilder builder) {
        Optional.ofNullable(showUnassigned).ifPresent(flag -> {
            BooleanExpression unassignedCondition = flag ?
                    contact.company.name.equalsIgnoreCase(UNASSIGNED_COMPANY) :
                    contact.company.name.toLowerCase().ne(UNASSIGNED_COMPANY);

            builder.and(unassignedCondition);
        });
    }

    private void applyDeletionFilter(QContact contact, BooleanBuilder builder) {
        Optional.ofNullable(isDeleted)
                .ifPresent(flag -> builder.and(contact.isDeleted.eq(flag)));
    }
}