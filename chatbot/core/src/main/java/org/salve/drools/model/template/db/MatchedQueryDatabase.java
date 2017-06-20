package org.salve.drools.model.template.db;

import org.salve.drools.model.db.Database;
import org.salve.drools.model.template.TemplateAttribute;
import org.salve.drools.model.template.TemplateValue;

import java.util.Map;

/**
 * Filled in template attributes db
 */
public class MatchedQueryDatabase extends Database<TemplateAttribute, TemplateValue>{
	public MatchedQueryDatabase(Map<TemplateAttribute, TemplateValue> data) {
		super(data);
	}

}
