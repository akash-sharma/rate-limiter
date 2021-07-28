package com.akash.service;

import com.akash.dto.RLConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RateLimiterService {

  private static final Logger LOGGER = LogManager.getLogger(RateLimiterService.class);

  @Value("${redisHosts:localhost:6379}")
  private String redisHosts;

  private JedisCluster jedisClient;
  private ObjectMapper MAPPER = new ObjectMapper();
  private String luaScript;

  @PostConstruct
  public void init() {

    // create jedis client
    Set<HostAndPort> jedisNodes = new HashSet();
    String[] redisHostSplit = redisHosts.split(",");
    for (String redisHostPort : redisHostSplit) {
      String hostAndPort[] = redisHostPort.split(":");
      jedisNodes.add(new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
    }
    jedisClient = new JedisCluster(jedisNodes, new GenericObjectPoolConfig());
    LOGGER.info("jedisClient : {}", jedisClient);

    // load lua script
    InputStream inputStream =
        this.getClass().getClassLoader().getResourceAsStream("RlValidator.lua");
    if (inputStream != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      luaScript = (String) reader.lines().collect(Collectors.joining(System.lineSeparator()));
      LOGGER.info("luaScript : {}", luaScript);
    } else {
      throw new RuntimeException("Unable to load lua script");
    }
  }

  // when key for lua script is "{key}mykey", then key is used
  // for finding single node in a cluster by using its hash
  // https://stackoverflow.com/questions/49622787/lua-script-attempted-to-access-a-non-local-key-in-a-cluster-node
  public boolean isAllowed(RLConfig rlConfig) throws JsonProcessingException {

    if (rlConfig != null) {
      String configJson = MAPPER.writeValueAsString(rlConfig);
      String shaKey = "{" + rlConfig.getClientId() + "}";
      LOGGER.info("shaKey : {} , configJson : {}", shaKey, configJson);
      Long count =
          (Long)
              jedisClient.eval(
                  luaScript,
                  Collections.singletonList(shaKey),
                  Collections.singletonList(configJson));
      LOGGER.info("count : {}", count);
      if (count == 1) {
        return true;
      }
      return false;
    }
    return true;
  }
}
