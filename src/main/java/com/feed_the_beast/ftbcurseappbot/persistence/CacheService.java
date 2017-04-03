package com.feed_the_beast.ftbcurseappbot.persistence;

import com.beust.jcommander.internal.Maps;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.data.MongoCommand;
import com.feed_the_beast.javacurselib.addondumps.AddonDatabase;
import com.feed_the_beast.javacurselib.common.classes.GroupMemberContract;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.service.contacts.contacts.GroupNotification;
import com.feed_the_beast.javacurselib.service.groups.groups.GroupMemberSearchRequest;
import com.feed_the_beast.javacurselib.service.groups.servers.GroupRoleDetails;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
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
    private LoadingCache<CurseGUID, List<GroupMemberContract>> groupmembers;
    @Getter
    private Map<CurseGUID, List<MongoCommand>> customServerCommands;
    @Getter
    private Supplier<ContactsResponse> contacts = Suppliers.memoizeWithExpiration(this::getContactsFromURL,
            5, TimeUnit.MINUTES);
    @Getter
    @Setter
    @Nullable
    private AddonDatabase addonDatabase;

    public CacheService () {
        customServerCommands = Maps.newHashMap();
        grouproledetails = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(serverId -> {
                    List<GroupRoleDetails> server = null;
                    try {
                        server = Main.getRestUserEndpoints().servers.getServerRoles(serverId).get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("error getting server roles from curse for server: " + serverId, e);
                    }
                    return server;
                });
        groupnotifications = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(serverId -> {
                    GroupNotification server = null;
                    try {
                        server = Main.getRestUserEndpoints().groups.get(serverId, false).get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("error getting groupNotification from curse for server: " + serverId, e);
                    }
                    return server;
                });
        groupmembers = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(serverId -> {
                    try {
                        int page = 1;
                        GroupMemberSearchRequest request = new GroupMemberSearchRequest(page);

                        List<GroupMemberContract> members = Main.getRestUserEndpoints().groups.searchMembers(serverId, request).get();
                        List<GroupMemberContract> allMembers = Lists.newArrayList();
                        do {
                            allMembers.addAll(members);
                            page = page + 1;
                            request.page = page;
                            members = Main.getRestUserEndpoints().groups.searchMembers(serverId, request).get();
                        } while (members.size() > 0);

                        return allMembers;

                    } catch (InterruptedException | ExecutionException e) {
                        log.error("error getting server roles from curse for server: " + serverId, e);
                    }
                    return null;
                });
    }

    @Nonnull
    public Optional<List<MongoCommand>> getCustomCommandsForServer (@Nonnull CurseGUID serverId) {
        return Optional.ofNullable(customServerCommands.get(serverId));
    }

    public void setServerCommandsEntry (@Nonnull CurseGUID serverId, List<MongoCommand> commands) {
        customServerCommands.put(serverId, commands);
    }

    @Nonnull
    public Optional<List<GroupRoleDetails>> getServerRole (@Nonnull CurseGUID serverId) {
        return Optional.of(grouproledetails.get(serverId));
    }

    @Nonnull
    public Optional<List<GroupMemberContract>> getServerMembers (@Nonnull CurseGUID serverId) {
        return Optional.of(groupmembers.get(serverId));
    }

    @Nonnull
    public Optional<GroupNotification> getGroupNotification(@Nonnull CurseGUID serverID) {
        return Optional.of(groupnotifications.get(serverID));
    }
    @Nonnull//TODO clean up filter statements
    public Optional<GroupMemberContract> getServerMember (@Nonnull CurseGUID serverId, long id, boolean canTryCacheClear) {
        Optional<List<GroupMemberContract>> members = getServerMembers(serverId);
        boolean hasCleared = false;
        if (!members.isPresent() || members.get().size() == 0) {
            if (canTryCacheClear) {
                groupmembers.refresh(serverId);
                hasCleared = true;
            } else {
                return Optional.empty();
            }
        } else {
            members = getServerMembers(serverId);
            Optional<GroupMemberContract> gc = Optional.empty();
            if (members.isPresent()) {
                gc = members.get().stream().filter(g -> g != null && g.userID == id).findFirst();
            }
            if (!gc.isPresent()) {
                if (canTryCacheClear) {
                    groupmembers.refresh(serverId);
                    members = getServerMembers(serverId);
                    hasCleared = true;
                } else {
                    return Optional.empty();
                }
            } else {
                return gc;
            }
        }
        if (!members.isPresent() || members.get().size() == 0) {
            return Optional.empty();
        } else if (hasCleared) {
            members = getServerMembers(serverId);
            Optional<GroupMemberContract> gc = Optional.empty();
            if (members.isPresent()) {
                gc = members.get().stream().filter(g -> g != null && g.userID == id).findFirst();
            }
            return gc;
        }
        return Optional.empty();
    }

    @Nonnull//TODO clean up filter statements
    public Optional<GroupMemberContract> getServerMember (@Nonnull CurseGUID serverId, @Nonnull String name, boolean canTryCacheClear) {
        Optional<List<GroupMemberContract>> members = getServerMembers(serverId);
        boolean hasCleared = false;
        if (!members.isPresent() || members.get().size() == 0) {
            if (canTryCacheClear) {
                groupmembers.refresh(serverId);
                hasCleared = true;
            } else {
                return Optional.empty();
            }
        } else {
            members = getServerMembers(serverId);
            Optional<GroupMemberContract> gc = Optional.empty();
            if (members.isPresent()) {
                gc = members.get().stream().filter(g -> g != null && ((g.nickName != null && g.nickName.equalsIgnoreCase(name)) || (g.username != null && g.username.equalsIgnoreCase(name))))
                        .findFirst();
            }
            if (!gc.isPresent()) {
                if (canTryCacheClear) {
                    groupmembers.refresh(serverId);
                    members = getServerMembers(serverId);
                    hasCleared = true;
                } else {
                    return Optional.empty();
                }
            } else {
                return gc;
            }
        }
        if (!members.isPresent() || members.get().size() == 0) {
            return Optional.empty();
        } else if (hasCleared) {
            members = getServerMembers(serverId);
            Optional<GroupMemberContract> gc = Optional.empty();
            if (members.isPresent()) {
                gc = members.get().stream().filter(g -> g != null && ((g.nickName != null && g.nickName.equalsIgnoreCase(name)) || (g.username != null && g.username.equalsIgnoreCase(name))))
                        .findFirst();
            }
            return gc;
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
