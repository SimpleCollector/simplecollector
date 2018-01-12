package io.github.simplecollector.data;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SnmpConfigGroupRepository extends MongoRepository<SnmpConfigGroup, String> {
	
	public SnmpConfigGroup findByName(String Name);
	
	@Query(value="{}", fields="{ '_id' : 1 }")
	List<SnmpConfigGroup> findAllIds();

}
