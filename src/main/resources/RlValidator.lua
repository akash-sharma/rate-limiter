local function rlAllowed(rlConf)

    local rlThresholds = rlConf.rlThresholds
    local shouldAllow = 1

    for i = 1, table.getn(rlThresholds) do

        redis.call('ZREMRANGEBYSCORE', rlThresholds[i].key, '-inf', rlThresholds[i].lastWindowTime)
        local countOfElements = redis.call('ZCARD', rlThresholds[i].key)

        local result = countOfElements < tonumber(rlThresholds[i].threshold)
        if (result == false) then
            -- store the hit in redis, even if false
            shouldAllow = 0
        end

        redis.call('ZADD', rlThresholds[i].key, rlThresholds[i].epoch, rlThresholds[i].epoch)
    end

    return shouldAllow
end

local rlConf = cjson.decode(ARGV[1])
return rlAllowed(rlConf)