-- 秒杀核心Lua脚本
-- 功能：原子性扣减库存 + 防重复秒杀（一人一单）
--
-- KEYS[1]: 库存Key，格式: seckill:stock:{activityId}
-- KEYS[2]: 用户购买记录Key，格式: seckill:user:{activityId}
-- ARGV[1]: 用户ID
-- ARGV[2]: 活动状态（0-未开始，1-进行中，2-已结束）
-- ARGV[3]: 当前时间戳
-- ARGV[4]: 活动开始时间戳
-- ARGV[5]: 活动结束时间戳
--
-- 返回值：
-- 0: 秒杀成功
-- 1: 库存不足
-- 2: 重复秒杀（用户已购买过）
-- 3: 活动未开始
-- 4: 活动已结束
-- 5: 未知错误

local stockKey = KEYS[1]
local userKey = KEYS[2]
local userId = ARGV[1]
local activityStatus = tonumber(ARGV[2])
local currentTime = tonumber(ARGV[3])
local startTime = tonumber(ARGV[4])
local endTime = tonumber(ARGV[5])

-- 优先用时间戳校验（比活动状态更可靠，避免状态同步延迟）
-- 原因：Java层的活动状态可能因缓存/数据库同步延迟，时间戳是绝对标准
if currentTime < startTime then
    return 3
end
if currentTime > endTime then
    return 4
end

-- 活动状态校验（降级兜底）
if activityStatus ~= 1 then
    if activityStatus == 0 then
        -- 状态0：活动未开始
        return 3
    elseif activityStatus == 2 then
        -- 状态2：活动已结束
        return 4
    else
        -- 其他异常状态（如nil/-1/3）
        return 5
    end
end

-- 检查用户是否已经购买过（一人一单）
if redis.call('sismember', userKey, userId) == 1 then
    return 2
end

-- 检查库存
local stock = tonumber(redis.call('get', stockKey))
if not stock or stock <= 0 then
    return 1
end

-- 扣减库存
redis.call('decr', stockKey)

-- 记录用户购买信息（防止重复购买）
redis.call('sadd', userKey, userId)

return 0
