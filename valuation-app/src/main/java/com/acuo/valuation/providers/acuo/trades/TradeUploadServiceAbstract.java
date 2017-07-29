package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.cache.manager.CacheManager;
import com.acuo.common.cache.manager.Cacheable;
import com.acuo.common.cache.manager.CachedObject;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.TradingAccount;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.services.TradeUploadService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
abstract class TradeUploadServiceAbstract implements TradeUploadService {

    static final ReentrantLock lock = new ReentrantLock();

    private final TradingAccountService accountService;
    protected final PortfolioService portfolioService;
    private final CacheManager cacheManager;
    private final int expireTime = 1;
    private final TimeUnit expireUnit = TimeUnit.MINUTES;

    TradeUploadServiceAbstract(TradingAccountService accountService,
                               PortfolioService portfolioService) {
        this.accountService = accountService;
        this.portfolioService = portfolioService;
        this.cacheManager = new CacheManager();
    }

    void linkPortfolio(Trade trade, String portfolioId) {
        if(log.isDebugEnabled()) {
            log.debug("linking to portfolioId: {}", portfolioId);
        }
        Portfolio portfolio = portfolio(PortfolioId.fromString(portfolioId));
        trade.setPortfolio(portfolio);
    }

    void linkAccount(Trade trade, String accountId) {
        if(log.isDebugEnabled()) {
            log.debug("linking to accountId: {}", accountId);
        }
        TradingAccount account = account(accountId);
        trade.setAccount(account);
    }

    private Portfolio portfolio(PortfolioId portfolioId) {
        Cacheable value = cacheManager.getCache(portfolioId);
        if (value == null) {
            lock.lock();
            try {
                Portfolio portfolio = portfolioService.find(portfolioId);
                value = new CachedObject(portfolio, portfolioId, expireTime, expireUnit);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                value = new CachedObject(null, portfolioId, expireTime, expireUnit);
            } finally {
                lock.unlock();
            }
            cacheManager.putCache(value);
        }
        return (Portfolio) value.getObject();
    }

    private TradingAccount account(String accountId) {
        Cacheable value = cacheManager.getCache(accountId);
        if (value == null) {
            lock.lock();
            try {
                TradingAccount account = accountService.find(accountId);
                value = new CachedObject(account, accountId, expireTime, expireUnit);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                value = new CachedObject(null, accountId, expireTime, expireUnit);
            } finally {
                lock.unlock();
            }
            cacheManager.putCache(value);
        }
        return (TradingAccount) value.getObject();
    }

}
