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
--
-- 返回值常量：
local RES_SUCCESS = 0          -- 秒杀成功
local RES_STOCK_NOT_ENOUGH = 1 -- 库存不足
local RES_REPEAT_BUY = 2       -- 重复秒杀
local RES_NOT_START = 3        -- 活动未开始
local RES_HAS_END = 4           -- 活动已结束
local RES_ERROR = 5             -- 未知错误


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
    return RES_NOT_START
end
if currentTime > endTime then
    return RES_HAS_END
end

-- 活动状态校验（降级兜底）
if activityStatus ~= 1 then
    if activityStatus == 0 then
        -- 状态0：活动未开始
        return RES_NOT_START
    elseif activityStatus == 2 then
        -- 状态2：活动已结束
        return RES_HAS_END
    else
        -- 其他异常状态（如nil/-1/3）
        return RES_ERROR
    end
end

-- 检查用户是否已经购买过（一人一单）
if redis.call('sismember', userKey, userId) == 1 then
    return RES_REPEAT_BUY
end

-- 检查库存
local stock = tonumber(redis.call('get', stockKey))
if not stock or stock <= 0 then
    return RES_STOCK_NOT_ENOUGH
end

-- 扣减库存
redis.call('decr', stockKey)

-- 记录用户购买信息（防止重复购买）
redis.call('sadd', userKey, userId)

return RES_SUCCESS
