/*
 * Copyright (c) 2019.  黄钰朝
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyc.wechat.provider;

import com.hyc.wechat.controller.constant.RequestMethod;
import com.hyc.wechat.factory.ServiceProxyFactory;
import com.hyc.wechat.model.dto.ServiceResult;
import com.hyc.wechat.model.po.Chat;
import com.hyc.wechat.model.po.Friend;
import com.hyc.wechat.model.po.Member;
import com.hyc.wechat.provider.annotation.Action;
import com.hyc.wechat.provider.annotation.ActionProvider;
import com.hyc.wechat.service.ChatService;
import com.hyc.wechat.service.FriendService;
import com.hyc.wechat.service.constants.ServiceMessage;
import com.hyc.wechat.service.impl.ChatServiceImpl;
import com.hyc.wechat.service.impl.FriendServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;

import static com.hyc.wechat.service.constants.Status.ERROR;
import static com.hyc.wechat.util.BeanUtils.jsonToJavaObject;
import static com.hyc.wechat.util.ControllerUtils.returnJsonObject;

/**
 * @author <a href="mailto:kobe524348@gmail.com">黄钰朝</a>
 * @description 负责好友相关业务流程
 * @date 2019-05-06 07:11
 */
@ActionProvider(path = "/friend")
public class FriendProvider extends Provider {
    private final FriendService friendService = (FriendService) new ServiceProxyFactory().getProxyInstance(new FriendServiceImpl());
    private final ChatService chatService = (ChatService) new ServiceProxyFactory().getProxyInstance(new ChatServiceImpl());

    /**
     * 提供添加好友的业务流程
     *
     * @name addFriend
     * @notice none
     * @author <a href="mailto:kobe524348@gmail.com">黄钰朝</a>
     * @date 2019/5/9
     */
    @Action(method = RequestMethod.ADD_DO)
    synchronized public void addFriend(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Friend friend = (Friend) jsonToJavaObject(req.getInputStream(), Friend.class);
        ServiceResult result;
        //尝试添加好友
        result = friendService.addFriend(friend);
        if (ERROR.equals(result.getStatus())) {
            returnJsonObject(resp, result);
            return;
        }
        if (friendService.isFriend(friend)) {
            //添加好友后判断是否对方也将自己添加为好友，如果双向添加，则建立聊天关系
            Chat chat = new Chat();
            //这里查到双向添加好友说明对方先加我为好友，聊天所有者为对方
            chat.setOwnerId(friend.getFriendId());
            chat = (Chat) chatService.createChat(chat, false).getData();
            //创建两个成员，将双方加入聊天中
            Member member1 = new Member();
            member1.setUserId(friend.getUserId());
            member1.setChatId(chat.getId());
            Member member2 = new Member();
            member2.setUserId(friend.getFriendId());
            member2.setChatId(chat.getId());
            chatService.joinChat(new Member[]{member1, member2});
            //添加成功后将聊天id更新到朋友信息中
            friend = friendService.getByUidAndFriendId(friend.getUserId(), friend.getFriendId());
            friend.setChatId(chat.getId());
            friendService.updateFriend(friend);
            friend = friendService.getByUidAndFriendId(friend.getFriendId(), friend.getUserId());
            friend.setChatId(chat.getId());
            friendService.updateFriend(friend);

        }
        returnJsonObject(resp, result);
    }

    /**
     * 提供获取好友列表的服务
     *
     * @name listFriend
     * @notice none
     * @author <a href="mailto:kobe524348@gmail.com">黄钰朝</a>
     * @date 2019/5/9
     */
    @Action(method = RequestMethod.LIST_DO)
    public void listFriend(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter("user_id");
        ServiceResult result;
        result = friendService.listFriend(new BigInteger(userId));
        returnJsonObject(resp, result);
    }

    /**
     * 提供获取好友列表的服务
     *
     * @name listFriend
     * @notice none
     * @author <a href="mailto:kobe524348@gmail.com">黄钰朝</a>
     * @date 2019/5/9
     */
    @Action(method = RequestMethod.UPDATE_DO)
    public void updateFriend(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Friend friend = (Friend) jsonToJavaObject(req.getInputStream(), Friend.class);
        ServiceResult result;
        result = friendService.updateFriend(friend);
        returnJsonObject(resp, result);
    }

    /**
     * 提供删除好友的服务
     *
     * @name deleteFriend
     * @notice none
     * @author <a href="mailto:kobe524348@gmail.com">黄钰朝</a>
     * @date 2019/5/9
     */
    @Action(method = RequestMethod.DELETE_DO)
    synchronized public void deleteFriend(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String friendId = req.getParameter("friend_id");
        String userId = req.getParameter("user_id");
        ServiceResult result;
        //删除好友之前将聊天关系移除,请求中的好友对象只含有userId和friendId，所以需要重新获取
        Friend friend = friendService.getByUidAndFriendId(new BigInteger(userId), new BigInteger(friendId));
        if (friend == null) {
            result = new ServiceResult();
            result.setMessage(ServiceMessage.FRIEND_NOT_EXIST.message);
            result.setStatus(ERROR);
            returnJsonObject(resp, result);
            return;
        }
        //调用聊天服务将聊天关系解除
        Chat chat = new Chat();
        chat.setId(friend.getChatId());
        chatService.removeChat(chat);
        //解除好友关系
        result = friendService.removeFriend(friend);
        returnJsonObject(resp, result);
    }

}
