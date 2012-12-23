neoorm
======

CDI Extension using jo4neo to bring Neo4j to Java EE 6 Applications

This Portable Extension builds upon the jo4neo ( http://code.google.com/p/jo4neo/ ) project which provides a lightweight but powerful object graph mapper 
for the Neo4j Database. 

With this Extension you get automatic initialization and lifecycle management by the CDI container.

For example you can do something like this: 

@NeoEntityManager
private NeoORM orm;

@NeoEntityManager(neopath="/path/to/db/dir")
private NeoORM orm;

The NeoORM object provides a jo4neo instance for the underlying database instance. 
(It is possible to run multiple Neo4j instances with this Extension) 

The Extension can be configured in three ways: 
* by JVM Argument -Dneo.path
* by Classpath File neo.properties in src/META-INF
* by the neopath parameter in the @NeoEntityManager annotation
