package io.github.simplecollector.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.simplecollector.data.HostGroup;
import io.github.simplecollector.data.HostGroupRepository;
import io.github.simplecollector.data.PollGroup;
import io.github.simplecollector.data.PollGroupRepository;
import io.github.simplecollector.data.SnmpConfigGroup;
import io.github.simplecollector.data.SnmpConfigGroupRepository;
import io.github.simplecollector.data.api.PollGroupSummary;
import io.github.simplecollector.engine.PollQueueManager;

@RestController
@RequestMapping("/api")
public class RestApiController {
	private Logger logger = LoggerFactory.getLogger(RestApiController.class);
	private static final String PAGE_SIZE = "25";
	
	@Autowired
	private PollQueueManager queue;
	@Autowired
	private PollGroupRepository pgRepository;
	@Autowired
	private HostGroupRepository hgRepository;
	@Autowired
	private SnmpConfigGroupRepository cfgRepository;

	
	// -------------------------------
	//  Administrative operations
	// -------------------------------

	@GetMapping("/admin/reload-config")
    public Map<String, Object> reloadConfig(@RequestParam(value="group", defaultValue="all") String group) {
		Map<String, Object> result = new HashMap<>();
		queue.reloadQueue();
		result.put("size", queue.size());
		result.put("items", queue.listItemIds());
		
        return result;
    }	

	
	// -------------------------------
	//  List operations
	// -------------------------------

	@GetMapping("/config/pollgroup-summary")
    public Map<String, Object> listPollgroups(@RequestParam(required=false, defaultValue="1") int page,
    		                          @RequestParam(required=false, defaultValue=PAGE_SIZE) int size) {
		page -= 1;
		Pageable pageableRequest = new PageRequest(page, size);
		Page<PollGroupSummary> groups = pgRepository.findPagedProjectedBy(pageableRequest);
		
		Map<String, Object> result = new LinkedHashMap<String, Object>(3);
		result.put("total", groups.getTotalElements());
		result.put("pgsize", size);
		result.put("items", groups.getContent());
		return result;
    }

	@GetMapping("/status/queue")
    public List<String> listQueueItems() {
        return queue.listItemIds();
    }

	@GetMapping("/config/hostgroups")
	public List<String> listHostgroups() {
		List<HostGroup> hgl = hgRepository.findAllIds();
		List<String> res = new ArrayList<String>();
		for (HostGroup hg : hgl) {
			res.add(hg.getName());
		}
		return res;
	}
	
	@GetMapping("/config/snmpconfigs")
	public List<String> listSnmpconfigs() {
		List<SnmpConfigGroup> cfgl = cfgRepository.findAllIds();
		List<String> res = new ArrayList<String>();
		for (SnmpConfigGroup cfg : cfgl) {
			res.add(cfg.getName());
		}
		return res;
	}

	
	// -------------------------------
	//  PollGroup CRUD operations
	// -------------------------------
	
	@PostMapping("/config/pollgroup")
    public PollGroup createPollgroup(@RequestBody PollGroup pg) {
		try {
			pg.cleanUp();
			return this.pgRepository.save(pg);
		} catch (DuplicateKeyException e) {
			throw new ApiClientException("Name " + pg.getName() + " aleady exists.");
		} catch (Exception e) {
			logger.error("Error creating poll group:", e);
			throw new ApiServerException(e);
		}
    }

	@GetMapping("/config/pollgroup/{id}")
    public PollGroup getPollgroupById(@PathVariable("id") String id) {
		return pgRepository.findOne(id);
    }
	
	@PutMapping("/config/pollgroup/{id}")
	public PollGroup updatePollgroupById(@PathVariable("id") String id, @RequestBody PollGroup pg) {
		PollGroup result = null;
		if (!pg.getId().equals(id))
			throw new ApiClientException("Id in url not equal to id in body");
		try {
			pg.cleanUp();
			result = this.pgRepository.save(pg);
		} catch (Exception e) {
			logger.error("Error updating poll group " + id + ":", e);
			throw new ApiServerException(e.getMessage(), e);
		}
		return result;
    }
		
	@DeleteMapping("/config/pollgroup/{id}")
    public Boolean deletePollgroupById(@PathVariable("id") String id) {
		try {
			pgRepository.delete(id);
		} catch (Exception e) {
			return false;
		}
		return true;
    }

	
	// -------------------------------
	//  HostGroup CRUD operations
	// -------------------------------

	@PostMapping("/config/hostgroup")
    public HostGroup createHostgroup(@RequestBody HostGroup hg) {
		try {
			hg.cleanUp();
			return this.hgRepository.save(hg);
		} catch (DuplicateKeyException e) {
			throw new ApiClientException("Name " + hg.getName() + " aleady exists.");
		} catch (Exception e) {
			logger.error("Error creating host group:", e);
			throw new ApiServerException(e);
		}
    }

	@GetMapping("/config/hostgroup/{id}")
    public HostGroup getHostgroupById(@PathVariable("id") String id) {
		return hgRepository.findOne(id);
    }
	
	@PutMapping("/config/hostgroup/{id}")
	public HostGroup updateHostgroupById(@PathVariable("id") String id, @RequestBody HostGroup hg) {
		HostGroup result = null;
		if (!hg.getName().equals(id))
			throw new ApiClientException("Id in url not equal to id in body");
		try {
			hg.cleanUp();
			result = this.hgRepository.save(hg);
		} catch (Exception e) {
			logger.error("Error updating host group " + id + ":", e);
			throw new ApiServerException(e.getMessage(), e);
		}
		return result;
    }
		
	@DeleteMapping("/config/hostgroup/{id}")
    public Boolean deleteHostgroupById(@PathVariable("id") String id) {
		try {
			pgRepository.delete(id);
		} catch (Exception e) {
			return false;
		}
		return true;
    }

	
	// -------------------------------
	//  SnmpConfigGroup CRUD operations
	// -------------------------------

	@PostMapping("/config/snmpconfiggroup")
    public SnmpConfigGroup createSnmpconfiggroup(@RequestBody SnmpConfigGroup cfg) {
		try {
			cfg.cleanUp();
			return this.cfgRepository.save(cfg);
		} catch (DuplicateKeyException e) {
			throw new ApiClientException("Name " + cfg.getName() + " aleady exists.");
		} catch (Exception e) {
			logger.error("Error creating host group:", e);
			throw new ApiServerException(e);
		}
    }

	@GetMapping("/config/snmpconfiggroup/{id}")
    public SnmpConfigGroup getSnmpconfiggroupById(@PathVariable("id") String id) {
		return cfgRepository.findOne(id);
    }
	
	@PutMapping("/config/snmpconfiggroup/{id}")
	public SnmpConfigGroup updateSnmpconfiggroupById(@PathVariable("id") String id, @RequestBody SnmpConfigGroup cfg) {
		SnmpConfigGroup result = null;
		if (!cfg.getName().equals(id))
			throw new ApiClientException("Id in url not equal to id in body");
		try {
			cfg.cleanUp();
			result = this.cfgRepository.save(cfg);
		} catch (Exception e) {
			logger.error("Error updating host group " + id + ":", e);
			throw new ApiServerException(e.getMessage(), e);
		}
		return result;
    }

	@DeleteMapping("/config/snmpconfiggroup/{id}")
    public Boolean deleteSnmpconfiggroupById(@PathVariable("id") String id) {
		try {
			pgRepository.delete(id);
		} catch (Exception e) {
			return false;
		}
		return true;
    }

}
