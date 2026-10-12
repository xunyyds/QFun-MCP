boolean isToolEnabled(String name) {
    try {
        return getBoolean("mcp", "tool_" + name, false);
    } catch (Exception e) {
        return false;
    }
}

int totalToolCount() {
    try {
        return listTools(false).getJSONArray("tools").length();
    } catch (Exception e) {
        return 0;
    }
}

int enabledToolCount() {
    try {
        return listTools(true).getJSONArray("tools").length();
    } catch (Exception e) {
        return 0;
    }
}

String argStr(JSONObject a, String k, String d) {
    try {
        Object v = a.opt(k);
        if (v == null) return d;
        String s = String.valueOf(v).trim();
        if (s.length() == 0) return d;
        return s;
    } catch (Exception e) {
        return d;
    }
}

long argLong(JSONObject a, String k, long d) {
    try {
        Object v = a.opt(k);
        if (v == null) return d;
        return Long.parseLong(String.valueOf(v).trim());
    } catch (Exception e) {
        return d;
    }
}

int argInt(JSONObject a, String k, int d) {
    return (int) argLong(a, k, d);
}

boolean argBool(JSONObject a, String k, boolean d) {
    try {
        Object v = a.opt(k);
        if (v == null) return d;
        if (v instanceof Boolean) return ((Boolean) v).booleanValue();
        String s = String.valueOf(v).trim();
        if ("true".equals(s) || "1".equals(s) || "yes".equals(s)) return true;
        if ("false".equals(s) || "0".equals(s) || "no".equals(s)) return false;
        return d;
    } catch (Exception e) {
        return d;
    }
}

JSONObject dumpObj(Object o) {
    JSONObject j = new JSONObject();
    if (o == null) return j;
    try {
        Field[] fs = o.getClass().getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            try {
                fs[i].setAccessible(true);
                j.put(fs[i].getName(), String.valueOf(fs[i].get(o)));
            } catch (Exception e) {}
        }
    } catch (Exception e) {}
    return j;
}

JSONObject buildTool(String name, String desc, Object[] props) {
    JSONObject t = new JSONObject();
    try {
        t.put("name", name);
        t.put("description", desc);
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        JSONObject pj = new JSONObject();
        JSONArray req = new JSONArray();
        for (int i = 0; i < props.length; i++) {
            Object[] p = (Object[]) props[i];
            JSONObject pr = new JSONObject();
            pr.put("type", (String) p[1]);
            pr.put("description", (String) p[2]);
            pj.put((String) p[0], pr);
            if ("req".equals(p[3])) req.put((String) p[0]);
        }
        schema.put("properties", pj);
        if (req.length() > 0) schema.put("required", req);
        t.put("inputSchema", schema);
    } catch (Exception e) {}
    return t;
}

JSONObject listTools(boolean onlyEnabled) {
    JSONObject o = new JSONObject();
    JSONArray ts = new JSONArray();
    try {
        ts.put(buildTool("send_msg", "发送文本消息内容支持 [atUin=QQ号]（0=全体成员）与 [pic=图片链接或本地路径] 组合", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"content", "string", "消息内容", "req"},
            new Object[]{"chat_type", "integer", "1=好友/私聊 2=群聊 100=陌生人，默认1", "opt"}
        }));
        ts.put(buildTool("send_pic", "发送图片", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"path", "string", "图片本地绝对路径", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_ptt", "发送语音", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"path", "string", "语音文件绝对路径", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_card", "发送 JSON 卡片消息", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"json", "string", "卡片 JSON 内容", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_file", "发送文件", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"path", "string", "文件绝对路径", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_video", "发送视频", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"path", "string", "视频文件绝对路径", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("reply_msg", "引用回复某条消息", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"msg_id", "integer", "被引用消息的 msgId", "req"},
            new Object[]{"content", "string", "回复内容", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("recall_msg", "撤回一条消息", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"msg_id", "integer", "要撤回的消息 msgId", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_pai", "拍一拍某人", new Object[]{
            new Object[]{"target_uin", "string", "被拍者QQ号", "req"},
            new Object[]{"peer_uin", "string", "群聊传群号，私聊传好友QQ", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("get_friends", "获取全部好友列表，返回 uin/uid/name/remark", new Object[]{}));
        ts.put(buildTool("is_friend", "判断某 QQ 号是否好友", new Object[]{
            new Object[]{"uin", "string", "QQ号", "req"}
        }));
        ts.put(buildTool("get_groups", "获取全部群列表，返回 group/name/owner", new Object[]{}));
        ts.put(buildTool("get_group_members", "获取群成员列表，返回 uin/name/role/joinGroupTime/lastActiveTime", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("get_member_info", "获取单个群成员资料", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"}
        }));
        ts.put(buildTool("get_prohibit_list", "获取群禁言列表，返回 user/userName/time/endTime", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("get_group_info", "获取群原始信息（TroopInfo 字段导出）", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("uin_to_uid", "QQ号(Uin) 转 Uid", new Object[]{
            new Object[]{"uin", "string", "QQ号", "req"}
        }));
        ts.put(buildTool("uid_to_uin", "Uid 转 QQ号(Uin)", new Object[]{
            new Object[]{"uid", "string", "Uid", "req"}
        }));
        ts.put(buildTool("send_zan", "给好友点赞", new Object[]{
            new Object[]{"uin", "string", "好友QQ号", "req"},
            new Object[]{"count", "integer", "点赞次数，默认1", "opt"}
        }));
        ts.put(buildTool("get_self_info", "获取当前登录 QQ 号与本服务器连接信息", new Object[]{}));
        ts.put(buildTool("shut_up", "禁言群成员，seconds=0 解除禁言", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"seconds", "integer", "禁言秒数，0=解禁，默认600", "opt"}
        }));
        ts.put(buildTool("shut_up_all", "开启/关闭全员禁言", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"enable", "boolean", "true=开启 false=关闭，默认true", "opt"}
        }));
        ts.put(buildTool("is_shut_up", "查询群是否处于全员禁言", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("kick", "踢出群成员", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"black", "boolean", "是否拉黑（拒绝再次入群），默认false", "opt"}
        }));
        ts.put(buildTool("set_admin", "设置/取消群管理员", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"enable", "boolean", "true=设为管理员 false=取消，默认true", "opt"}
        }));
        ts.put(buildTool("set_title", "设置成员群头衔（仅群主可用）", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"title", "string", "头衔文本，空串清除", "req"}
        }));
        ts.put(buildTool("change_card", "修改成员群名片", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"name", "string", "新群名片", "req"}
        }));
        ts.put(buildTool("clock_in", "群打卡", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("get_skey", "获取当前账号 skey", new Object[]{}));
        ts.put(buildTool("get_real_skey", "获取当前账号 realSkey", new Object[]{}));
        ts.put(buildTool("get_pskey", "获取指定域名 pskey", new Object[]{
            new Object[]{"domain", "string", "域名，如 qzone.qq.com", "req"}
        }));
        ts.put(buildTool("get_pt4_token", "获取指定域名 pt4Token", new Object[]{
            new Object[]{"domain", "string", "域名", "req"}
        }));
        ts.put(buildTool("get_stweb", "获取 stweb 凭证", new Object[]{}));
        ts.put(buildTool("get_gtk", "获取指定域名 GTK 值", new Object[]{
            new Object[]{"domain", "string", "域名", "req"}
        }));
        ts.put(buildTool("get_group_rkey", "获取群聊图片 RKey", new Object[]{}));
        ts.put(buildTool("get_friend_rkey", "获取私聊图片 RKey", new Object[]{}));
        ts.put(buildTool("get_bkn", "计算 BKN", new Object[]{
            new Object[]{"key", "string", "参与计算的 key", "req"}
        }));
        ts.put(buildTool("run_script", "【高级】在手机端执行 BeanShell 脚本并返回结果，可调用全部 QFun API超时 30 秒高风险工具，建议默认禁用", new Object[]{
            new Object[]{"code", "string", "BeanShell 代码；返回值 toString 会回传", "req"}
        }));

        JSONArray filtered = new JSONArray();
        for (int i = 0; i < ts.length(); i++) {
            JSONObject t = ts.getJSONObject(i);
            if (!onlyEnabled || isToolEnabled(t.optString("name", ""))) filtered.put(t);
        }
        o.put("tools", filtered);
    } catch (Exception e) {
        log("/Log/mcp.txt", "listTools 失败: " + e.getMessage());
    }
    return o;
}

void addCall(String name, String args, boolean ok, long ms, String text) {
    try {
        String a = args == null ? "" : args;
        if (a.length() > 100) a = a.substring(0, 100) + "..";
        String time = new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date());
        String line = time + " | " + name + (a.length() > 0 ? " " + a : "") + " | " + (ok ? "ok" : "失败") + " " + ms + "ms";
        if (!ok && text != null && text.length() > 0) line = line + " | " + text;
        synchronized (callLog) {
            callLog.add(0, line);
            while (callLog.size() > 200) callLog.remove(callLog.size() - 1);
        }
        log("/Log/mcp_calls.txt", line);
    } catch (Exception e) {}
}

JSONObject callTool(JSONObject params) {
    JSONObject res = new JSONObject();
    try {
        String name = params.optString("name", "");
        JSONObject args = params.optJSONObject("arguments");
        if (args == null) args = new JSONObject();
        long t0 = System.currentTimeMillis();
        JSONObject r = invokeTool(name, args);
        long ms = System.currentTimeMillis() - t0;
        addCall(name, args.toString(), !r.optBoolean("isError", false), ms, r.optString("text", ""));
        JSONArray content = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("type", "text");
        item.put("text", r.optString("text", ""));
        content.put(item);
        res.put("content", content);
        res.put("isError", r.optBoolean("isError", false));
    } catch (Exception e) {
        try {
            JSONArray content = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("type", "text");
            item.put("text", "调用失败: " + e.getMessage());
            content.put(item);
            res.put("content", content);
            res.put("isError", true);
        } catch (Exception ex) {}
    }
    return res;
}

void setOk(JSONObject r) {
    try { r.put("text", "ok"); r.put("isError", false); } catch (Exception e) {}
}

void setJson(JSONObject r, Object v) {
    try { r.put("text", String.valueOf(v)); r.put("isError", false); } catch (Exception e) {}
}

void setFail(JSONObject r, String msg) {
    try { r.put("text", msg); r.put("isError", true); } catch (Exception e) {}
}

JSONObject invokeTool(String name, JSONObject a) {
    JSONObject r = new JSONObject();
    try {
        if (!isToolEnabled(name)) {
            setFail(r, "工具已禁用，请在手机端 MCP 面板启用");
            return r;
        }
        if ("send_msg".equals(name)) {
            sendMsg(argStr(a, "uin", ""), argStr(a, "content", ""), argInt(a, "chat_type", 1));
            setOk(r);
        } else if ("send_pic".equals(name)) {
            sendPic(argStr(a, "uin", ""), argStr(a, "path", ""), argInt(a, "chat_type", 1));
            setOk(r);
        } else if ("send_ptt".equals(name)) {
            sendPtt(argStr(a, "uin", ""), argStr(a, "path", ""), argInt(a, "chat_type", 1));
            setOk(r);
        } else if ("send_card".equals(name)) {
            sendCard(argStr(a, "uin", ""), argStr(a, "json", ""), argInt(a, "chat_type", 1));
            setOk(r);
        } else if ("send_file".equals(name)) {
            sendFile(argStr(a, "uin", ""), argStr(a, "path", ""), argInt(a, "chat_type", 1));
            setOk(r);
        } else if ("send_video".equals(name)) {
            sendVideo(argStr(a, "uin", ""), argStr(a, "path", ""), argInt(a, "chat_type", 1));
            setOk(r);
        } else if ("reply_msg".equals(name)) {
            sendReplyMsg(argStr(a, "uin", ""), argLong(a, "msg_id", 0), argStr(a, "content", ""), argInt(a, "chat_type", 1));
            setOk(r);
        } else if ("recall_msg".equals(name)) {
            recallMsg(argInt(a, "chat_type", 1), argStr(a, "uin", ""), argLong(a, "msg_id", 0));
            setOk(r);
        } else if ("send_pai".equals(name)) {
            sendPai(argStr(a, "target_uin", ""), argStr(a, "peer_uin", ""), argInt(a, "chat_type", 1));
            setOk(r);
        } else if ("get_friends".equals(name)) {
            List fs = getAllFriend();
            JSONArray arr = new JSONArray();
            for (int i = 0; i < fs.size(); i++) {
                Object f = fs.get(i);
                JSONObject o = new JSONObject();
                o.put("uin", String.valueOf(f.uin));
                o.put("uid", String.valueOf(f.uid));
                o.put("name", String.valueOf(f.name));
                o.put("remark", String.valueOf(f.remark));
                arr.put(o);
            }
            setJson(r, arr);
        } else if ("is_friend".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("isFriend", isFriend(argStr(a, "uin", "")));
            setJson(r, o);
        } else if ("get_groups".equals(name)) {
            List gs = getGroupList();
            JSONArray arr = new JSONArray();
            for (int i = 0; i < gs.size(); i++) {
                Object g = gs.get(i);
                JSONObject o = new JSONObject();
                o.put("group", String.valueOf(g.group));
                o.put("name", String.valueOf(g.groupName));
                o.put("owner", String.valueOf(g.groupOwner));
                arr.put(o);
            }
            setJson(r, arr);
        } else if ("get_group_members".equals(name)) {
            List ms = getGroupMemberList(argStr(a, "group_uin", ""));
            JSONArray arr = new JSONArray();
            for (int i = 0; i < ms.size(); i++) {
                Object m = ms.get(i);
                JSONObject o = new JSONObject();
                o.put("uin", String.valueOf(m.uin));
                o.put("name", String.valueOf(m.uinName));
                o.put("role", String.valueOf(m.role));
                o.put("joinGroupTime", String.valueOf(m.joinGroupTime));
                o.put("lastActiveTime", String.valueOf(m.lastActiveTime));
                arr.put(o);
            }
            setJson(r, arr);
        } else if ("get_member_info".equals(name)) {
            Object mi = getMemberInfo(argStr(a, "group_uin", ""), argStr(a, "uin", ""));
            if (mi == null) setFail(r, "未找到该成员");
            else {
                JSONObject o = new JSONObject();
                o.put("uin", String.valueOf(mi.uin));
                o.put("name", String.valueOf(mi.uinName));
                o.put("role", String.valueOf(mi.role));
                o.put("joinGroupTime", String.valueOf(mi.joinGroupTime));
                o.put("lastActiveTime", String.valueOf(mi.lastActiveTime));
                setJson(r, o);
            }
        } else if ("get_prohibit_list".equals(name)) {
            List ps = getProhibitList(argStr(a, "group_uin", ""));
            JSONArray arr = new JSONArray();
            for (int i = 0; i < ps.size(); i++) {
                Object p = ps.get(i);
                JSONObject o = new JSONObject();
                o.put("uin", String.valueOf(p.user));
                o.put("name", String.valueOf(p.userName));
                o.put("remainSeconds", String.valueOf(p.time));
                o.put("endTime", String.valueOf(p.endTime));
                arr.put(o);
            }
            setJson(r, arr);
        } else if ("get_group_info".equals(name)) {
            Object gi = getGroupInfo(argStr(a, "group_uin", ""));
            setJson(r, dumpObj(gi));
        } else if ("uin_to_uid".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("uid", getUidFromUin(argStr(a, "uin", "")));
            setJson(r, o);
        } else if ("uid_to_uin".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("uin", getUinFromUid(argStr(a, "uid", "")));
            setJson(r, o);
        } else if ("send_zan".equals(name)) {
            sendZan(argStr(a, "uin", ""), argInt(a, "count", 1));
            setOk(r);
        } else if ("get_self_info".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("uin", myUin);
            String ip = serverAddr[0];
            if (ip == null || ip.length() == 0) ip = getLocalIp();
            o.put("server", "http://" + ip + ":" + mcpNowPort() + "/mcp");
            setJson(r, o);
        } else if ("shut_up".equals(name)) {
            shutUp(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argLong(a, "seconds", 600));
            setOk(r);
        } else if ("shut_up_all".equals(name)) {
            shutUpAll(argStr(a, "group_uin", ""), argBool(a, "enable", true));
            setOk(r);
        } else if ("is_shut_up".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("isShutUp", isShutUp(argStr(a, "group_uin", "")));
            setJson(r, o);
        } else if ("kick".equals(name)) {
            kickGroup(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argBool(a, "black", false));
            setOk(r);
        } else if ("set_admin".equals(name)) {
            setGroupAdmin(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argBool(a, "enable", true));
            setOk(r);
        } else if ("set_title".equals(name)) {
            setGroupMemberTitle(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argStr(a, "title", ""));
            setOk(r);
        } else if ("change_card".equals(name)) {
            changeMemberName(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argStr(a, "name", ""));
            setOk(r);
        } else if ("clock_in".equals(name)) {
            clockIn(argStr(a, "group_uin", ""));
            setOk(r);
        } else if ("get_skey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("skey", getSkey());
            setJson(r, o);
        } else if ("get_real_skey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("realSkey", getRealSkey());
            setJson(r, o);
        } else if ("get_pskey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("pskey", getPskey(argStr(a, "domain", "")));
            setJson(r, o);
        } else if ("get_pt4_token".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("pt4Token", getPt4Token(argStr(a, "domain", "")));
            setJson(r, o);
        } else if ("get_stweb".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("stweb", getStweb());
            setJson(r, o);
        } else if ("get_gtk".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("gtk", String.valueOf(getGTK(argStr(a, "domain", ""))));
            setJson(r, o);
        } else if ("get_group_rkey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("rkey", getGroupRKey());
            setJson(r, o);
        } else if ("get_friend_rkey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("rkey", getFriendRKey());
            setJson(r, o);
        } else if ("get_bkn".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("bkn", String.valueOf(getBkn(argStr(a, "key", ""))));
            setJson(r, o);
        } else if ("run_script".equals(name)) {
            String code = argStr(a, "code", "");
            if (code.length() == 0) { setFail(r, "code 不能为空"); return r; }
            try {
                log("/Log/mcp_script.txt", "\n=== " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ===\n" + code + "\n");
            } catch (Exception e) {}
            final Object[] res = new Object[1];
            final Throwable[] err = new Throwable[1];
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try { res[0] = this.interpreter.eval(code); }
                    catch (Throwable e) { err[0] = e; }
                }
            });
            t.start();
            t.join(30000);
            if (t.isAlive()) { t.interrupt(); setFail(r, "执行超时(30秒)，已尝试中断"); }
            else if (err[0] != null) setFail(r, "执行异常: " + err[0]);
            else setJson(r, res[0] == null ? "null" : String.valueOf(res[0]));
        } else {
            setFail(r, "未知工具: " + name);
        }
    } catch (Exception e) {
        setFail(r, "执行失败: " + e.getMessage());
    }
    return r;
}