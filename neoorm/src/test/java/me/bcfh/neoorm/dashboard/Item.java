package me.bcfh.neoorm.dashboard;

import jo4neo.Nodeid;
import jo4neo.neo;

public class Item extends NeoEntity {

	@neo(index = true)
	private String widgetId;

	@neo
	private String name;

	@neo
	private String editor;

	@neo
	private String text;

	@neo
	private Float complexity;

	public Item(Item other) {
		this.widgetId = other.widgetId;
		this.name = other.name;
		this.editor = other.editor;
		this.text = other.text;
		this.complexity = other.complexity;
	}

	public Item() {
		// TODO Auto-generated constructor stub
	}

	public Item(String widgetId, String name) {
		super();
		this.widgetId = widgetId;
		this.name = name;
	}

	public Float getComplexity() {
		return complexity;
	}

	public void setComplexity(Float complexity) {
		this.complexity = complexity;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getWidgetId() {
		if (widgetId == null) {
			return name;
		}
		return widgetId;
	}

	public Item withWidgetId(String widgetId) {
		this.widgetId = widgetId;
		return this;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public String getName() {
		return name;
	}

	public Item withName(String name) {
		this.name = name;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEditor() {
		return editor;
	}

	public Item withEditor(String editor) {
		this.editor = editor;
		return this;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	@Override
	public String toString() {
		return "Item [widgetId=" + widgetId + ", name=" + name + ", editor="
				+ editor + ", text=" + text + ", complexity=" + complexity
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((widgetId == null) ? 0 : widgetId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (widgetId == null) {
			if (other.widgetId != null)
				return false;
		} else if (!widgetId.equals(other.widgetId))
			return false;
		return true;
	}

}
