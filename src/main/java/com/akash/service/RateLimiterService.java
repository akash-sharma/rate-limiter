package com.akash.service;

import com.akash.dto.RLConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RateLimiterService {

  private static final char[] HEX_CHARS =
      new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  private static final Charset UTF8_CHARSET = Charset.forName("UTF8");

  private JedisCluster jedisClient;

  @Value("${localhost:6379}")
  private String redisHosts;

  private ObjectMapper MAPPER = new ObjectMapper();

  private String scriptSha1;

  @PostConstruct
  public void init() {

    // create jedis client
    Set<HostAndPort> jedisNodes = new HashSet();
    String[] redisHostSplit = redisHosts.split(",");
    jedisNodes.add(new HostAndPort(redisHostSplit[0], Integer.parseInt(redisHostSplit[1])));
    jedisClient = new JedisCluster(jedisNodes, new GenericObjectPoolConfig());

    // load lua script
    InputStream inputStream =
        this.getClass().getClassLoader().getResourceAsStream("RlValidator.lua");
    if (inputStream != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      String script = (String) reader.lines().collect(Collectors.joining(System.lineSeparator()));
      scriptSha1 = sha1DigestAsHex(script);
    } else {
      throw new RuntimeException("Unable to load lua script");
    }
  }

  public boolean isAllowed(RLConfig rlConfig) throws JsonProcessingException {

    if (rlConfig != null) {
      String configJson = MAPPER.writeValueAsString(rlConfig);
      String shaKey = rlConfig.getClientId();
      Long count =
          (Long)
              jedisClient.evalsha(
                  scriptSha1,
                  Collections.singletonList(shaKey),
                  Collections.singletonList(configJson));
      if (count == 1) {
        return true;
      }
      return false;
    }
    return true;
  }

  private String sha1DigestAsHex(String data) {
    byte[] dataBytes = getDigest("SHA").digest(data.getBytes(UTF8_CHARSET));
    return new String(encodeHex(dataBytes));
  }

  private MessageDigest getDigest(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("error while getting digest " + algorithm, e);
    }
  }

  private char[] encodeHex(byte[] data) {
    int l = data.length;
    char[] out = new char[l << 1];
    int i = 0;

    for (int var4 = 0; i < l; ++i) {
      out[var4++] = HEX_CHARS[(240 & data[i]) >>> 4];
      out[var4++] = HEX_CHARS[15 & data[i]];
    }

    return out;
  }
}
