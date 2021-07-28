package com.akash.filter;

import com.akash.constant.Route;
import com.akash.dto.RLConfig;
import com.akash.dto.RLThreshold;
import com.akash.service.RateLimiterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LogManager.getLogger(RateLimiterFilter.class);

  @Autowired private RateLimiterService rateLimiterService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestUri = request.getRequestURI();
    LOGGER.info("requestUri : {}", requestUri);
    RLConfig rlConfig = null;
    if (Route.V1_PRODUCT.equalsIgnoreCase(requestUri)) {
      rlConfig = getV1ProductRlConfig();
    } else if (Route.V2_PRODUCT.equalsIgnoreCase(requestUri)) {
      rlConfig = getV2ProductRlConfig();
    }

    boolean allowed = rateLimiterService.isAllowed(rlConfig);
    LOGGER.info("allowed : {}", allowed);
    if (!allowed) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      return;
    }
    filterChain.doFilter(request, response);
  }

  // TODO : values can be fetched from cache or stored in memory
  private RLConfig getV1ProductRlConfig() {
    RLThreshold threshold1 = new RLThreshold(2, 500);
    RLThreshold threshold2 = new RLThreshold(5, 1000);
    List<RLThreshold> rlThresholds = new ArrayList<>();
    rlThresholds.add(threshold1);
    rlThresholds.add(threshold2);
    return new RLConfig(Route.V1_PRODUCT, rlThresholds);
  }

  private RLConfig getV2ProductRlConfig() {
    RLThreshold threshold1 = new RLThreshold(3, 400);
    RLThreshold threshold2 = new RLThreshold(10, 1100);
    List<RLThreshold> rlThresholds = new ArrayList<>();
    rlThresholds.add(threshold1);
    rlThresholds.add(threshold2);
    return new RLConfig(Route.V2_PRODUCT, rlThresholds);
  }
}
