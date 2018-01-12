package io.github.simplecollector.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import io.github.simplecollector.data.api.PollGroupSummary;

public interface PollGroupRepository extends MongoRepository<PollGroup, String> {
	
	public PollGroup findByName(String Name);
	
	Page<PollGroupSummary> findPagedProjectedBy(Pageable pageable);

}
