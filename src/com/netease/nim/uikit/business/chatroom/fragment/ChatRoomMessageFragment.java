package com.netease.nim.uikit.business.chatroom.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.chatroom.ChatRoomSessionCustomization;
import com.netease.nim.uikit.business.ait.AitManager;
import com.netease.nim.uikit.business.chatroom.helper.ChatRoomHelper;
import com.netease.nim.uikit.business.chatroom.module.ChatRoomInputPanel;
import com.netease.nim.uikit.business.chatroom.module.ChatRoomMsgListPanel;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.robot.model.RobotMsgType;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室直播互动fragment
 * 可以直接集成到应用中
 */
public class ChatRoomMessageFragment extends TFragment implements ModuleProxy {
    private String roomId;
    protected View rootView;
    private static ChatRoomSessionCustomization customization;

    // modules
    protected ChatRoomInputPanel inputPanel;
    protected ChatRoomMsgListPanel messageListPanel;
    protected AitManager aitManager;

    public static void setChatRoomSessionCustomization(ChatRoomSessionCustomization roomSessionCustomization) {
        customization = roomSessionCustomization;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.nim_chat_room_message_fragment, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (inputPanel != null) {
            inputPanel.onPause();
        }
        if (messageListPanel != null) {
            messageListPanel.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (messageListPanel != null) {
            messageListPanel.onResume();
        }
    }

    public boolean onBackPressed() {
        if (inputPanel != null && inputPanel.collapse(true)) {
            return true;
        }

        if (messageListPanel != null && messageListPanel.onBackPressed()) {
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerObservers(false);

        if (messageListPanel != null) {
            messageListPanel.onDestroy();
        }
        if (aitManager != null) {
            aitManager.reset();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (aitManager != null) {
            aitManager.onActivityResult(requestCode, resultCode, data);
        }

        inputPanel.onActivityResult(requestCode, resultCode, data);
    }

    public void onLeave() {
        if (inputPanel != null) {
            inputPanel.collapse(false);
        }
    }

    public void init(String roomId) {
        this.roomId = roomId;
        registerObservers(true);
        findViews();
    }

    private void findViews() {
        Container container = new Container(getActivity(), roomId, SessionTypeEnum.ChatRoom, this);
        if (messageListPanel == null) {
            messageListPanel = new ChatRoomMsgListPanel(container, rootView);
        } else {
            messageListPanel.reload(container);
        }

        if (inputPanel == null) {
            inputPanel = new ChatRoomInputPanel(container, rootView, getActionList(), false);
        } else {
            inputPanel.reload(container, null);
        }

        if (NimUIKitImpl.getOptions().aitEnable && NimUIKitImpl.getOptions().aitChatRoomRobot) {
            if (aitManager == null) {
                aitManager = new AitManager(getContext(), null, true);
            }
            inputPanel.addAitTextWatcher(aitManager);
            aitManager.setTextChangeListener(inputPanel);
        }
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(ChatRoomServiceObserver.class).observeReceiveMessage(incomingChatRoomMsg, register);
    }

    Observer<List<ChatRoomMessage>> incomingChatRoomMsg = new Observer<List<ChatRoomMessage>>() {
        @Override
        public void onEvent(List<ChatRoomMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }

            messageListPanel.onIncomingMessage(messages);
        }
    };

    /************************** Module proxy ***************************/

    @Override
    public boolean sendMessage(IMMessage msg) {
        ChatRoomMessage message = (ChatRoomMessage) msg;

        // 检查是否转换成机器人消息
        message = changeToRobotMsg(message);

        ChatRoomHelper.buildMemberTypeInRemoteExt(message, roomId);

        NIMClient.getService(ChatRoomService.class).sendMessage(message, false)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == ResponseCode.RES_CHATROOM_MUTED) {
                            Toast.makeText(NimUIKit.getContext(), "用户被禁言", Toast.LENGTH_SHORT).show();
                        } else if (code == ResponseCode.RES_CHATROOM_ROOM_MUTED) {
                            Toast.makeText(NimUIKit.getContext(), "全体禁言", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NimUIKit.getContext(), "消息发送失败：code:" + code, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Toast.makeText(NimUIKit.getContext(), "消息发送失败！", Toast.LENGTH_SHORT).show();
                    }
                });
        messageListPanel.onMsgSend(message);
        if (aitManager != null) {
            aitManager.reset();
        }
        return true;
    }

    private ChatRoomMessage changeToRobotMsg(ChatRoomMessage message) {
        if (aitManager == null) {
            return message;
        }
        if (message.getMsgType() == MsgTypeEnum.robot) {
            return message;
        }
        String robotAccount = aitManager.getAitRobot();
        if (TextUtils.isEmpty(robotAccount)) {
            return message;
        }
        String text = message.getContent();
        String content = aitManager.removeRobotAitString(text, robotAccount);
        content = content.equals("") ? " " : content;
        message = ChatRoomMessageBuilder.createRobotMessage(roomId, robotAccount, text, RobotMsgType.TEXT, content, null, null);

        return message;
    }

    @Override
    public void onInputPanelExpand() {
        messageListPanel.scrollToBottom();
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
    }

    @Override
    public void onItemFooterClick(IMMessage message) {
        if (aitManager != null) {
            RobotAttachment attachment = (RobotAttachment) message.getAttachment();
            NimRobotInfo robot = NimUIKit.getRobotInfoProvider().getRobotByAccount(attachment.getFromRobotAccount());
            aitManager.insertAitRobot(robot.getAccount(), robot.getName(), inputPanel.getEditSelectionStart());
        }
    }

    @Override
    public boolean isLongClickEnabled() {
        return !inputPanel.isRecording();
    }

    // 操作面板集合
    protected List<BaseAction> getActionList() {
        List<BaseAction> actions = new ArrayList<>();
        if (customization != null) {
            actions.addAll(customization.actions);
        }

        return actions;
    }
}
