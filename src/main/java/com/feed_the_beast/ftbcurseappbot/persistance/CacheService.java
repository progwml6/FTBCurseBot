package com.feed_the_beast.ftbcurseappbot.persistance;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.service.contacts.contacts.GroupNotification;
import com.feed_the_beast.javacurselib.service.groups.servers.GroupRoleDetails;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * basic service for caching data.
 */
@Slf4j
public class CacheService {
    private LoadingCache<CurseGUID, List<GroupRoleDetails>> grouproledetails;
    private LoadingCache<CurseGUID, GroupNotification> groupnotifications;
    @Getter
    private Supplier<ContactsResponse> contacts = Suppliers.memoizeWithExpiration(this::getContactsFromURL,
            5, TimeUnit.MINUTES);

    public CacheService () {
        grouproledetails = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<CurseGUID, List<GroupRoleDetails>>() { // build the cacheloader

                    @Override
                    public List<GroupRoleDetails> load (CurseGUID serverId) throws Exception {
                        List<GroupRoleDetails> server = null;
                        try {
                            server = Main.getRestUserEndpoints().servers.getServerRoles(serverId).get();
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
        groupnotifications = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<CurseGUID, GroupNotification>() { // build the cacheloader

                    @Override
                    public GroupNotification load (CurseGUID serverId) throws Exception {
                        GroupNotification server = null;
                        try {
                            server = Main.getRestUserEndpoints().groups.get(serverId, false).get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("error getting groupNotification from curse for server: " + serverId, e);
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

    @Nullable
    private ContactsResponse getContactsFromURL () {
        try {
            CompletableFuture<ContactsResponse> cr = Main.getRestUserEndpoints().contacts.get();
            return cr.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("error getting contacts response", e);
            return null;
        }
    }
}
