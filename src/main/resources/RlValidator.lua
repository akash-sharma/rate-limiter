local function rlAllowed(rlConf)

    local rlThresholds = rlConf.rlThresholds
    local passRateLimiter = 1

    for i = 1, table.getn(rlThresholds) do

        -- Removes all elements in the sorted set stored at key with a score , O(log N)
        redis.call('ZREMRANGEBYSCORE', rlThresholds[i].key, '-inf', rlThresholds[i].lastWindowTime)
        
        -- Returns number of elements in a sorted set, O(1)
        local countOfElements = redis.call('ZCARD', rlThresholds[i].key)

        local rateLimiterResult = countOfElements < tonumber(rlThresholds[i].threshold)
        if (rateLimiterResult == false) then
            -- add key to redis, even if false
            passRateLimiter = 0
        end

        -- add new element in sorted set, O(log N)
        redis.call('ZADD', rlThresholds[i].key, rlThresholds[i].epoch, rlThresholds[i].epoch)
    end

    return passRateLimiter
end

local rlConf = ARGV[1]
return rlAllowed(rlConf)
