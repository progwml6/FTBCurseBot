package com.feed_the_beast.ftbcurseappbot.persistance;

import com.feed_the_beast.javacurselib.rest.REST;
import com.feed_the_beast.javacurselib.service.groups.servers.GroupRoleDetails;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Created by progwml6 on 6/10/16.
 */
@Slf4j
public class CacheService {
    private LoadingCache<CurseGUID, List<GroupRoleDetails>> grouproledetails;

    public CacheService () {
        grouproledetails = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<CurseGUID, List<GroupRoleDetails>>() { // build the cacheloader

                    @Override
                    public List<GroupRoleDetails> load (CurseGUID serverId) throws Exception {
                        List<GroupRoleDetails> server = null;
                        try {
                            server = REST.servers.getServerRoles(serverId).get();
                            String lg = "";
                            for (GroupRoleDetails g : server) {
                                lg += g.toString();
                            }
                            log.info(lg);
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("error getting server roles from curse for server: " + serverId, e);
                        }
                        return server;
                    }
                });

    }

    @Nonnull
    public Optional<List<GroupRoleDetails>> getServerRole (@Nonnull CurseGUID serverId) {
        try {
            return Optional.of(grouproledetails.get(serverId));
        } catch (ExecutionException e) {
            log.error("error getting details from cache", e);
        }
        return Optional.empty();
    }
}
