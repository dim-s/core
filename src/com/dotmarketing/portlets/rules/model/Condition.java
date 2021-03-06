package com.dotmarketing.portlets.rules.model;

import com.dotcms.repackage.com.google.common.collect.Maps;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.rules.RuleComponentInstance;
import com.dotmarketing.portlets.rules.RuleComponentModel;
import com.dotmarketing.portlets.rules.conditionlet.Conditionlet;
import com.dotmarketing.util.Logger;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Condition implements RuleComponentModel, Serializable {

    private static final long serialVersionUID = 1L;
    private RuleComponentInstance instance;

    public enum Operator {
        AND,
        OR;

        @Override
        public String toString() {
            return super.name();
        }
    }

    private String id;
    private String name;
    private String conditionletId;
    private String conditionGroup;
    private List<ParameterModel> values;
    private Date modDate;
    private Operator operator;
    private int priority;
    private Conditionlet conditionlet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConditionletId() {
        return conditionletId;
    }

    public void setConditionletId(String conditionletId) {
        this.conditionletId = conditionletId;
    }

    public String getConditionGroup() {
        return conditionGroup;
    }

    public void setConditionGroup(String conditionGroup) {
        this.conditionGroup = conditionGroup;
    }

    public List<ParameterModel> getValues() {
        return values;
    }

    public Map<String, ParameterModel> getParameters() {
        Map<String, ParameterModel> p = Maps.newLinkedHashMap();
        for (ParameterModel value : values) {
            p.put(value.getKey(), value);
        }
        return p;
    }

    public void addParameter(String key, String value){
        getParameters().put(key, new ParameterModel(key, value));
    }

    public void addParameter(ParameterModel value) {
        getParameters().put(value.getKey(), value);
    }

    public void setValues(List<ParameterModel> values) {
        this.values = values;
    }

    public Date getModDate() {
        return modDate;
    }

    public void setModDate(Date modDate) {
        this.modDate = modDate;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void checkValid() {
        this.instance = getConditionlet().doCheckValid(this);
    }

    public Conditionlet getConditionlet() {
        if(conditionlet==null) {
            try {
                conditionlet = APILocator.getRulesAPI().findConditionlet(conditionletId);
            } catch (DotDataException | DotSecurityException e) {
                Logger.error(this, "Unable to load conditionlet for condition with id: " + id);
            }
        }

        return conditionlet;
    }

    public final boolean evaluate(HttpServletRequest req, HttpServletResponse res) {
        //noinspection unchecked
        return getConditionlet().doEvaluate(req, res, instance);
    }


	@Override
	public String toString() {
		return "Condition [id=" + id + ", name=" + name
                + ", conditionletId=" + conditionletId + ", conditionGroup="
				+ conditionGroup + ", values="
				+ values + ", modDate=" + modDate + ", operator=" + operator
				+ ", priority=" + priority + "]";
	}

}
