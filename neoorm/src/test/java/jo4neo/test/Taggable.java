package jo4neo.test;

import java.util.Collection;

public interface Taggable {

	void setTags(Collection<Tag> tags);
	Collection<Tag> getTags();
}
