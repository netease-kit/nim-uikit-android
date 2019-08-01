package com.netease.nim.uikit.impl.cache;

import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.constant.MemberQueryType;
import com.netease.nimlib.sdk.chatroom.constant.MemberType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天室成员资料缓存
 * Created by huangjun on 2016/1/18.
 */
public class ChatRoomMemberCache {

    private static final String TAG = "ChatRoomMemberCache";

    public static ChatRoomMemberCache getInstance() {
        return InstanceHolder.instance;
    }

    private Map<String, Map<String, ChatRoomMember>> cache = new ConcurrentHashMap<>();

    private Map<String, List<SimpleCallback<ChatRoomMember>>> frequencyLimitCache = new ConcurrentHashMap<>(); // 重复请求处理

    public void clear() {
        cache.clear();
        frequencyLimitCache.clear();
    }

    public void clearRoomCache(String roomId) {
        if (cache.containsKey(roomId)) {
            cache.remove(roomId);
        }
    }

    public ChatRoomMember getChatRoomMember(String roomId, String account) {
        if (cache.containsKey(roomId)) {
            return cache.get(roomId).get(account);
        }

        return null;
    }

    public void saveMyMember(ChatRoomMember chatRoomMember) {
        saveMember(chatRoomMember);
    }

    /**
     * 从服务器获取聊天室成员资料（去重处理）（异步）
     */
    public void fetchMember(final String roomId, final String account, final SimpleCallback<ChatRoomMember> callback) {
        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(account)) {
            if (callback != null) {
                callback.onResult(false, null, -1);
            }
            return;
        }

        // 频率控制
        if (frequencyLimitCache.containsKey(account)) {
            if (callback != null) {
                frequencyLimitCache.get(account).add(callback);
            }
            return; // 已经在请求中，不要重复请求
        } else {
            List<SimpleCallback<ChatRoomMember>> cbs = new ArrayList<>();
            if (callback != null) {
                cbs.add(callback);
            }
            frequencyLimitCache.put(account, cbs);
        }

        // fetch
        List<String> accounts = new ArrayList<>(1);
        accounts.add(account);
        NIMClient.getService(ChatRoomService.class).fetchRoomMembersByIds(roomId, accounts).setCallback(new RequestCallbackWrapper<List<ChatRoomMember>>() {
            @Override
            public void onResult(int code, List<ChatRoomMember> members, Throwable exception) {
                ChatRoomMember member = null;
                boolean hasCallback = !frequencyLimitCache.get(account).isEmpty();
                boolean success = code == ResponseCode.RES_SUCCESS && members != null && !members.isEmpty();

                // cache
                if (success) {
                    saveMembers(members);
                    member = members.get(0);
                } else {
                    LogUtil.e(TAG, "fetch chat room member failed, code=" + code);
                }

                // callback
                if (hasCallback) {
                    List<SimpleCallback<ChatRoomMember>> cbs = frequencyLimitCache.get(account);
                    for (SimpleCallback<ChatRoomMember> cb : cbs) {
                        cb.onResult(success, member, code);
                    }
                }

                frequencyLimitCache.remove(account);
            }
        });
    }

    public void fetchRoomMembers(String roomId, MemberQueryType memberQueryType, long time, int limit,
                                 final SimpleCallback<List<ChatRoomMember>> callback) {
        if (TextUtils.isEmpty(roomId)) {
            if (callback != null) {
                callback.onResult(false, null, -1);
            }
            return;
        }

        NIMClient.getService(ChatRoomService.class).fetchRoomMembers(roomId, memberQueryType, time, limit).setCallback(new RequestCallbackWrapper<List<ChatRoomMember>>() {
            @Override
            public void onResult(int code, List<ChatRoomMember> result, Throwable exception) {
                boolean success = code == ResponseCode.RES_SUCCESS;

                if (success) {
                    saveMembers(result);
                } else {
                    LogUtil.e(TAG, "fetch members by page failed, code:" + code);
                }

                if (callback != null) {
                    callback.onResult(success, result, code);
                }
            }
        });
    }

    private void saveMember(ChatRoomMember member) {
        if (member != null && !TextUtils.isEmpty(member.getRoomId()) && !TextUtils.isEmpty(member.getAccount())) {
            Map<String, ChatRoomMember> members = cache.get(member.getRoomId());

            if (members == null) {
                members = new HashMap<>();
                cache.put(member.getRoomId(), members);
            }

            members.put(member.getAccount(), member);
        }
    }

    private void saveMembers(List<ChatRoomMember> members) {
        if (members == null || members.isEmpty()) {
            return;
        }

        for (ChatRoomMember m : members) {
            saveMember(m);
        }
    }

    /**
     * ************************************ 单例 ***************************************
     */
    static class InstanceHolder {
        final static ChatRoomMemberCache instance = new ChatRoomMemberCache();
    }

    /**
     * ********************************** 监听 ********************************
     */

    public void registerObservers(boolean register) {
        NIMClient.getService(ChatRoomServiceObserver.class).observeReceiveMessage(incomingChatRoomMsg, register);
    }

    private Observer<List<ChatRoomMessage>> incomingChatRoomMsg = new Observer<List<ChatRoomMessage>>() {
        @Override
        public void onEvent(List<ChatRoomMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }

            for (IMMessage msg : messages) {
                if (msg == null) {
                    LogUtil.e(TAG, "receive chat room message null");
                    continue;
                }

                if (msg.getMsgType() == MsgTypeEnum.notification) {
                    handleNotification(msg);
                }
            }
        }
    };

    private void handleNotification(IMMessage message) {
        if (message.getAttachment() == null) {
            return;
        }

        String roomId = message.getSessionId();
        ChatRoomNotificationAttachment attachment = (ChatRoomNotificationAttachment) message.getAttachment();
        List<String> targets = attachment.getTargets();
        if (targets != null) {
            for (String target : targets) {
                ChatRoomMember member = getChatRoomMember(roomId, target);
                handleMemberChanged(attachment.getType(), member);
            }
        }
    }

    private void handleMemberChanged(NotificationType type, ChatRoomMember member) {
        if (member == null) {
            return;
        }

        switch (type) {
            case ChatRoomMemberIn:
                NimUIKit.getChatRoomMemberChangedObservable().notifyMemberChange(member, true);
                break;
            case ChatRoomMemberExit:
                NimUIKit.getChatRoomMemberChangedObservable().notifyMemberChange(member, false);
                break;
            case ChatRoomManagerAdd:
                member.setMemberType(MemberType.ADMIN);
                break;
            case ChatRoomManagerRemove:
                member.setMemberType(MemberType.NORMAL);
                break;
            case ChatRoomMemberBlackAdd:
                member.setInBlackList(true);
                break;
            case ChatRoomMemberBlackRemove:
                member.setInBlackList(false);
                break;
            case ChatRoomMemberMuteAdd:
                member.setMuted(true);
                break;
            case ChatRoomMemberMuteRemove:
                member.setMuted(false);
                member.setMemberType(MemberType.GUEST);
                break;
            case ChatRoomCommonAdd:
                member.setMemberType(MemberType.NORMAL);
                break;
            case ChatRoomCommonRemove:
                member.setMemberType(MemberType.GUEST);
                break;
            default:
                break;
        }

        saveMember(member);
    }
}
