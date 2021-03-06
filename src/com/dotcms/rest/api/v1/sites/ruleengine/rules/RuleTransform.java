package com.dotcms.rest.api.v1.sites.ruleengine.rules;

import com.dotcms.rest.api.v1.sites.ruleengine.rules.conditions.ConditionGroupTransform;
import com.dotcms.rest.api.v1.sites.ruleengine.rules.conditions.RestConditionGroup;
import com.dotcms.rest.exception.BadRequestException;
import com.dotmarketing.business.ApiProvider;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.rules.business.RulesAPI;
import com.dotmarketing.portlets.rules.model.ConditionGroup;
import com.dotmarketing.portlets.rules.model.Rule;
import com.dotmarketing.portlets.rules.model.RuleAction;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dotcms.rest.validation.Preconditions.checkNotNull;

/**
 * @author Geoff M. Granum
 */
public class RuleTransform {
    private final RulesAPI rulesAPI;
    private ConditionGroupTransform groupTransform;

    public RuleTransform() { this(new ApiProvider()); }

    public RuleTransform(ApiProvider apiProvider) {
        this.rulesAPI = apiProvider.rulesAPI();
        groupTransform = new ConditionGroupTransform(apiProvider);
    }

    public Rule restToApp(RestRule rest, User user) {
        Rule app = new Rule();
        return applyRestToApp(rest, app, user);
    }

    public Rule applyRestToApp(RestRule rest, Rule app, User user) {
        app.setName(rest.name);
        app.setFireOn(Rule.FireOn.valueOf(rest.fireOn));
        app.setPriority(rest.priority);
        app.setShortCircuit(rest.shortCircuit);
        app.setEnabled(rest.enabled);
        List<ConditionGroup> groups = checkGroupsExist(rest, app, user);

        app.setGroups(groups);
        return app;
    }

    private List<ConditionGroup> checkGroupsExist(RestRule rest, Rule app, User user) {
        List<ConditionGroup> groups = new ArrayList<>();

        for (Map.Entry<String, RestConditionGroup> group : rest.conditionGroups.entrySet()) {
            try {
                ConditionGroup existingGroup = checkNotNull(rulesAPI.getConditionGroupById(group.getKey(), user, false),
                        BadRequestException.class, "Group with key '%s' not found", group);

                if(!existingGroup.getRuleId().equals(app.getId()))
                    throw new BadRequestException("Group with key '%s' does not belong to Rule", existingGroup.getId());

                groups.add(existingGroup);
            } catch (DotDataException | DotSecurityException e) {
                Logger.error(this, "Error applying RestRule to Rule", e);
                throw new BadRequestException(e, e.getMessage());
            }
        }
        return groups;
    }

    public RestRule appToRest(Rule app) {
        return toRest.apply(app);
    }

    public Function<Rule, RestRule> appToRestFn() {
        return toRest;
    }

    private final Function<Rule, RestRule> toRest = (app) -> {
        Map<String, RestConditionGroup> groups = app.getGroups()
                                                    .stream()
                                                    .collect(Collectors.toMap(ConditionGroup::getId, groupTransform.appToRestFn()));

        Map<String, Boolean> ruleActions = app.getRuleActions()
                                                         .stream()
                                                         .collect(Collectors.toMap(RuleAction::getId, ruleAction -> true));

        return new RestRule.Builder()
                .key(app.getId())
                .name(app.getName())
                .fireOn(app.getFireOn().toString())
                .shortCircuit(app.isShortCircuit())
                .priority(app.getPriority())
                .enabled(app.isEnabled())
                .conditionGroups(groups)
                .ruleActions(ruleActions)
                .build();
    };

}

