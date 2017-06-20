package org.salve.drools.model.template.db;

import com.google.common.collect.Maps;
import org.salve.drools.model.db.Database;
import org.salve.drools.model.template.TemplateAttribute;
import org.salve.drools.model.template.TemplateValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CapturedMatchDB extends Database<TemplateAttribute, TemplateValue> {
	public static final CapturedMatchDB empty = new CapturedMatchDB(Maps.newHashMap());
	public CapturedMatchDB(Map<TemplateAttribute, TemplateValue> data) {
		super(data);
	}

	public static CapturedMatchDB create(Pattern pattern, MatchResult matcher){
		Map<String, Integer> groupmap = extractGroupNameToGroupID(pattern);
		if(groupmap.isEmpty()){
			return empty;
		}
		Map<TemplateAttribute, TemplateValue> result = groupmap
				.entrySet()
				.stream()
				.collect(
					Collectors.toMap(
						key -> new TemplateAttribute(key.getKey()),
						value -> new TemplateValue(matcher.group(value.getValue()))
					)
				);
		if(result.isEmpty()){
			return empty;
		}
		return new CapturedMatchDB(result);
	}

	/**
	 * Official api doesn't support this but we need this, see:
	 * https://stackoverflow.com/questions/15588903/get-group-names-in-java-regex
	 * @param regex
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Integer> extractGroupNameToGroupID(Pattern regex) {

		Method namedGroupsMethod = null;
		try {
			namedGroupsMethod = Pattern.class.getDeclaredMethod("namedGroups");
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("ups", e);
		}
		namedGroupsMethod.setAccessible(true);

		Map<String, Integer> namedGroups = null;
		try {
			namedGroups = (Map<String, Integer>) namedGroupsMethod.invoke(regex);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("ups", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("invocation", e);
		}

		if (namedGroups == null) {
			throw new NullPointerException("no named groups after all");
		}
		return Collections.unmodifiableMap(namedGroups);
	}
}
