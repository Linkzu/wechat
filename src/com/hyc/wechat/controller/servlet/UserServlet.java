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

package com.hyc.wechat.controller.servlet;

import com.hyc.wechat.controller.*;
import com.hyc.wechat.controller.Impl.ControllerImpl;
import com.hyc.wechat.controller.Impl.Provider;
import com.hyc.wechat.controller.Impl.UserProvider;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kobe524348@gmail.com">黄钰朝</a>
 * @program wechat
 * @description 用于处理用户相关请求
 * @date 2019-05-02 03:28
 */
@WebServlet("/*")
public class UserServlet extends HttpServlet {
//TODO 改写到web.xml

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Provider userProvider = new UserProvider();

        Controller controller = ControllerImpl.getInstance();
        controller.registerProvider(userProvider.getClass().getName(),userProvider);
        controller.doPost(req, resp);
    }

    private List<Provider> providers = new LinkedList();
}


