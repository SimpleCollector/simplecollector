package io.github.simplecollector.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoTypeMapper;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "io.github.simplecollector.data")
class MongoConfiguration {
	
	 @Autowired
	  private MongoDbFactory mongoFactory;

	  @Autowired
	  private MongoMappingContext mongoMappingContext;

	  @Bean
	  public MappingMongoConverter mongoConverter() throws Exception {
	    DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
	    MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
	    
	    // The following is to prevent the _class field to appear in documents
	    MongoTypeMapper typeMapper = new DefaultMongoTypeMapper(null);
	    mongoConverter.setTypeMapper(typeMapper);
	    
	    return mongoConverter;
	  }	
	
}