package me.bcfh.neoorm;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import me.bcfh.neoorm.OrmProvider;


@Startup
@Singleton
public class OrmShutdownService {

	@PreDestroy
	private void shutdown() {
		OrmProvider.shutdown();
	}

}
